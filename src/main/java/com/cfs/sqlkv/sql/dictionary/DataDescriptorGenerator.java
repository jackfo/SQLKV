package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 12:23
 */
public class DataDescriptorGenerator {

    protected final DataDictionary dataDictionary;

    public	DataDescriptorGenerator( DataDictionary dataDictionary ) {
        this.dataDictionary = dataDictionary;
    }

    public TableDescriptor	newTableDescriptor(String tableName,SchemaDescriptor schema, int tableType, char lockGranularity) {
        return new TableDescriptor(dataDictionary, tableName, schema, tableType, lockGranularity);
    }

    public TableDescriptor newTableDescriptor(String tableName,SchemaDescriptor schema,int tableType,boolean onCommitDeleteRows,boolean onRollbackDeleteRows){
        return new TableDescriptor(dataDictionary, tableName, schema, tableType, onCommitDeleteRows, onRollbackDeleteRows);
    }

    public ConglomerateDescriptor newConglomerateDescriptor(
            long conglomerateId,
            String name,
            boolean indexable,
            IndexRowGenerator indexRowGenerator,
            boolean isConstraint,
            UUID uuid,
            UUID tableID,
            UUID schemaID){
        return new ConglomerateDescriptor(dataDictionary,
                conglomerateId,
                name,
                indexable,
                indexRowGenerator,
                isConstraint,
                uuid,
                tableID,
                schemaID);
    }

}
