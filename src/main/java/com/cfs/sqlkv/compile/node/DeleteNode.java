package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.CurrentRowLocationNode;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.result.CursorResultSet;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.table.FromBaseTable;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.engine.execute.DeleteConstantAction;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-15 23:07
 */
public class DeleteNode extends DMLStatementNode {

    private static final String COLUMNNAME = "###RowLocationToDelete";

    private FromTable targetTable;

    private FormatableBitSet readColsBitSet;

    public DeleteNode(TableName targetTableName, ResultSetNode queryExpression, ContextManager cm) {
        super(queryExpression, cm);
        this.targetTableName = targetTableName;
    }


    @Override
    public String statementToString() {
        return "DELETE";
    }

    @Override
    public void bindStatement() {
        FromList fromList = new FromList(getContextManager());
        ResultColumn rowLocationColumn = null;
        CurrentRowLocationNode rowLocationNode;
        DataDictionary dataDictionary = getDataDictionary();
        super.bindTables(dataDictionary);
        SelectNode sel = (SelectNode) resultSet;
        targetTable = (FromTable) sel.fromList.elementAt(0);
        verifyTargetTable();
        resultColumnList = new ResultColumnList(getContextManager());
        /**
         * 创建位置列
         * */
        rowLocationNode = new CurrentRowLocationNode(getContextManager());
        rowLocationColumn = new ResultColumn(COLUMNNAME, rowLocationNode, getContextManager());
        rowLocationColumn.markGenerated();

        resultColumnList.addResultColumn(rowLocationColumn);

        /* Add the new result columns to the driving result set */
        ResultColumnList originalRCL = resultSet.getResultColumns();
        if (originalRCL != null) {
            originalRCL.appendResultColumns(resultColumnList);
            resultColumnList = originalRCL;
        }
        resultSet.setResultColumns(resultColumnList);

        super.bindExpressions();

        resultSet.getResultColumns().bindUntypedNullsToResultColumns(resultColumnList);

        rowLocationColumn.bindResultColumnToExpression();
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        acb.pushGetResultSetFactoryExpression(mb);
        acb.newRowLocationScanResultSetName();
        resultSet.generate(acb, mb);
        if (targetTableDescriptor != null) {
            acb.newFieldDeclaration(Modifier.PRIVATE, CursorResultSet.class.getName(), acb.getRowLocationScanResultSetName());
        }
        String resultSetArrayType = ResultSet.class.getName() + "[]";
        mb.callMethod(VMOpcode.INVOKEINTERFACE,  null, "getDeleteResultSet", ResultSet.class.getName(), 1);
    }

    @Override
    public ConstantAction makeConstantAction(){
        TransactionManager transactionManager = getLanguageConnectionContext().getTransactionCompile();
        if (targetTableDescriptor != null){
            long heapConglomId = targetTableDescriptor.getHeapConglomerateId();
            Heap heap = (Heap)transactionManager.findConglomerate(heapConglomId);
            return new DeleteConstantAction(heapConglomId,heap,null,targetTableDescriptor.getNumberOfColumns(),null);
        }
        throw new RuntimeException("makeConstantAction can't be null");
    }


}
