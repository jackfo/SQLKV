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

    private final String name;

    private UUID oid;

    public SchemaDescriptor(DataDictionary dataDictionary, String name, String aid, UUID oid,boolean isSystem){
        super(dataDictionary);
        this.oid = oid;
        this.name = name;
    }

    @Override
    public UUID getUUID() {
        return oid;
    }

    /**
     * 在系统模式,可能的值是UCS_BASIC
     * 在用户模式,可能的值是UCS_BASIC和TERRITORY_BASED
     */
    private int collationType;

    public int getCollationType() {
        return collationType;
    }

    public String getSchemaName() {
        return name;
    }
}
