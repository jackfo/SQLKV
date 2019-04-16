package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ColumnReference;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.predicate.*;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.compile.result.ResultColumnList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 18:49
 */
public abstract class FromTable extends ResultSetNode implements Optimizable {

    public TableName origTableName;

    public String correlationName;

    public int tableNumber;
    public AccessPathImpl currentAccessPath;
    public AccessPathImpl bestAccessPath;
    public AccessPathImpl bestSortAvoidancePath;
    public AccessPathImpl trulyTheBestAccessPath;

    public FromTable(String correlationName, ContextManager contextManager) {
        super(contextManager);
        this.correlationName = correlationName;
        tableNumber = -1;
    }

    public FromTable(ContextManager contextManager) {
        super(contextManager);
    }

    public void setOrigTableName(TableName tableName) {
        this.origTableName = tableName;
    }

    public TableName getOrigTableName() {
        return this.origTableName;
    }

    public ResultColumnList getResultColumnsForList(TableName allTableName, ResultColumnList inputRcl, TableName tableName) {
        TableName exposedName;
        if (correlationName == null) {
            exposedName = tableName;
        } else {
            exposedName = makeTableName(null, correlationName);
        }
        final ContextManager cm = getContextManager();
        ResultColumnList rcList = new ResultColumnList(cm);
        for (ResultColumn rc : inputRcl) {
            ColumnReference newCR = new ColumnReference(rc.getName(), exposedName, cm);
            ResultColumn newRc = new ResultColumn(rc.getName(), newCR, cm);
            rcList.addResultColumn(newRc);
        }
        return rcList;
    }


    public void pushExpressions(PredicateList predicateList) {

    }

    @Override
    public Optimizable modifyAccessPath() {
        return this;
    }

    public void initAccessPaths(Optimizer optimizer) {
        if (currentAccessPath == null) {
            currentAccessPath = new AccessPathImpl(optimizer);
        }
        if (bestAccessPath == null) {
            bestAccessPath = new AccessPathImpl(optimizer);
        }
        if (bestSortAvoidancePath == null) {
            bestSortAvoidancePath = new AccessPathImpl(optimizer);
        }
        if (trulyTheBestAccessPath == null) {
            trulyTheBestAccessPath = new AccessPathImpl(optimizer);
        }
    }

    public int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getBaseTableName() {
        return "";
    }

    public AccessPath getCurrentAccessPath() {
        return currentAccessPath;
    }

    /**
     * 目前不支持多条AccessPath
     * */
    public boolean nextAccessPath(Optimizer optimizer, OptimizablePredicateList predList) {
        return false;
    }

    @Override
    public void optimizeIt(Optimizer optimizer, OptimizablePredicateList predList) {

    }


    public boolean pushOptPredicate(OptimizablePredicate optimizablePredicate) {
        return false;
    }

}
