package com.cfs.sqlkv.compile.table;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.column.ColumnDescriptorList;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.*;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.*;
import com.cfs.sqlkv.compile.predicate.*;
import com.cfs.sqlkv.compile.result.*;

import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptorList;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-04 20:17
 */
public class FromBaseTable extends FromTable {
    public TableName tableName;

    public static final int UPDATE = 1;
    public static final int DELETE = 2;

    public TableDescriptor tableDescriptor;

    public PredicateList restrictionList;
    PredicateList baseTableRestrictionList;

    public PredicateList storeRestrictionList;

    private String[] columnNames;

    public int updateOrDelete;
    public static final int UNSET = -1;
    public int bulkFetch = UNSET;

    public ConglomerateDescriptor baseConglomerateDescriptor;
    public ConglomerateDescriptor[] conglomDescs;
    private ResultColumnList templateColumns;

    private String rowLocationColumnName;

    public FromBaseTable(TableName tableName, String correlationName, ResultColumnList derivedRCL,
                         Properties tableProperties, ContextManager cm) {
        super(correlationName, cm);
        this.tableName = tableName;
        setResultColumns(derivedRCL);
        setOrigTableName(this.tableName);
        templateColumns = getResultColumns();
    }


    public FromBaseTable(TableName tableName, String correlationName, int updateOrDelete, ResultColumnList derivedRCL, ContextManager cm) {
        super(correlationName, cm);
        this.tableName = tableName;
        this.updateOrDelete = updateOrDelete;
        setResultColumns(derivedRCL);
        setOrigTableName(this.tableName);
        templateColumns = getResultColumns();
    }

    /**
     * 获取表的模式名
     * 获取表的描述
     */
    @Override
    public ResultSetNode bindNonVTITables(DataDictionary dataDictionary, FromList fromListParam) {

        tableName.bind();
        TableDescriptor tabDescr = bindTableDescriptor();
        restrictionList = new PredicateList(getContextManager());
        baseTableRestrictionList = new PredicateList(getContextManager());
        setResultColumns(genResultColList());
        templateColumns = getResultColumns();
        CompilerContext compilerContext = getCompilerContext();
        long heapConglomerateId = tabDescr.getHeapConglomerateId();
        baseConglomerateDescriptor = tabDescr.getConglomerateDescriptor(heapConglomerateId);
        columnNames = getResultColumns().getColumnNames();
        if (tableNumber == -1) {
            tableNumber = compilerContext.getNextTableNumber();
        }
        return this;
    }


    private TableDescriptor bindTableDescriptor() {
        String schemaName = tableName.getSchemaName();
        SchemaDescriptor sd = getSchemaDescriptor(schemaName);
        tableDescriptor = getTableDescriptor(tableName.getTableName(), sd);
        if (tableDescriptor == null) {
            throw new RuntimeException("tableDescriptor can't be null");
        }
        return tableDescriptor;
    }


    /**
     * 根据表描述来构建结果列
     */
    public ResultColumnList genResultColList() {
        ResultColumn resultColumn;
        ValueNode valueNode;
        TableName exposedName = getExposedTableName();
        ResultColumnList rcList = new ResultColumnList((getContextManager()));
        ColumnDescriptorList cdl = tableDescriptor.getColumnDescriptorList();
        int cdlSize = cdl.size();
        for (int index = 0; index < cdlSize; index++) {
            ColumnDescriptor colDesc = cdl.elementAt(index);
            colDesc.setTableDescriptor(tableDescriptor);
            valueNode = new BaseColumnNode(colDesc.getColumnName(), exposedName, colDesc.getType(), getContextManager());
            resultColumn = new ResultColumn(colDesc, valueNode, getContextManager());
            rcList.addResultColumn(resultColumn);
        }

        /**
         * 如果存在位置列,则进行对应的生成,并且添加到结果列中去
         * */
        if (rowLocationColumnName != null) {
            CurrentRowLocationNode rowLocationNode = new CurrentRowLocationNode(getContextManager());
            ResultColumn rowLocationColumn = new ResultColumn(rowLocationColumnName, rowLocationNode, getContextManager());
            rowLocationColumn.markGenerated();
            rowLocationNode.bindExpression(null);
            rowLocationColumn.bindResultColumnToExpression();
            rcList.addResultColumn(rowLocationColumn);
        }
        return rcList;
    }

    public void setRowLocationColumnName(String rowLocationColumnName) {
        this.rowLocationColumnName = rowLocationColumnName;
    }

    /**
     * FromBaseTable是不带约束条件的,故restriction和restrictionList传入控制
     */
    @Override
    public ResultSetNode preprocess(int numTables, FromList fromList) {
        return genBaseProjectNode();
    }

