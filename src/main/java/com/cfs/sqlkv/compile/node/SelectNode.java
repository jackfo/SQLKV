package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.predicate.*;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.table.FromBaseTable;
import com.cfs.sqlkv.engine.execute.BaseProjectResult;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 18:49
 */
public class SelectNode extends ResultSetNode {
    public FromList fromList;
    public FromTable targetTable;
    public ValueNode whereClause;
    public PredicateList wherePredicates;

    private int nestingLevel;

    public SelectNode(ContextManager contextManager) {
        super(contextManager);
    }

    public SelectNode(ResultColumnList selectList, FromList fromList, ValueNode whereClause, ContextManager contextManager) {
        super(contextManager);
        this.fromList = fromList;
        this.whereClause = whereClause;
        setResultColumns(selectList);
    }


    /**
     * 最终会将fromListParam中所有的Table加到当前节点的fromList
     * 之后对所有的table做一个绑定
     */
    @Override
    public ResultSetNode bindNonVTITables(DataDictionary dataDictionary, FromList fromListParam) {
        int fromListSize = fromList.size();
        wherePredicates = new PredicateList(getContextManager());
        if (fromListParam.size() == 0) {
            nestingLevel = 0;
        } else {
            nestingLevel = ((FromTable) fromListParam.elementAt(0)).getLevel() + 1;
        }
        fromList.setLevel(nestingLevel);
        for (int index = 0; index < fromListSize; index++) {
            fromListParam.insertElementAt(fromList.elementAt(index), 0);
        }
        fromList.bindTables(dataDictionary, fromListParam);
        for (int index = 0; index < fromListSize; index++) {
            fromListParam.removeElementAt(0);
        }
        return this;
    }

    @Override
    public void bindResultColumns(FromList fromListParam) {
        fromList.bindResultColumns(fromListParam);
        super.bindResultColumns(fromListParam);
        if (getResultColumns().size() == 0) {
            throw new RuntimeException("列的集合为空");
        }
    }

    @Override
    public void bindResultColumns(FromList fromListParam, TableDescriptor targetTableDescriptor, ResultColumnList targetColumnList) {
        fromList.bindResultColumns(fromListParam);
        super.bindResultColumns(fromListParam, targetTableDescriptor, targetColumnList);
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {

    }

    /**
     * 对Select节点做预处理
     * 包括查询表和结果集的处理
     */
    @Override
    public ResultSetNode preprocess(int numTables, FromList fl) {
        ResultSetNode newTop = this;

        whereClause = normExpressions(whereClause);

        //在里面处理获取到FromBaseTable节点处理 之后添加到集合
        fromList.preprocess(numTables);
        ResultColumnList resultColumnList = getResultColumns();
        resultColumnList.preprocess(numTables, fromList, wherePredicates);
        if (whereClause != null) {
            whereClause = whereClause.preprocess(numTables, fromList, wherePredicates);
        }
        //将where条件添加到谓语集合
        if (whereClause != null) {
            wherePredicates.pullExpressions(numTables, whereClause);
            whereClause = null;
        }
        fromList.pushPredicates(wherePredicates);
        return newTop;
    }

    /**
     * 规范化表达式
     *
     * @param boolClause 代表字句 可能是WHERE字句 也可能是HAVING字句
     */
    private ValueNode normExpressions(ValueNode boolClause) {
        if (boolClause != null) {
            boolClause = boolClause.putAndsOnTop();
        }
        return boolClause;
    }

    /**
     * 修改可利用结果集节点,主要是做优化
     * 这里针对Select条件,一般存在添加约束条件以及索引
     */
    @Override
    public ResultSetNode modifyAccessPaths() {
        getOptimizer().modifyAccessPaths();
        ResultColumnList resultColumnList = getResultColumns();
        BaseProjectNode baseProjectNode = new BaseProjectNode(fromList.elementAt(0), resultColumnList, whereClause, wherePredicates, getContextManager());
        return baseProjectNode;
    }

    @Override
    public ResultSetNode optimize(DataDictionary dataDictionary, PredicateList predicateList) {
        getOptimizer(fromList, wherePredicates, dataDictionary);
        if (wherePredicates != null) {
            for (int i = wherePredicates.size() - 1; i >= 0; i--) {
                Predicate pred = (Predicate) wherePredicates.getOptPredicate(i);
                predicateList.addOptPredicate(pred);
                wherePredicates.removeOptPredicate(pred);
            }
        }

        return this;
    }


    private Optimizer getOptimizer(OptimizableList optList, OptimizablePredicateList predList, DataDictionary dataDictionary) {
        if (getOptimizer() == null) {
            Optimizer optimizer = new OptimizerImpl(optList, predList, dataDictionary);
            setOptimizer(optimizer);
        }
        return getOptimizer();
    }

    @Override
    public void bindExpressions(FromList fromListParam) {
        fromList.bindExpressions(fromListParam);
        int fromListSize = fromList.size();
        for (int index = 0; index < fromListSize; index++) {
            fromListParam.insertElementAt(fromList.elementAt(index), index);
        }
        getResultColumns().bindExpressions(fromListParam);
        if (whereClause != null) {
            whereClause = whereClause.bindExpression(fromListParam);
        }
    }

    public ValueNode getWhereClause() {
        return whereClause;
    }

    public void pushExpressionsIntoSelect(Predicate predicate) {
        wherePredicates.pullExpressions(1, predicate.getAndNode());
        fromList.pushPredicates(wherePredicates);
    }
}
