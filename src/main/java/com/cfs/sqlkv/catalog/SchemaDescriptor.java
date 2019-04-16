package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:31
 */
public final class SchemaDescriptor extends UniqueTupleDescriptor {

    public static final String STD_SYSTEM_SCHEMA_NAME = "SYS";

    public static final String STD_SYSTEM_DIAG_SCHEMA_NAME = "SYSCS_DIAG";
    private final String name;

    private UUID oid;

    public static final String SYSCAT_SCHEMA_UUID = "c013800d-00fb-2641-07ec-000000134f30";
    public static final String SYSFUN_SCHEMA_UUID = "c013800d-00fb-2642-07ec-000000134f30";
    public static final String SYSPROC_SCHEMA_UUID = "c013800d-00fb-2643-07ec-000000134f30";
    public static final String SYSSTAT_SCHEMA_UUID = "c013800d-00fb-2644-07ec-000000134f30";
    public static final String SYSCS_DIAG_SCHEMA_UUID = "c013800d-00fb-2646-07ec-000000134f30";
    public static final String SYSCS_UTIL_SCHEMA_UUID = "c013800d-00fb-2649-07ec-000000134f30";
    public static final String NULLID_SCHEMA_UUID = "c013800d-00fb-2647-07ec-000000134f30";
    public static final String SQLJ_SCHEMA_UUID = "c013800d-00fb-2648-07ec-000000134f30";
    public static final String SYSTEM_SCHEMA_UUID = "8000000d-00d0-fd77-3ed8-000a0a0b1900";
    public static final String SYSIBM_SCHEMA_UUID = "c013800d-00f8-5b53-28a9-00000019ed88";
    public static final String DEFAULT_SCHEMA_UUID = "80000000-00d2-b38f-4cda-000a0a412c00";

    public void setUUID(UUID oid) {
        this.oid = oid;
    }

    public SchemaDescriptor(DataDictionary dataDictionary, String name, String aid, UUID oid, boolean isSystem) {
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
