package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ColumnReference;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.predicate.Optimizable;
import com.cfs.sqlkv.compile.predicate.OptimizableList;
import com.cfs.sqlkv.compile.predicate.Optimizer;
import com.cfs.sqlkv.compile.predicate.PredicateList;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.compile.result.ResultColumnList;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 18:48
 */
public class FromList extends QueryTreeNodeVector<ResultSetNode> implements OptimizableList {

    public FromList(ContextManager contextManager) {
        super(ResultSetNode.class, contextManager);
    }

    public final void addFromTable(FromTable fromTable) {
        addElement(fromTable);
    }

    public void bindResultColumns(FromList fromListParam) {
        FromTable fromTable;
        int origList = fromListParam.size();
        int size = size();
        for (int index = 0; index < size; index++) {
            fromTable = (FromTable) elementAt(index);
            fromListParam.insertElementAt(fromTable, 0);
        }
        while (fromListParam.size() > origList) {
            fromListParam.removeElementAt(0);
        }
    }

    /**
     * 处理所有的表
     * */
    public void preprocess(int numTables) {
        int size = size();
        for (int index = 0; index < size; index++) {
            FromTable ft = (FromTable) elementAt(index);
            setElementAt(ft.preprocess(numTables, this), index);
        }
    }

    @Override
    public Optimizable getOptimizable(int index) {
        return (Optimizable) elementAt(index);
    }

    @Override
    public void setOptimizable(int index, Optimizable optimizable) {
        setElementAt((FromTable) optimizable, index);
    }

    /**
     * 对集合中所有表进行绑定
     * */
    public void bindTables(DataDictionary dataDictionary, FromList fromListParam) {
        FromTable fromTable;
        int size = size();
        for (int index = 0; index < size; index++) {
            fromTable = (FromTable) elementAt(index);
            FromTable newNode = (FromTable) fromTable.bindNonVTITables(dataDictionary, fromListParam);
            setElementAt(newNode, index);
        }
        for (int index = 0; index < size; index++) {
            fromTable = (FromTable) elementAt(index);
            FromTable newNode = (FromTable) fromTable.bindVTITables(fromListParam);
            setElementAt(newNode, index);
        }
    }

    public ResultColumnList expandAll(TableName allTableName) {
        ResultColumnList resultColumnList = null;
        ResultColumnList tempRCList;
        boolean matchfound = false;
        FromTable fromTable;
        int size = size();
        if (size != 1) {
            throw new RuntimeException("没有实现多表操作");
        }
        fromTable = (FromTable) elementAt(0);
        resultColumnList = fromTable.getAllResultColumns(allTableName);

        return resultColumnList;
    }

    public void bindExpressions(FromList fromListParam) {
        FromTable fromTable;
        int size = size();
        for (int index = 0; index < size; index++) {
            fromTable = (FromTable) elementAt(index);
            fromTable.bindExpressions(fromListParam);
        }
    }


    public ResultColumn bindColumnReference(ColumnReference columnReference) {
        FromTable fromTable = null;
        ResultColumn resultColumn;
        ResultColumn matchingRC = null;
        boolean columnNameMatch = false;
        int size = size();
        for (int index = 0; index < size; index++) {
            fromTable = (FromTable) elementAt(index);
            resultColumn = fromTable.getMatchingColumn(columnReference);
            if (resultColumn != null) {
                if (!columnNameMatch) {
                    matchingRC = resultColumn;
                    columnReference.setSource(resultColumn);
                    columnNameMatch = true;
                } else {
                    throw new RuntimeException("Column name ''{0}'' is in more than one table in the FROM list.");
                }
            }
        }
        return matchingRC;
    }

    public void pushPredicates(PredicateList predicateList) {
        int size = size();
        for (int index = 0; index < size; index++) {
            FromTable fromTable = (FromTable) elementAt(index);
            fromTable.pushExpressions(predicateList);
        }
    }

    /**
     * 优化节点的路径,主要是针对FromTable进行生成
     */
    public void initAccessPaths(Optimizer optimizer) {
        int size = size();
        for (int index = 0; index < size; index++) {
            FromTable ft = (FromTable) elementAt(index);
            ft.initAccessPaths(optimizer);
        }
    }

    public void setLevel(int level) {
        int size = size();
        for (int index = 0; index < size; index++) {
            FromTable fromTable = (FromTable) elementAt(index);
            fromTable.setLevel(level);
        }
    }


}
