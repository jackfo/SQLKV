package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.sql.types.*;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-05 10:38
 */
public class SYSCOLUMNSRowFactory extends CatalogRowFactory {

    protected static final int SYSCOLUMNS_INDEX1_ID = 0;
    protected static final int SYSCOLUMNS_INDEX2_ID = 1;
    public static final String TABLENAME_STRING = "SYSCOLUMNS";
    protected static final int SYSCOLUMNS_COLUMN_COUNT = 10;
    private static final boolean[] uniqueness = {true, false};

    protected static final int SYSCOLUMNS_REFERENCEID = 1;
    protected static final int SYSCOLUMNS_COLUMNNAME = 2;
    protected static final int SYSCOLUMNS_COLUMNNUMBER = 3;
    protected static final int SYSCOLUMNS_COLUMNDATATYPE = 4;
    protected static final int SYSCOLUMNS_COLUMNDEFAULT = 5;
    protected static final int SYSCOLUMNS_COLUMNDEFAULTID = 6;
    protected static final int SYSCOLUMNS_AUTOINCREMENTVALUE = 7;
    protected static final int SYSCOLUMNS_AUTOINCREMENTSTART = 8;
    protected static final int SYSCOLUMNS_AUTOINCREMENTINC = 9;
    protected static final int SYSCOLUMNS_AUTOINCREMENTINCCYCLE = 10;

    private static final String[] uuids = {
            "8000001e-00d0-fd77-3ed8-000a0a0b1900"    // catalog UUID
            , "80000029-00d0-fd77-3ed8-000a0a0b1900"    // heap UUID
            , "80000020-00d0-fd77-3ed8-000a0a0b1900"    // SYSCOLUMNS_INDEX1 UUID
            , "6839c016-00d9-2829-dfcd-000a0a411400"    // SYSCOLUMNS_INDEX2 UUID
    };

    private static final int[][] indexColumnPositions = {
            {SYSCOLUMNS_REFERENCEID, SYSCOLUMNS_COLUMNNAME},
            {SYSCOLUMNS_COLUMNDEFAULTID}
    };
    private final DataDictionary dataDictionary;





    public SYSCOLUMNSRowFactory(DataDictionary dd, UUIDFactory uuidf, DataValueFactory dvf) {
        super(uuidf, dvf);
        this.dataDictionary = dd;
        initInfo(SYSCOLUMNS_COLUMN_COUNT, TABLENAME_STRING, indexColumnPositions, uniqueness, uuids);
    }

    @Override
    public ExecRow makeRow(TupleDescriptor td, TupleDescriptor parent)   {
        return makeRow(td, getHeapColumnCount());
    }

    private ExecRow makeRow(TupleDescriptor td, int columnCount)   {
        ExecRow row;
        String colName = null;
        String defaultID = null;
        String tabID = null;
        Integer colID = null;
        TypeDescriptor typeDesc = null;
        Object defaultSerializable = null;
        long autoincStart = 0;
        long autoincInc = 0;
        long autoincValue = 0;
        boolean autoincCycle = false;
        long autoinc_create_or_modify_Start_Increment = -1;
        if (td != null) {
            //获取相应的列描述
            ColumnDescriptor column = (ColumnDescriptor) td;
            typeDesc = column.getType().getCatalogType();
            tabID = column.getReferencingUUID().toString();
            colName = column.getColumnName();
            colID = column.getPosition();
            autoincStart = column.getAutoincStart();
            autoincInc = column.getAutoincInc();
            autoincValue = column.getAutoincValue();
            autoinc_create_or_modify_Start_Increment = column.getAutoinc_create_or_modify_Start_Increment();
            autoincCycle = column.getAutoincCycle();
            if (column.getDefaultInfo() != null) {
                defaultSerializable = column.getDefaultInfo();
            } else {
                defaultSerializable = column.getDefaultValue();
            }
            if (column.getDefaultUUID() != null) {
                defaultID = column.getDefaultUUID().toString();
            }
        }


        row = new ValueRow(columnCount);

        row.setColumn(SYSCOLUMNS_REFERENCEID, new SQLChar(tabID));
        row.setColumn(SYSCOLUMNS_COLUMNNAME, new SQLVarchar(colName));
        row.setColumn(SYSCOLUMNS_COLUMNNUMBER, new SQLInteger(colID));
        row.setColumn(SYSCOLUMNS_COLUMNDATATYPE, new UserType(typeDesc));
        row.setColumn(SYSCOLUMNS_COLUMNDEFAULT, new UserType(defaultSerializable));
        row.setColumn(SYSCOLUMNS_COLUMNDEFAULTID, new SQLChar(defaultID));
        row.setColumn(SYSCOLUMNS_AUTOINCREMENTVALUE, new SQLLongint(autoincValue));
        row.setColumn(SYSCOLUMNS_AUTOINCREMENTSTART, new SQLLongint(autoincStart));
        row.setColumn(SYSCOLUMNS_AUTOINCREMENTINC, new SQLLongint(autoincInc));
        row.setColumn(SYSCOLUMNS_AUTOINCREMENTINCCYCLE, new SQLBoolean(autoincCycle));
        return row;

    }

