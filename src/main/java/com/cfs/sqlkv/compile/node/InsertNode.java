package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.table.FromBaseTable;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.engine.execute.InsertConstantAction;

import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.temp.Temp;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-28 11:49
 */
public class InsertNode extends DMLStatementNode {

    private ResultColumnList targetColumnList;

    public InsertNode(
            QueryTreeNode targetName,
            ResultColumnList insertColumns,
            ResultSetNode queryExpression,
            Object matchingClause,
            Properties targetProperties,
            Object orderByList,
            ValueNode offset,
            ValueNode fetchFirst,
            boolean hasJDBClimitClause,
            ContextManager cm) {

        super(queryExpression, cm);
        if (targetName == null) {
            throw new RuntimeException("insertNode's targetName can't be null");
        }
        setTarget(targetName);

    }


    //构造插入节点的执行行为
    public ConstantAction makeConstantAction() {
        if (targetTableDescriptor != null) {
            //根据表描述获取标识
            long tableConglomId = targetTableDescriptor.getHeapConglomerateId();
            //todo:获取索引数
            //创建插入执行行为
            TransactionManager transactionManager = getLanguageConnectionContext().getTransactionCompile();
            Conglomerate conglomerate =transactionManager.findConglomerate(tableConglomId);
            return new InsertConstantAction(targetTableDescriptor, conglomerate, tableConglomId, null, autoincRowLocation);
        } else {
            throw new RuntimeException("tableDespcrition can't be null");
        }
    }

    public void bindStatement() {
        FromList fromList = new FromList(getContextManager());
        DataDictionary dataDictionary = getDataDictionary();
        super.bindResultSetsWithTables(dataDictionary);
        verifyTargetTable();
        getResultColumnList();
        resultSet.bindResultColumns(fromList);
        //TODO:设定结果集是否需要进行规范化
    }

    protected void getResultColumnList() {
        getResultColumnList(null);
    }

    @Override
    public FromBaseTable getResultColumnList(ResultColumnList inputRcl) {
        FromBaseTable fbt = new FromBaseTable(targetTableName,
                null, null, null, getContextManager());

        fbt.bindNonVTITables(getDataDictionary(), new FromList(getContextManager()));
        return fbt;
    }






    @Override
    public void optimizeStatement() {

    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        acb.pushGetResultSetFactoryExpression(mb);
        resultSet.generate(acb, mb);
        mb.push(targetTableName.getSchemaName());
        mb.push(targetTableName.getTableName());
        mb.callMethod(VMOpcode.INVOKEINTERFACE, null, "getInsertResultSet", ResultSet.class.getName(), 3);
    }
}
