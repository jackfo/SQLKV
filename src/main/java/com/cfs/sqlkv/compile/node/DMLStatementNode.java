package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;

import com.cfs.sqlkv.compile.result.ResultColumnDescriptor;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.table.FromBaseTable;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.temp.Temp;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 20:32
 */
public abstract class DMLStatementNode extends StatementNode {

    public DMLStatementNode(ResultSetNode resultSet, ContextManager contextManager) {
        super(contextManager);
        this.resultSet = resultSet;
    }

    protected TableRowLocation[] autoincRowLocation;
    //获取目标表描述
    TableDescriptor targetTableDescriptor;

    public ResultColumnList resultColumnList;

    protected TableName targetTableName;

    //解析的结果集节点
    ResultSetNode resultSet;

    public int activationKind() {
        return StatementNode.NEED_ROW_ACTIVATION;
    }

    protected void bindTables(DataDictionary dataDictionary) {
        ContextManager cm = getContextManager();
        resultSet = resultSet.bindNonVTITables(dataDictionary, new FromList(cm));
        resultSet = resultSet.bindVTITables(new FromList(cm));
    }

    public QueryTreeNode bindResultSetsWithTables(DataDictionary dataDictionary) {
        bindTables(dataDictionary);
        return this;
    }

    public void setTarget(QueryTreeNode targetName) {
        if (targetName instanceof TableName) {
            this.targetTableName = (TableName) targetName;
        } else {
            throw new RuntimeException("don't need a vit");
        }
    }

    public QueryTreeNode bind(DataDictionary dataDictionary) {
        bindTables(dataDictionary);
        bindExpressions();
        return this;
    }

    protected void bindExpressions() {
        FromList fromList = new FromList(getContextManager());
        resultSet.bindExpressions(fromList);
    }


    @Override
    public void optimizeStatement() {
        int numberTables = getCompilerContext().getNumTables();
        resultSet = resultSet.preprocess(numberTables, null);
        //将Table中约束条件进行转化
        resultSet = resultSet.optimize(getDataDictionary(), null);
        resultSet = resultSet.modifyAccessPaths();
        if (this instanceof CursorNode) {
            resultSet = new ScrollInsensitiveResultSetNode(resultSet, resultSet.getResultColumns(), null, getContextManager());
        }
    }

    @Override
    public ResultDescription makeResultDescription() {
        ResultColumnDescriptor[] colDescs = resultSet.makeResultDescriptors();
        String statementType = statementToString();
        return new ResultDescription(colDescs, statementType);
    }

    public String statementToString() {
        return "DML MOD";
    }

    /**
     * 验证表是否存在,并且获取相关表描述
     */
    public void verifyTargetTable() {
        DataDictionary dataDictionary = getDataDictionary();
        if (targetTableName != null) {
            SchemaDescriptor schemaDescriptor = Temp.schemaDescriptor;
            //根据表名和模式描述获取表描述
            targetTableDescriptor = getTableDescriptor(targetTableName.getTableName(), schemaDescriptor);
            //表明没有索引列
            if (targetTableDescriptor == null) {

            }

        }
    }


    public FromBaseTable getResultColumnList(ResultColumnList inputRcl) {
        FromBaseTable fbt = new FromBaseTable(targetTableName,
                null, null, null, getContextManager());
        FromList fromList = new FromList(getContextManager());
        fbt.bindNonVTITables(getDataDictionary(), fromList);
        getResultColumnList(fbt, inputRcl);
        return fbt;
    }

    private void getResultColumnList(FromBaseTable fromBaseTable, ResultColumnList inputRcl) {
        if (inputRcl == null) {
            resultColumnList = fromBaseTable.getAllResultColumns(null);
            resultColumnList.bindResultColumnsByPosition(targetTableDescriptor);
        } else {
            resultColumnList = fromBaseTable.getResultColumnsForList(null, inputRcl, fromBaseTable.getTableNameField());
            resultColumnList.bindResultColumnsByName(targetTableDescriptor);
        }
    }
}
