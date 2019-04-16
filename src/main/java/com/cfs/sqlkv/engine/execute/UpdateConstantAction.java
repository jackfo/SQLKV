package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.access.Heap;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-12 12:53
 */
public class UpdateConstantAction extends WriteCursorConstantAction {

    public int numColumns;

    private String schemaName;
    private String tableName;
    private String columnNames[];

    boolean singleRowSource;

    public int[] changedColumnIds;

    public UpdateConstantAction() {
        super();
    }

    public UpdateConstantAction(TableDescriptor targetTableDesc, Heap heap, int[] changedColumnIds, int numColumns) {
        super(targetTableDesc.getHeapConglomerateId(),heap,null);
        this.numColumns = numColumns;
        this.changedColumnIds = changedColumnIds;
        this.tableName = targetTableDesc.getName();
        this.columnNames = targetTableDesc.getColumnNamesArray();
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.UPDATE_CONSTANT_ACTION_V01_ID;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName(int i) {
        return columnNames[i];
    }

    public String[] getColumnNames() {
        return columnNames;
    }
}
