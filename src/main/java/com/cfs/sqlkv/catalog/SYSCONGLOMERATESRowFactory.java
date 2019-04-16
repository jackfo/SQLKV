package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;
import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.sql.dictionary.IndexRowGenerator;
import com.cfs.sqlkv.sql.types.*;
import com.cfs.sqlkv.temp.Temp;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-05 15:44
 */
public class SYSCONGLOMERATESRowFactory extends CatalogRowFactory {

    private static final String TABLENAME_STRING = "SYSCONGLOMERATES";
    protected static final int SYSCONGLOMERATES_COLUMN_COUNT = 8;


    protected static final int SYSCONGLOMERATES_SCHEMAID = 1;
    protected static final int SYSCONGLOMERATES_TABLEID = 2;
    protected static final int SYSCONGLOMERATES_CONGLOMERATENUMBER = 3;
    protected static final int SYSCONGLOMERATES_CONGLOMERATENAME = 4;
    protected static final int SYSCONGLOMERATES_ISINDEX = 5;
    protected static final int SYSCONGLOMERATES_DESCRIPTOR = 6;
    protected static final int SYSCONGLOMERATES_ISCONSTRAINT = 7;
    protected static final int SYSCONGLOMERATES_CONGLOMERATEID = 8;


    private static final boolean[] uniqueness = {
            false,
            true,
            false
    };

    private static final int[][] indexColumnPositions = {
            {SYSCONGLOMERATES_CONGLOMERATEID},
            {SYSCONGLOMERATES_CONGLOMERATENAME, SYSCONGLOMERATES_SCHEMAID},
            {SYSCONGLOMERATES_TABLEID}
    };


    private static final String[] uuids = {
            "80000010-00d0-fd77-3ed8-000a0a0b1900"    // catalog UUID
            , "80000027-00d0-fd77-3ed8-000a0a0b1900"    // heap UUID
            , "80000012-00d0-fd77-3ed8-000a0a0b1900"    // SYSCONGLOMERATES_INDEX1
            , "80000014-00d0-fd77-3ed8-000a0a0b1900"    // SYSCONGLOMERATES_INDEX2
            , "80000016-00d0-fd77-3ed8-000a0a0b1900"    // SYSCONGLOMERATES_INDEX3
    };

    protected static final int		SYSCONGLOMERATES_INDEX1_ID = 0;
    protected static final int		SYSCONGLOMERATES_INDEX2_ID = 1;
    protected static final int		SYSCONGLOMERATES_INDEX3_ID = 2;

    public SYSCONGLOMERATESRowFactory(UUIDFactory uuidf, DataValueFactory dvf) {
        super(uuidf, dvf);
        initInfo(SYSCONGLOMERATES_COLUMN_COUNT, TABLENAME_STRING, indexColumnPositions, uniqueness, uuids);
    }

    public ExecRow makeRow(TupleDescriptor td, TupleDescriptor parent)   {
        //TODO:
        parent = Temp.schemaDescriptor;
        ExecRow row;
        String tableId = null;
        Long conglomNumber = null;
        String conglomName = null;
        String schemaID = null;
        String conglomUUIDString = null;
        IndexRowGenerator indexRowGenerator = null;
        Boolean supportsConstraint = null;
        Boolean supportsIndex = null;
        ConglomerateDescriptor conglomerate = (ConglomerateDescriptor) td;
        if (td != null) {
            if (parent != null) {
                SchemaDescriptor sd = (SchemaDescriptor) parent;
                schemaID = sd.getUUID().toString();
            } else {
                schemaID = conglomerate.getSchemaID().toString();
            }
            UUID tableUUID = conglomerate.getTableID();
            tableId = tableUUID.toString();
            conglomNumber = conglomerate.getConglomerateNumber();
            conglomName = conglomerate.getConglomerateName();
            conglomUUIDString = conglomerate.getUUID().toString();
        }
        row = new ValueRow(SYSCONGLOMERATES_COLUMN_COUNT);
        row.setColumn(1, new SQLChar(schemaID));
        row.setColumn(2, new SQLChar(tableId));
        row.setColumn(3, new SQLLongint(conglomNumber));
        row.setColumn(4, (conglomName == null) ? new SQLVarchar(tableId) : new SQLVarchar(conglomName));
        //设置当前表是否支持索引
        row.setColumn(5, new SQLBoolean(supportsIndex));
        UserType userType;
        if (indexRowGenerator == null) {
            userType = new UserType(null);
        } else {
            userType = new UserType(indexRowGenerator.getIndexDescriptor());
        }
        row.setColumn(6, userType);
        row.setColumn(7, new SQLBoolean(supportsConstraint));
        row.setColumn(8, new SQLChar(conglomUUIDString));
        return row;
    }

    public ExecRow makeEmptyRow()   {
        return makeRow(null, null);
    }

    @Override
    public TupleDescriptor buildDescriptor(ExecRow row, TupleDescriptor parentTuple, DataDictionary dataDictionary)   {
        DataDescriptorGenerator ddg = dataDictionary.getDataDescriptorGenerator();
        long conglomerateNumber;
        String name;
        boolean isConstraint;
        boolean isIndex;
        IndexRowGenerator indexRowGenerator;
        DataValueDescriptor col;
        ConglomerateDescriptor conglomerateDesc;
        String conglomUUIDString;
        UUID conglomUUID;
        String schemaUUIDString;
        UUID schemaUUID;
        String tableUUIDString;
        UUID tableUUID;

        col = row.getColumn(1);
        schemaUUIDString = col.getString();
        schemaUUID = getUUIDFactory().recreateUUID(schemaUUIDString);

        col = row.getColumn(2);
        tableUUIDString = col.getString();
        tableUUID = getUUIDFactory().recreateUUID(tableUUIDString);

        col = row.getColumn(3);
        conglomerateNumber = col.getLong();

        col = row.getColumn(4);
        name = col.getString();

        col = row.getColumn(5);
        isIndex = col.getBoolean();

        col = row.getColumn(6);
        indexRowGenerator = new IndexRowGenerator((IndexDescriptor) col.getObject());

        col = row.getColumn(7);
        isConstraint = col.getBoolean();

        col = row.getColumn(8);
        conglomUUIDString = col.getString();
        conglomUUID = getUUIDFactory().recreateUUID(conglomUUIDString);

        conglomerateDesc = ddg.newConglomerateDescriptor(conglomerateNumber, name, isIndex, indexRowGenerator, isConstraint, conglomUUID, tableUUID, schemaUUID);

        return conglomerateDesc;
    }
}
