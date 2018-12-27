package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:31
 */
public final class SchemaDescriptor extends UniqueTupleDescriptor{

    public static final String STD_SYSTEM_SCHEMA_NAME = "SYS";

    private UUID oid;

    public SchemaDescriptor(DataDictionary dataDictionary, String name, String aid, UUID oid,boolean isSystem){
        super(dataDictionary);
        this.oid = oid;
    }

    @Override
    public UUID getUUID() {
        return oid;
    }
}