    @Override
    public int getHeapColumnCount()   {
        int heapCols = super.getHeapColumnCount();
        return heapCols;
    }

    @Override
    public TupleDescriptor buildDescriptor(ExecRow row, TupleDescriptor parentTupleDescriptor, DataDictionary dataDictionary)   {
        int columnNumber;
        String columnName;
        String defaultID;
        ColumnDescriptor colDesc;
        DataValueDescriptor defaultValue = null;
        UUID defaultUUID = null;
        UUID uuid = null;
        UUIDFactory uuidFactory = getUUIDFactory();
        long autoincStart, autoincInc, autoincValue;
        boolean autoincCycle = false;

        DataDescriptorGenerator ddg = dataDictionary.getDataDescriptorGenerator();

        if (parentTupleDescriptor != null) {
            uuid = ((UniqueTupleDescriptor) parentTupleDescriptor).getUUID();
        } else {
            uuid = uuidFactory.recreateUUID(row.getColumn(SYSCOLUMNS_REFERENCEID).getString());
        }
        Object object = row.getColumn(SYSCOLUMNS_COLUMNDEFAULT).getObject();
        if (object instanceof DataValueDescriptor) {
            defaultValue = (DataValueDescriptor) object;
        }

        defaultID = row.getColumn(SYSCOLUMNS_COLUMNDEFAULTID).getString();
        if (defaultID != null) {
            defaultUUID = uuidFactory.recreateUUID(defaultID);
        }

        columnName = row.getColumn(SYSCOLUMNS_COLUMNNAME).getString();
        columnNumber = row.getColumn(SYSCOLUMNS_COLUMNNUMBER).getInt();
        TypeDescriptor catalogType = (TypeDescriptor) row.getColumn(SYSCOLUMNS_COLUMNDATATYPE).getObject();

        DataTypeDescriptor dataTypeServices = DataTypeDescriptor.getType(catalogType);

        autoincValue = row.getColumn(SYSCOLUMNS_AUTOINCREMENTVALUE).getLong();
        autoincStart = row.getColumn(SYSCOLUMNS_AUTOINCREMENTSTART).getLong();
        autoincInc = row.getColumn(SYSCOLUMNS_AUTOINCREMENTINC).getLong();
        if (row.nColumns() >= 10) {
            DataValueDescriptor col = row.getColumn(SYSCOLUMNS_AUTOINCREMENTINCCYCLE);
            autoincCycle = col.getBoolean();
        }
        DataValueDescriptor col = row.getColumn(SYSCOLUMNS_AUTOINCREMENTSTART);
        autoincStart = col.getLong();

        col = row.getColumn(SYSCOLUMNS_AUTOINCREMENTINC);
        autoincInc = col.getLong();

        colDesc = new ColumnDescriptor(columnName, columnNumber,
                dataTypeServices, defaultValue, null, uuid,
                defaultUUID, autoincStart, autoincInc,
                autoincValue, autoincCycle);

        return colDesc;
    }
}