    public ResultSetNode genBaseProjectNode() {
        /**
         * 获取结果列并做一个拷贝重新注入句柄 目的是为了对获取的结果列处理不影响当前表的结果列
         * */
        ResultColumnList prRCList = getResultColumns();
        setResultColumns(getResultColumns().copyListAndObjects());
        //设置索引列的目的是为了找到对应的表
        getResultColumns().setIndexRow(baseConglomerateDescriptor.getConglomerateNumber(), true);
        //取代表达式,生成虚拟节点
        prRCList.genVirtualColumnNodes(this, getResultColumns(), false);
        prRCList.doProjection();
        return new BaseProjectNode(this, prRCList, null, null, getContextManager());
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        if (rowLocationColumnName != null) {
            getResultColumns().conglomerateId = tableDescriptor.getHeapConglomerateId();
        }
        generateResultSet(acb, mb);
        acb.pushGetResultSetFactoryExpression(mb);
        acb.pushThisAsActivation(mb);
        mb.push(getResultSetNumber());
        int resultRowTemplate = buildResultRowTemplate(acb, mb);
        mb.push(resultRowTemplate);
        long heapConglomerateId = tableDescriptor.getHeapConglomerateId();
        ConglomerateDescriptor conglomerateDescriptor = tableDescriptor.getConglomerateDescriptor(heapConglomerateId);
        TransactionManager transactionManager = getLanguageConnectionContext().getTransactionCompile();
        long conglomNumber = conglomerateDescriptor.getConglomerateNumber();
        Conglomerate conglomerate = transactionManager.findConglomerate(conglomNumber);
        mb.push(acb.addItem(conglomerate));
        mb.push(bulkFetch);
        restrictionList.generateQualifiers(acb, mb, this, true);
        mb.upCast(Qualifier.class.getName() + "[][]");

        if (bulkFetch != UNSET) {

            mb.callMethod(VMOpcode.INVOKEINTERFACE, null, "getBulkTableScanResultSet", NoPutResultSet.class.getName(), 6);
        } else {
            mb.callMethod(VMOpcode.INVOKEINTERFACE, null, "getTableScanResultSet", NoPutResultSet.class.getName(), 6);

        }


        if ((updateOrDelete == UPDATE) || (updateOrDelete == DELETE)) {
            mb.cast(CursorResultSet.class.getName());
            mb.putField(acb.getRowLocationScanResultSetName(), CursorResultSet.class.getName());
            mb.cast(NoPutResultSet.class.getName());
        }
    }

    @Override
    public void generateResultSet(ActivationClassBuilder acb, MethodBuilder mb) {
        assignResultSetNumber();
    }

    public int buildResultRowTemplate(ActivationClassBuilder acb, MethodBuilder mb) {
        int resultRowTemplate = acb.addItem(getResultColumns().buildRowTemplate(null, false));

        return resultRowTemplate;
    }

    @Override
    public ResultColumnList getAllResultColumns(TableName allTableName) {
        return getResultColumnsForList(allTableName, getResultColumns(),
                getOrigTableName());
    }

    @Override
    public ResultColumn getMatchingColumn(ColumnReference columnReference) {
        ResultColumn resultColumn = null;
        TableName exposedTableName;
        TableName columnsTableName = columnReference.getQualifiedTableName();
        exposedTableName = getExposedTableName();

        if (columnsTableName == null || columnsTableName.equals(exposedTableName)) {
            if (getResultColumns() == null) {
                throw new RuntimeException("Illegal reference to column ");
            }
            resultColumn = getResultColumns().getResultColumn(columnReference.getColumnName());

            if (resultColumn != null) {
                columnReference.setTableNumber(tableNumber);
                columnReference.setColumnNumber(resultColumn.getColumnPosition());
            }
        }

        return resultColumn;
    }

    public TableName getExposedTableName() {
        if (correlationName != null) {
            return makeTableName(null, correlationName);
        } else {
            return getOrigTableName();
        }
    }

    @Override
    public ResultSetNode changeAccessPath() {
        templateColumns = getResultColumns();
        setResultColumns(getResultColumns().compactColumns(false, false));
        storeRestrictionList = new PredicateList(getContextManager());
        restrictionList.setPredicatesAndProperties(storeRestrictionList);
        if (!forUpdate()) {
            bulkFetch = 16;
        }
        return this;
    }

    public boolean forUpdate() {
        return updateOrDelete != 0;
    }

    public TableName getTableNameField() {
        return tableName;
    }

    public ResultColumnList addColsToList(ResultColumnList inputRcl, FormatableBitSet colsWeWant) {
        ResultColumn resultColumn;
        TableName exposedName;
        exposedName = getExposedTableName();
        ResultColumnList newRcl = new ResultColumnList((getContextManager()));
        ColumnDescriptorList cdl = tableDescriptor.getColumnDescriptorList();
        int cdlSize = cdl.size();
        for (int index = 0; index < cdlSize; index++) {
            ColumnDescriptor cd = cdl.elementAt(index);
            int position = cd.getPosition();

            if (!colsWeWant.get(position)) {
                continue;
            }

            if ((resultColumn = inputRcl.getResultColumn(position)) == null) {
                ColumnReference cr = new ColumnReference(cd.getColumnName(), exposedName, getContextManager());
                resultColumn = new ResultColumn(cd, cr, getContextManager());
            }
            newRcl.addResultColumn(resultColumn);
        }

        return newRcl;
    }

    public void markUpdated(ResultColumnList updateColumns) {
        getResultColumns().markUpdated(updateColumns);
    }

    @Override
    public String getBaseTableName() {
        return tableName.getTableName();
    }

    @Override
    public boolean nextAccessPath(Optimizer optimizer, OptimizablePredicateList predList) {
        AccessPath ap = getCurrentAccessPath();
        ConglomerateDescriptor currentConglomerateDescriptor = ap.getConglomerateDescriptor();
        if (currentConglomerateDescriptor != null) {
            if (!super.nextAccessPath(optimizer, predList)) {
                currentConglomerateDescriptor = null;
            }
        } else {
            currentConglomerateDescriptor = getFirstConglom();
        }
        if (currentConglomerateDescriptor != null) {
            currentConglomerateDescriptor.setColumnNames(columnNames);
        }
        ap.setConglomerateDescriptor(currentConglomerateDescriptor);
        return currentConglomerateDescriptor != null;
    }


    private ConglomerateDescriptor getFirstConglom() {
        getConglomDescs();
        return conglomDescs[0];
    }

    private void getConglomDescs() {
        if (conglomDescs == null) {
            conglomDescs = tableDescriptor.getConglomerateDescriptors();
        }
    }

    @Override
    public boolean pushOptPredicate(OptimizablePredicate optimizablePredicate) {
        restrictionList.addPredicate((Predicate) optimizablePredicate);
        return true;
    }
}
