package com.cfs.sqlkv.factory;


import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.sql.types.*;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.NumberDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 15:32
 */
public class DataValueFactoryImpl implements DataValueFactory {

    /**
     * 基于format id 和 collation type获取对应空数据描述
     */
    @Override
    public DataValueDescriptor getNull(int formatId) {
        DataValueDescriptor dataValueDescriptor = getNullDVDWithUCS_BASICcollation(formatId);
        return dataValueDescriptor;
    }

    public NumberDataValue getDataValue(Integer value, NumberDataValue previous) {
        if (previous == null) {
            return new SQLInteger(value);
        }
        previous.setValue(value);
        return previous;
    }

    @Override
    public NumberDataValue getDataValue(int value, NumberDataValue previous) {
        if (previous == null) {
            return new SQLInteger(value);
        }
        previous.setValue(value);
        return previous;
    }

    @Override
    public RefDataValue getDataValue(TableRowLocation value, RefDataValue previous) {
        if (previous == null){
            return new SQLRef(value);
        }
        previous.setValue(value);
        return previous;
    }

    /**
     * @param formatId 格式id
     */
    private static DataValueDescriptor getNullDVDWithUCS_BASICcollation(int formatId) {
        switch (formatId) {
            case StoredFormatIds.SQL_BIT_ID:
                return new SQLBit();
            case StoredFormatIds.SQL_BOOLEAN_ID:
                return new SQLBoolean();
            case StoredFormatIds.SQL_CHAR_ID:
                return new SQLChar();
            case StoredFormatIds.SQL_DATE_ID:
                return new SQLDate();
            case StoredFormatIds.SQL_DECIMAL_ID:
                return new SQLDecimal();
            case StoredFormatIds.SQL_DOUBLE_ID:
                return new SQLDouble();
            case StoredFormatIds.SQL_INTEGER_ID:
                return new SQLInteger();
            case StoredFormatIds.SQL_LONGINT_ID:
                return new SQLLongint();
            case StoredFormatIds.SQL_REAL_ID:
                return new SQLReal();
            case StoredFormatIds.SQL_REF_ID:
                return new SQLRef();
            case StoredFormatIds.SQL_SMALLINT_ID:
                return new SQLSmallint();
            case StoredFormatIds.SQL_TIME_ID:
                return new SQLTime();
            case StoredFormatIds.SQL_TIMESTAMP_ID:
                return new SQLTimestamp();
            case StoredFormatIds.SQL_TINYINT_ID:
                return new SQLTinyint();
            case StoredFormatIds.SQL_VARCHAR_ID:
                return new SQLVarchar();
            case StoredFormatIds.SQL_LONGVARCHAR_ID:
                return new SQLLongvarchar();
            case StoredFormatIds.SQL_VARBIT_ID:
                return new SQLVarbit();
            case StoredFormatIds.SQL_LONGVARBIT_ID:
                return new SQLLongVarbit();
            case StoredFormatIds.SQL_USERTYPE_ID_V3:
                return new UserType();
            case StoredFormatIds.SQL_BLOB_ID:
                return new SQLBlob();
            case StoredFormatIds.SQL_CLOB_ID:
                return new SQLClob();
            case StoredFormatIds.XML_ID:
                return new XML();
            case StoredFormatIds.ACCESS_HEAP_ROW_LOCATION_V1_ID:
                return new TableRowLocation();
            default:
                return null;
        }
    }
}
