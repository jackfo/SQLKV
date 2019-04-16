package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.sql.types.SQLChar;
import com.cfs.sqlkv.sql.types.SQLVarchar;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description 系统模式工厂
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 21:16
 */
public class SYSSCHEMASRowFactory extends CatalogRowFactory {

    public static final int SYSSCHEMAS_SCHEMAID = 1;
    public static final int SYSSCHEMAS_SCHEMANAME = 2;
    public static final int SYSSCHEMAS_SCHEMAAID = 3;

    /**shcmeName和schemaId单独设置索引*/
    private static final int[][] indexColumnPositions = {
            {SYSSCHEMAS_SCHEMANAME},
            {SYSSCHEMAS_SCHEMAID}
    };

    private static final boolean[] uniqueness = null;

    private static final String[] uuids = {
            "80000022-00d0-fd77-3ed8-000a0a0b1900"    // catalog UUID
            , "8000002a-00d0-fd77-3ed8-000a0a0b1900"    // heap UUID
            , "80000024-00d0-fd77-3ed8-000a0a0b1900"    // SYSSCHEMAS_INDEX1
            , "80000026-00d0-fd77-3ed8-000a0a0b1900"    // SYSSCHEMAS_INDEX2
    };

    public SYSSCHEMASRowFactory(UUIDFactory uuidf, DataValueFactory dvf) {
        super(uuidf, dvf);
        initInfo(SYSSCHEMAS_COLUMN_COUNT, TABLENAME_STRING,
                indexColumnPositions, uniqueness, uuids);
    }

    private static final String TABLENAME_STRING = "SYSSCHEMAS";
    public static final int SYSSCHEMAS_COLUMN_COUNT = 3;



    protected static final int SYSSCHEMAS_INDEX1_ID = 0;
    protected static final int SYSSCHEMAS_INDEX2_ID = 1;


    public ExecRow makeRow(TupleDescriptor td, TupleDescriptor parent)   {
        ExecRow row;
        String name = null;
        UUID oid = null;
        String uuid = null;
        String aid = null;

        if (td != null) {
            SchemaDescriptor schemaDescriptor = (SchemaDescriptor) td;
            name = schemaDescriptor.getSchemaName();
            oid = schemaDescriptor.getUUID();
            uuid = oid.toString();
        }

        row = new ValueRow(SYSSCHEMAS_COLUMN_COUNT);
        row.setColumn(1, new SQLChar(uuid));
        row.setColumn(2, new SQLVarchar(name));
        row.setColumn(3, new SQLVarchar(aid));
        return row;
    }

    @Override
    public TupleDescriptor buildDescriptor(ExecRow row, TupleDescriptor parentTuple, DataDictionary dataDictionary)   {
        DataValueDescriptor col;
        SchemaDescriptor descriptor;
        String name;
        UUID id;
        String aid;
        String uuid;
        DataDescriptorGenerator ddg = dataDictionary.getDataDescriptorGenerator();
        col = row.getColumn(1);
        uuid = col.getString();
        id = getUUIDFactory().recreateUUID(uuid);

        col = row.getColumn(2);
        name = col.getString();

        col = row.getColumn(3);
        aid = col.getString();

        descriptor = ddg.newSchemaDescriptor(name, aid, id);

        return descriptor;
    }

}
