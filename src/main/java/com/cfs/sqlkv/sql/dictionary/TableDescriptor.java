package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.column.ColumnDescriptorList;
import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 19:16
 */
public class TableDescriptor extends UniqueSQLObjectDescriptor {

    public static final char	ROW_LOCK_GRANULARITY = 'R';
    public static final char	TABLE_LOCK_GRANULARITY = 'T';
    public static final char	DEFAULT_LOCK_GRANULARITY = ROW_LOCK_GRANULARITY;

    public static final int BASE_TABLE_TYPE = 0;
    public static final int SYSTEM_TABLE_TYPE = 1;
    public static final int VIEW_TYPE = 2;
    public static final int GLOBAL_TEMPORARY_TABLE_TYPE = 3;
    public static final int SYNONYM_TYPE = 4;
    public static final int VTI_TYPE = 5;

    private char					lockGranularity;
    private boolean					onCommitDeleteRows; //true means on commit delete rows, false means on commit preserve rows of temporary table.
    private boolean					onRollbackDeleteRows; //true means on rollback delete rows. This is the only value supported.
    private boolean                 indexStatsUpToDate = true;
    private String                  indexStatsUpdateReason;
    SchemaDescriptor				schema;
    String							tableName;
    UUID                            oid;
    int								tableType;

    public TableDescriptor(DataDictionary dataDictionary, String				tableName,
                    SchemaDescriptor schema,
                    int					tableType,
                    char				lockGranularity) {
        this.schema = schema;
        this.tableName = tableName;
        this.tableType = tableType;
        this.lockGranularity = lockGranularity;
        this.columnDescriptorList = new ColumnDescriptorList();
    }

    public TableDescriptor(
                    DataDictionary		dataDictionary,
                    String				tableName,
                    SchemaDescriptor	schema,
                    int					tableType,
                    boolean				onCommitDeleteRows,
                    boolean				onRollbackDeleteRows) {
        this(dataDictionary, tableName, schema, tableType, '\0');
        this.onCommitDeleteRows = onCommitDeleteRows;
        this.onRollbackDeleteRows = onRollbackDeleteRows;
        this.columnDescriptorList = new ColumnDescriptorList();
    }

    @Override
    public UUID getUUID() {
        return oid;
    }

    /**根据表描述获取列描述*/
    ColumnDescriptorList columnDescriptorList;
    public ColumnDescriptorList getColumnDescriptorList() {
        return columnDescriptorList;
    }


}
