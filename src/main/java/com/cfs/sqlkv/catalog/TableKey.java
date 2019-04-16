package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-31 23:12
 */
public class TableKey {
    private final String tableName;
    private final UUID schemaId;
    public TableKey(UUID schemaUUID, String tableName){
        this.tableName = tableName;
        this.schemaId = schemaUUID;
    }

    public String getTableName() {
        return tableName;
    }

    public UUID getSchemaId() {
        return schemaId;
    }
    public boolean equals(Object otherTableKey) {
        if (otherTableKey instanceof TableKey) {
            TableKey otk = (TableKey) otherTableKey;
            if (tableName.equals(otk.tableName) && schemaId.equals(otk.schemaId)){
                return true;
            }
        }
        return false;
    }


    public int hashCode() {
        return tableName.hashCode();
    }
}
