package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.column.ColumnDescriptorList;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptorList;
import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 09:39
 */
public class CreateTableConstantAction extends DDLConstantAction {

    /**
     * 表名
     */
    private String tableName;
    /**
     * 模式名
     */
    private String schemaName;

    /**
     * 列的相关信息
     */
    private ColumnInfo[] columnInfo;

    private char lockGranularity;
    private boolean onCommitDeleteRows;
    private boolean onRollbackDeleteRows;
    private int tableType;
    private CreateConstraintConstantAction[] constraintActions;
    private Properties properties;

    /**
     * @param schemaName           表存在的模式名
     * @param tableName            表名
     * @param tableType            表的类型
     * @param columnInfo           表的列信息
     * @param constraintActions    约束行为
     * @param properties           属性
     * @param lockGranularity      锁粒度
     * @param onCommitDeleteRows   如果为true，则在提交时删除行上的其他提交保留临时表的行
     * @param onRollbackDeleteRows 如果为true，则在回滚时，从临时修改的临时表中删除行
     */
    public CreateTableConstantAction(
            String schemaName,
            String tableName,
            int tableType,
            ColumnInfo[] columnInfo,
            CreateConstraintConstantAction[] constraintActions,
            Properties properties,
            char lockGranularity,
            boolean onCommitDeleteRows,
            boolean onRollbackDeleteRows) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.tableType = tableType;
        this.columnInfo = columnInfo;
        this.constraintActions = constraintActions;
        this.properties = properties;
        this.lockGranularity = lockGranularity;
        this.onCommitDeleteRows = onCommitDeleteRows;
        this.onRollbackDeleteRows = onRollbackDeleteRows;

    }

    @Override
    public void executeConstantAction(Activation activation)   {
        TableDescriptor td;
        UUID toid;
        SchemaDescriptor schemaDescriptor;
        ColumnDescriptor columnDescriptor;
        ExecRow template;
        /**
         * 通过激活获取语言连接上下文
         * */
        LanguageConnectionContext lcc = activation.getLanguageConnectionContext();
        //获取数据字典
        DataDictionary dd = lcc.getDataDictionary();
        //设置是创建表的行为
        activation.setForCreateTable();
        TransactionManager tc = lcc.getTransactionExecute();

        //获取行的模版
        template = RowUtil.getEmptyValueRow(columnInfo.length, lcc);

        //创建校对id数目
        int[] collation_ids = new int[columnInfo.length];
        for (int ix = 0; ix < columnInfo.length; ix++) {

            ColumnInfo col_info = columnInfo[ix];
            if (col_info.defaultValue != null) {
                //设置模版当前列的值
                template.setColumn(ix + 1, col_info.defaultValue);
            } else {
                template.setColumn(ix + 1, col_info.dataType.getNull());
            }
            //记录当前列的编码类型
            collation_ids[ix] = col_info.dataType.getCollationType();
        }

        long conglomId = tc.createConglomerate("table", template.getRowArray(), null, properties);
        /**
         * 如果不是全局临时表
         * */
        if (tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE) {
            dd.startWriting(lcc);
        }

        SchemaDescriptor sd;
        if (tableType == TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE) {
            sd = dd.getSchemaDescriptor(schemaName, tc, true);
        } else {
            sd = DDLConstantAction.getSchemaDescriptorForCreate(dd, activation, schemaName);
        }

        /**
         * 获取数据描述生成器
         * */
        DataDescriptorGenerator ddg = dd.getDataDescriptorGenerator();
        if (tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE) {
            td = ddg.newTableDescriptor(tableName, sd, tableType, lockGranularity);
            //在数据字典中添加相应的表描述
            dd.addDescriptor(td, sd, DataDictionary.SYSTABLES_CATALOG_NUM, false, tc);
        } else {
            td = ddg.newTableDescriptor(tableName, sd, tableType, onCommitDeleteRows, onRollbackDeleteRows);
            UUIDFactory uuidFactory = new UUIDFactory();
            td.setUUID(uuidFactory.createUUID());
        }
        //获取表对应的UUID
        toid = td.getUUID();

        //设置DDL表描述
        activation.setDDLTableDescriptor(td);

        int index = 1;
        //添加所有列的描述
        ColumnDescriptor[] cdlArray = new ColumnDescriptor[columnInfo.length];
        for (int ix = 0; ix < columnInfo.length; ix++) {
            UUID defaultUUID = columnInfo[ix].newDefaultUUID;
            if (columnInfo[ix].defaultInfo != null && defaultUUID == null) {
                //defaultUUID = dd.getUUIDFactory().createUUID();
            }

            /**
             * 将列的信息加到列的描述中去
             * */
            if (columnInfo[ix].autoincInc != 0) {
                columnDescriptor = new ColumnDescriptor(
                        columnInfo[ix].name,
                        index++,
                        columnInfo[ix].dataType,
                        columnInfo[ix].defaultValue,
                        columnInfo[ix].defaultInfo,
                        td,
                        defaultUUID,
                        columnInfo[ix].autoincStart,
                        columnInfo[ix].autoincInc,
                        columnInfo[ix].autoinc_create_or_modify_Start_Increment,
                        columnInfo[ix].autoincCycle);
            } else {
                columnDescriptor = new ColumnDescriptor(
                        columnInfo[ix].name,
                        index++,
                        columnInfo[ix].dataType,
                        columnInfo[ix].defaultValue,
                        columnInfo[ix].defaultInfo,
                        td,
                        defaultUUID,
                        columnInfo[ix].autoincStart,
                        columnInfo[ix].autoincInc,
                        columnInfo[ix].autoincCycle);
            }
            cdlArray[ix] = columnDescriptor;
        }
        //TODO:如果表的类型不是GLOBAL_TEMPORARY_TABLE_TYPE
        if (tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE) {
            dd.addDescriptorArray(cdlArray, td, DataDictionary.SYSCOLUMNS_CATALOG_NUM, false, tc);
        }

        ColumnDescriptorList cdl = td.getColumnDescriptorList();
        for (int i = 0; i < cdlArray.length; i++) {
            cdl.add(cdlArray[i]);
        }

        ConglomerateDescriptor cgd = ddg.newConglomerateDescriptor(conglomId, null, false, null, false, null, toid, sd.getUUID());

        if (tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE) {
            dd.addDescriptor(cgd, sd, DataDictionary.SYSCONGLOMERATES_CATALOG_NUM, false, tc);
        }

        ConglomerateDescriptorList conglomList = td.getConglomerateDescriptorList();
        conglomList.add(cgd);

    }
}
