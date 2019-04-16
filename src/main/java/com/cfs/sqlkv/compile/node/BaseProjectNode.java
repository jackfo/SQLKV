package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.ReferencedColumnsDescriptorImpl;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.predicate.*;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.engine.execute.RowChanger;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.loader.GeneratedMethod;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-08 15:42
 */
public class BaseProjectNode extends SingleChildResultSetNode {

    int bulkFetch = 16;

    public ValueNode restriction;

    /**
     * 约束条件集合
     */
    public PredicateList restrictionList;

    public BaseProjectNode(ResultSetNode childResult, ResultColumnList resultColumns, ValueNode restriction, PredicateList restrictionList, ContextManager cm) {
        super(childResult, cm);
        setResultColumns(resultColumns);
        this.restriction = restriction;
        this.restrictionList = restrictionList;
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        generateMinion(acb, mb, false);
    }

    private void generateMinion(ActivationClassBuilder acb, MethodBuilder mb, boolean genChildResultSet) {
        //如果没有约束条件判断为真,没有的话则是FromBaseTable对应的优化节点生成结果集
        if (nopProjectRestrict()) {
            if (genChildResultSet) {
                childResult.generateResultSet(acb, mb);
            } else {
                childResult.generate(acb, mb);
            }
            return;
        }

        if (restrictionList != null) {
            restrictionList = null;
        }

        ResultColumnList.ColumnMapping mappingArrays = getResultColumns().mapSourceColumns();
        int[] mapArray = mappingArrays.mapArray;
        boolean[] cloneMap = mappingArrays.cloneMap;


        int mapArrayItem = acb.addItem(new ReferencedColumnsDescriptorImpl(mapArray));
        int cloneMapItem = acb.addItem(cloneMap);
        boolean doesProjection = true;
        if ((!reflectionNeededForProjection()) &&
                mapArray != null &&
                mapArray.length == childResult.getResultColumns().size()) {
            int index = 0;
            for (; index < mapArray.length; index++) {
                if (mapArray[index] != index + 1) {
                    break;
                }
            }
            if (index == mapArray.length) {
                doesProjection = false;
            }
        }
        //以下是为有约束条件做处理
        acb.pushGetResultSetFactoryExpression(mb);
        childResult.generate(acb, mb);
        mb.pushNull(GeneratedMethod.class.getName());

        /**
         * 是否需要为约束条件做反射处理,如果需要,在这里会生成反射方法
         * 在实际获取下一行的时候会进行调用
         * */
        if (reflectionNeededForProjection()) {
            getResultColumns().generateCore(acb, mb, false);
        } else {
            mb.pushNull(GeneratedMethod.class.getName());
        }
        mb.push(mapArrayItem);
        mb.push(cloneMapItem);
        mb.push(doesProjection);
        assignResultSetNumber();
        mb.push(getResultSetNumber());
        mb.callMethod(VMOpcode.INVOKEINTERFACE, null, "getBaseProjectResult", NoPutResultSet.class.getName(), 7);
    }


    protected boolean reflectionNeededForProjection() {
        return !(getResultColumns().allExpressionsAreColumns(childResult));
    }

    /**
     * 由于SelectNode和FromBaseTable都会封装成BaseProjectNode
     * 而FromTable是不会添加约束表达式的,故需要进行相应的验证
     */
    @Override
    public void pushExpressions(PredicateList predicateList) {
        PredicateList pushPList = predicateList.getPushablePredicates();
        if (pushPList != null && childResult instanceof SelectNode) {
            SelectNode childSelect = (SelectNode) childResult;
            predicateList.pushExpressionsIntoSelect(childSelect);
        }
        if (restrictionList == null) {
            restrictionList = pushPList;
        }
    }

    @Override
    public Optimizable modifyAccessPath() {
        if (!(childResult instanceof Optimizable)) {
            childResult = childResult.modifyAccessPaths(restrictionList);
        }
        childResult = childResult.changeAccessPath();
        if (restrictionList != null) {
            restrictionList.pushUsefulPredicates((Optimizable) childResult);
        }
        return this;
    }

    @Override
    public boolean pushOptPredicate(OptimizablePredicate optimizablePredicate) {
        if (restrictionList == null) {
            restrictionList = new PredicateList(getContextManager());
        }
        restrictionList.addPredicate((Predicate) optimizablePredicate);
        return true;
    }


    /**
     * 判断当前操作是否具有约束条件
     * <p>
     * 保证孩子结果集的结果列和当前节点的结果列是相同
     * 如FromBaseTable  会生成BaseProjectNode  之后二者需要结果集相同才行
     */
    public boolean nopProjectRestrict() {
        if ((restrictionList != null && restrictionList.size() > 0) || restriction != null) {
            return false;
        }
        ResultColumnList childColumns = childResult.getResultColumns();
        ResultColumnList PRNColumns = this.getResultColumns();
        if (PRNColumns.nopProjection(childColumns)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean nextAccessPath(Optimizer optimizer, OptimizablePredicateList predList) {
        if (childResult instanceof Optimizable) {
            //主要目的是将列Conglom描述设置到当前利用path
            return ((Optimizable) childResult).nextAccessPath(optimizer, restrictionList);
        } else {
            return super.nextAccessPath(optimizer, predList);
        }
    }
}
