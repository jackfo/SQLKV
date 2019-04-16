package com.cfs.sqlkv.sql.types;


import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:51
 */
public class SQLTinyint extends NumberDataType {

    private byte value;
    private boolean isnull;

    public SQLTinyint() {
        isnull = true;
    }

    public SQLTinyint(byte val) {
        value = val;
    }

    private SQLTinyint(byte val, boolean isnull) {
        value = val;
        this.isnull = isnull;
    }

    public SQLTinyint(Byte obj) {
        isnull = (obj == null);
        if (!isnull) {
            value = obj.byteValue();
        }
    }

    public int getInt() {
        return (int) value;
    }

    public byte getByte() {
        return value;
    }

    public short getShort() {
        return (short) value;
    }

    public long getLong() {
        return (long) value;
    }

    public float getFloat() {
        return (float) value;
    }

    public double getDouble() {
        return (double) value;
    }

    public boolean getBoolean() {
        return (value != 0);
    }

    public String getString() {
        return (isNull()) ?
                null :
                Byte.toString(value);
    }

    static final int TINYINT_LENGTH = 1;

    public int getLength() {
        return TINYINT_LENGTH;
    }

    public Object getObject() {
        return (isNull()) ?
                null :
                value;
    }


    public String getTypeName() {
        return TypeId.TINYINT_NAME;
    }

    public int getTypeFormatId() {
        return StoredFormatIds.SQL_TINYINT_ID;
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte(value);
    }

    public void readExternal(ObjectInput in) throws IOException {
        value = in.readByte();
        isnull = false;
    }

    public void restoreToNull() {
        value = 0;
        isnull = true;
    }

    public boolean isNull() {
        return isnull;
    }

    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        return new SQLTinyint(value, isnull);
    }

    public DataValueDescriptor getNewNull() {
        return new SQLTinyint();
    }

    public void setValueFromResultSet(ResultSet resultSet, int colNumber, boolean isNullable) throws SQLException {
        value = resultSet.getByte(colNumber);
        isnull = (isNullable && resultSet.wasNull());
    }

    public final void setInto(PreparedStatement ps, int position) throws SQLException {
        if (isNull()) {
            ps.setNull(position, java.sql.Types.TINYINT);
            return;
        }
        ps.setByte(position, value);
    }

    public final void setInto(ResultSet rs, int position) throws SQLException {
        rs.updateByte(position, value);
    }

    public void setValue(String theValue) {
        if (theValue == null) {
            value = 0;
            isnull = true;
        } else {
            try {
                value = Byte.valueOf(theValue.trim()).byteValue();
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(nfe.getMessage());
            }
            isnull = false;
        }
    }

    public void setValue(byte theValue) {
        value = theValue;
        isnull = false;
    }

    protected void setFrom(DataValueDescriptor theValue) {
        setValue(theValue.getByte());
    }

    public int typePrecedence() {
        return TypeId.TINYINT_PRECEDENCE;
    }

    public String toString() {
        if (isNull())
            return "NULL";
        else
            return Byte.toString(value);
    }

    public int hashCode() {
        return (int) value;
    }

    @Override
    public int typeCompare(DataValueDescriptor arg){
        int thisValue, otherValue;
        thisValue = this.getInt();
        otherValue = arg.getInt();
        if (thisValue == otherValue)
            return 0;
        else if (thisValue > otherValue)
            return 1;
        else
            return -1;
    }

}
