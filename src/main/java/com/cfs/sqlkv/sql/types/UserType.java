package com.cfs.sqlkv.sql.types;

import com.cfs.sqlkv.catalog.types.TypeId;

import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:52
 */
public class UserType extends DataType{

    private Object value;

    public UserType() {
    }

    public UserType(Object value) {
        this.value = value;
    }

    @Override
    public String getString() {
        if (!isNull()) {
            return value.toString();

        } else {
            return null;
        }
    }

    public boolean getBoolean() {
        if (!isNull()) {
            if (value instanceof Boolean) return ((Boolean) value).booleanValue();
        }
        return super.getBoolean();
    }

    public byte getByte() {
        if (!isNull()) {
            if (value instanceof Number) return ((Number) value).byteValue();
        }
        return super.getByte();
    }

    public short getShort() {
        if (!isNull()) {
            if (value instanceof Number) return ((Number) value).shortValue();
        }
        return super.getShort();
    }

    public int getInt() {
        if (!isNull()) {
            if (value instanceof Number) return ((Number) value).intValue();
        }
        return super.getInt();
    }

    public long getLong() {
        if (!isNull()) {
            if (value instanceof Number) return ((Number) value).longValue();
        }
        return super.getLong();
    }

    public float getFloat() {
        if (!isNull()) {
            if (value instanceof Number) return ((Number) value).floatValue();
        }
        return super.getFloat();
    }

    public double getDouble() {
        if (!isNull())
            if (value instanceof Number) return ((Number) value).doubleValue();
        return super.getDouble();
    }

    public byte[] getBytes() {
        if (!isNull()) {
            if (value instanceof byte[]) return ((byte[]) value);
        }
        return super.getBytes();
    }

    public void setObject(Object theValue) {
        setValue(theValue);
    }

    public Object getObject() {
        return value;
    }

    @Override
    public int compare(DataValueDescriptor other) {
        if (typePrecedence() < other.typePrecedence()) {
            return -(other.compare(this));
        }
        boolean thisNull, otherNull;
        thisNull = this.isNull();
        otherNull = other.isNull();
        if (thisNull || otherNull) {
            if (!thisNull)
                return -1;
            if (!otherNull)
                return 1;
            return 0;
        }
        int comparison = ((java.lang.Comparable<Object>) value).compareTo(other.getObject());
        if (comparison < 0)
            comparison = -1;
        else if (comparison > 0)
            comparison = 1;

        return comparison;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setFrom(DataValueDescriptor theValue) {
        setValue(theValue.getObject());
    }

    public int hashCode() {
        if (isNull()) {
            return 0;
        }
        return value.hashCode();
    }

    public int typePrecedence() {
        return TypeId.USER_PRECEDENCE;
    }

    public final boolean isNull() {
        return (value == null);
    }

    @Override
    public void restoreToNull() {
        value = null;
    }

    @Override
    public DataValueDescriptor getNewNull() {
        return new UserType();
    }

    public String toString() {
        if (isNull()) {
            return "NULL";
        } else {
            return value.toString();
        }
    }

    public boolean equals(DataValueDescriptor left, DataValueDescriptor right) {
        return left.compare(ORDER_OP_EQUALS, right, true, false);
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.SQL_USERTYPE_ID_V3;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readObject();
    }
}
