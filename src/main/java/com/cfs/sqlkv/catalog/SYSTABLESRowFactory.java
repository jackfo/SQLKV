package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.sql.types.SQLChar;
import com.cfs.sqlkv.sql.types.SQLVarchar;
import com.cfs.sqlkv.temp.Temp;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-05 10:38
 */
public class SYSTABLESRowFactory extends CatalogRowFactory {

    protected static final int SYSTABLES_INDEX1_ID = 0;
    protected static final int SYSTABLES_INDEX2_ID = 1;

    public SYSTABLESRowFactory(UUIDFactory uuidFactory, DataValueFactory dvf) {
        super(uuidFactory, dvf);
        initInfo(SYSTABLES_COLUMN_COUNT, TABLENAME_STRING, indexColumnPositions, (boolean[]) null, uuids);
    }

    private static final String TABLENAME_STRING = "SYSTABLES";
    protected static final int SYSTABLES_COLUMN_COUNT = 5;

    protected static final int SYSTABLES_TABLEID = 1;
    protected static final int SYSTABLES_TABLENAME = 2;
    protected static final int SYSTABLES_TABLETYPE = 3;
    protected static final int SYSTABLES_SCHEMAID = 4;
    protected static final int SYSTABLES_LOCKGRANULARITY = 5;


    /**
     * 索引列有{tableName schemaId}和{tableId}两种组合
     */
    private static final int[][] indexColumnPositions = {{SYSTABLES_TABLENAME, SYSTABLES_SCHEMAID}, {SYSTABLES_TABLEID}};

    private static final String[] uuids = {
            "80000018-00d0-fd77-3ed8-000a0a0b1900"    // catalog UUID
            , "80000028-00d0-fd77-3ed8-000a0a0b1900"    // heap UUID
            , "8000001a-00d0-fd77-3ed8-000a0a0b1900"    // SYSTABLES_INDEX1
            , "8000001c-00d0-fd77-3ed8-000a0a0b1900"    // SYSTABLES_INDEX2
    };

    /**
     * 构建系统表对应的列
     */
    public ExecRow makeRow(TupleDescriptor td, TupleDescriptor parent)   {
        String tabSType = null;
        int tabIType;
        ExecRow row;
        UUID oid;
        String lockGranularity = null;
        String tableID = null;
        String schemaID = null;
        String tableName = null;

        if (td != null) {
            TableDescriptor descriptor = (TableDescriptor) td;
            SchemaDescriptor schema = Temp.schemaDescriptor;
            oid = getUUIDFactory().createUUID();
            descriptor.setUUID(oid);
            tableID = oid.toString();
            schemaID = schema.getUUID().toString();
            tableName = descriptor.getName();
            tabIType = descriptor.getTableType();
            switch (tabIType) {
                case TableDescriptor.BASE_TABLE_TYPE:
                    tabSType = "T";
                    break;
                case TableDescriptor.SYSTEM_TABLE_TYPE:
                    tabSType = "S";
                    break;
            }
            char[] lockGChar = new char[1];
            lockGChar[0] = descriptor.getLockGranularity();
            lockGranularity = new String(lockGChar);
        }
        row = new ValueRow(SYSTABLES_COLUMN_COUNT);
        row.setColumn(SYSTABLES_TABLEID, new SQLChar(tableID));
        row.setColumn(SYSTABLES_TABLENAME, new SQLVarchar(tableName));
        row.setColumn(SYSTABLES_TABLETYPE, new SQLChar(tabSType));
        row.setColumn(SYSTABLES_SCHEMAID, new SQLChar(schemaID));
        row.setColumn(SYSTABLES_LOCKGRANULARITY, new SQLChar(lockGranularity));
        return row;
    }

    @Override
    public TupleDescriptor buildDescriptor(ExecRow row, TupleDescriptor parentTuple, DataDictionary dataDictionary)   {
        DataDescriptorGenerator ddg = dataDictionary.getDataDescriptorGenerator();
        String tableUUIDString;
        String schemaUUIDString;
        int tableTypeEnum;
        String lockGranularity;
        String tableName, tableType;
        DataValueDescriptor col;
        UUID tableUUID;
        UUID schemaUUID;
        SchemaDescriptor schema;
        TableDescriptor tabDesc;

        //获取表标识
        col = row.getColumn(SYSTABLES_TABLEID);
        tableUUIDString = col.getString();
        tableUUID = getUUIDFactory().recreateUUID(tableUUIDString);

        col = row.getColumn(SYSTABLES_TABLENAME);
        tableName = col.getString();

        col = row.getColumn(SYSTABLES_TABLETYPE);
        tableType = col.getString();

        switch (tableType.charAt(0)) {
            case 'T':
                tableTypeEnum = TableDescriptor.BASE_TABLE_TYPE;
                break;
            case 'S':
                tableTypeEnum = TableDescriptor.SYSTEM_TABLE_TYPE;
                break;
            default:
                tableTypeEnum = -1;
        }

        col = row.getColumn(SYSTABLES_SCHEMAID);
        schemaUUIDString = col.getString();
        schema = Temp.schemaDescriptor;

        col = row.getColumn(SYSTABLES_LOCKGRANULARITY);
        lockGranularity = col.getString();
        tabDesc = ddg.newTableDescriptor(tableName, schema, tableTypeEnum, lockGranularity.charAt(0));
        tabDesc.setUUID(tableUUID);
        return tabDesc;
    }


}
