package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.catalog.types.DefaultInfo;
import com.cfs.sqlkv.column.ColumnDescriptorList;
import com.cfs.sqlkv.common.UUID;
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
import com.cfs.sqlkv.engine.execute.UpdateConstantAction;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.temp.Temp;
import com.cfs.sqlkv.util.ByteArray;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-12 11:49
 */
public class UpdateNode extends DMLStatementNode {

    public int[] changedColumnIds;
    /**
     * 修改的表
     */
    public FromTable targetTable;

    protected FormatableBitSet readColsBitSet;
    protected boolean positionedUpdate;
    public static final String COLUMNNAME = "###RowLocationToUpdate";

    public UpdateNode(TableName targetTableName, ResultSetNode resultSet, ContextManager cm) {
        super(resultSet, cm);
        this.targetTableName = targetTableName;
    }

    public ResultSetNode getResultSetNode() {
        return resultSet;
    }

    @Override
    public QueryTreeNode bind(DataDictionary dataDictionary) {

        throw new RuntimeException("");
    }

    @Override
    public void bindStatement() {
        FromList fromList = new FromList(getContextManager());
        ResultColumnList afterColumns;
        DataDictionary dataDictionary = getDataDictionary();
        bindTables(dataDictionary);

        //获取FromList中第一个表作为当前更新的targetTable
        SelectNode sel;
        sel = (SelectNode) resultSet;
        //获取修改表
        targetTable = (FromTable) sel.fromList.elementAt(0);
        verifyTargetTable();
        /**将结果集对应的标记为更新*/
        resultSet.getResultColumns().markUpdated();
        //绑定结果列
        resultSet.bindResultColumns(fromList, targetTableDescriptor, resultSet.getResultColumns());
        //标记需要更新的列
        if (targetTable instanceof FromBaseTable) {
            ((FromBaseTable) targetTable).markUpdated(resultSet.getResultColumns());
        }
        changedColumnIds = getChangedColumnIds(resultSet.getResultColumns());
        readColsBitSet = new FormatableBitSet();
        FromBaseTable fbt = getResultColumnList(resultSet.getResultColumns());
        //获取结果集如Select的结果列,对应列会带相应的表达式
        afterColumns = resultSet.getResultColumns().copyListAndObjects();
        readColsBitSet = getReadMap(targetTableDescriptor, afterColumns);
        afterColumns = fbt.addColsToList(afterColumns, readColsBitSet);
        int i = 1;
        int size = targetTableDescriptor.getMaxColumnID();
        for (; i <= size; i++) {
            if (!readColsBitSet.get(i)) {
                break;
            }
        }
        if (i > size) {
            readColsBitSet = null;
        }


        ValueNode rowLocationNode;
        resultColumnList.appendResultColumns(afterColumns);
        /*生成位置列*/
        rowLocationNode = new CurrentRowLocationNode(getContextManager());
        ResultColumn rowLocationColumn = new ResultColumn(COLUMNNAME, rowLocationNode, getContextManager());
        rowLocationColumn.markGenerated();
        /*追加位置列的结果集 */
        resultColumnList.addResultColumn(rowLocationColumn);
        checkTableNameAndScrubResultColumns(resultColumnList);
        resultSet.setResultColumns(resultColumnList);
        super.bindExpressions();

        resultSet.getResultColumns().bindUntypedNullsToResultColumns(resultColumnList);

        rowLocationColumn.bindResultColumnToExpression();
        //FromBaseTable fbt = getResultColumnList(resultSet.getResultColumns());
    }


    private void checkTableNameAndScrubResultColumns(ResultColumnList rcl) {
        for (ResultColumn column : rcl) {
            boolean foundMatchingTable = false;
            if (column.getTableName() != null) {
                for (ResultSetNode rsn : ((SelectNode) resultSet).fromList) {
                    FromTable fromTable = (FromTable) rsn;
                    final String tableName;
                    tableName = fromTable.getBaseTableName();
                    if (column.getTableName().equals(tableName)) {
                        foundMatchingTable = true;
                        break;
                    }
                }
                if (!foundMatchingTable) {
                    throw new RuntimeException("column not found");
                }
            }
            column.clearTableName();
        }
    }

    static FormatableBitSet getUpdateReadMap(TableDescriptor baseTable, ResultColumnList updateColumnList) {
        int columnCount = baseTable.getMaxColumnID();
        FormatableBitSet columnMap = new FormatableBitSet(columnCount + 1);
        int[] changedColumnIds = updateColumnList.sortMe();
        for (int ix = 0; ix < changedColumnIds.length; ix++) {
            columnMap.set(changedColumnIds[ix]);
        }
        return columnMap;
    }


    public FormatableBitSet getReadMap(TableDescriptor baseTable, ResultColumnList updateColumnList) {
        FormatableBitSet columnMap = getUpdateReadMap(baseTable, updateColumnList);
        return columnMap;
    }

    private int[] getChangedColumnIds(ResultColumnList rcl) {
        if (rcl == null) {
            return null;
        } else {
            return rcl.sortMe();
        }
    }

    /**
     * 获取修改列的列名
     */
    private ArrayList<String> getExplicitlySetColumns() {
        ArrayList<String> result = new ArrayList();
        ResultColumnList rcl = resultSet.getResultColumns();
        for (int i = 0; i < rcl.size(); i++) {
            result.add(rcl.elementAt(i).getName());
        }
        return result;
    }

    private void addGeneratedColumns(ResultSetNode updateSet) {
        ResultColumnList updateColumnList = updateSet.getResultColumns();
        HashSet<String> updatedColumns = new HashSet();
        for (ResultColumn rc : updateColumnList) {
            updatedColumns.add(rc.getName());
        }
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        acb.newFieldDeclaration(Modifier.PRIVATE, CursorResultSet.class.getName(), acb.newRowLocationScanResultSetName());
        acb.pushGetResultSetFactoryExpression(mb);
        resultSet.generate(acb, mb);
        mb.pushNull(GeneratedMethod.class.getName());
        mb.pushNull(GeneratedMethod.class.getName());
        mb.callMethod(VMOpcode.INVOKEINTERFACE, null, "getUpdateResultSet", ResultSet.class.getName(), 3);
    }

    @Override
    public ConstantAction makeConstantAction() {
        long heapConglomId = targetTableDescriptor.getHeapConglomerateId();
        TransactionManager transactionManager = getLanguageConnectionContext().getTransactionCompile();
        Heap heap = (Heap) transactionManager.findConglomerate(heapConglomId);
        return new UpdateConstantAction(targetTableDescriptor, heap, changedColumnIds, targetTableDescriptor.getNumberOfColumns());
    }


    @Override
    public String toString() {
        return "UpdateNode{" +
                "targetTableDescriptor=" + targetTableDescriptor +
                ", targetTableName=" + targetTableName +
                '}';
    }
}
