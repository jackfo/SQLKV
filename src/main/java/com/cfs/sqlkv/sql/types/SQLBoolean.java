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
 * @create 2019-01-08 10:50
 */
public class SQLBoolean extends DataType implements BooleanDataValue {

    public static final int BOOLEAN_LENGTH = 1;
    private static final SQLBoolean BOOLEAN_TRUE = new SQLBoolean(true);
    private static final SQLBoolean BOOLEAN_FALSE = new SQLBoolean(false);
    private boolean value;
    private boolean isnull;
    static final SQLBoolean UNKNOWN = new SQLBoolean();
    private boolean immutable;

    public SQLBoolean() {
        isnull = true;
    }

    public SQLBoolean(boolean val) {
        value = val;
    }

    public SQLBoolean(Boolean obj) {
        isnull = (obj == null);
        if (!isnull) {
            value = obj.booleanValue();
        }

    }

    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public BooleanDataValue and(BooleanDataValue otherValue) {
        if (this.equals(false) || otherValue.equals(false)) {
            return BOOLEAN_FALSE;
        } else {
            return truthValue(this, otherValue, this.getBoolean() && otherValue.getBoolean());
        }
    }

    @Override
    public BooleanDataValue or(BooleanDataValue otherValue) {
        if (this.equals(true) || otherValue.equals(true)) {
            return BOOLEAN_TRUE;
        } else {
            return truthValue(this, otherValue, this.getBoolean() || otherValue.getBoolean());
        }
    }

    public static SQLBoolean truthValue(
            DataValueDescriptor leftOperand,
            DataValueDescriptor rightOperand,
            boolean truth) {
        if (leftOperand.isNull() || rightOperand.isNull()) {
            return UNKNOWN;
        }
        if (truth == true) {
            return BOOLEAN_TRUE;
        } else {
            return BOOLEAN_FALSE;
        }
    }

    @Override
    public BooleanDataValue is(BooleanDataValue otherValue) {
        if (this.equals(true) && otherValue.equals(true)) {
            return BOOLEAN_TRUE;
        }

        if (this.equals(false) && otherValue.equals(false)) {
            return BOOLEAN_TRUE;
        }

        if (this.isNull() && otherValue.isNull()) {
            return BOOLEAN_TRUE;
        }

        return BOOLEAN_FALSE;
    }

    @Override
    public boolean equals(boolean value) {
        if (isNull()) {
            return false;
        } else {
            return this.value == value;
        }
    }

    @Override
    public boolean isNull() {
        return isnull;
    }

    private static int makeInt(boolean b) {
        return (b ? 1 : 0);
    }

    public byte getByte() {
        return (byte) makeInt(value);
    }

    public short getShort() {
        return (short) makeInt(value);
    }

    public int getInt() {
        return makeInt(value);
    }


    public float getFloat() {
        return (float) makeInt(value);
    }

    public double getDouble() {
        return (double) makeInt(value);
    }

    public int typeToBigDecimal() {
        return java.sql.Types.BIGINT;
    }

    public long getLong() {
        return (long) makeInt(value);
    }

    @Override
    public String getString() {
        if (isNull())
            return null;
        else if (value == true)
            return "true";
        else
            return "false";
    }

    @Override
    public Object getObject() {
        if (isNull())
            return null;
        else
            return value;
    }


    public int getLength() {
        return BOOLEAN_LENGTH;
    }

    public String getTypeName() {
        return TypeId.BOOLEAN_NAME;
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.SQL_BOOLEAN_ID;
    }

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
        boolean thisValue;
        boolean otherValue = false;
        thisValue = this.getBoolean();
        otherValue = other.getBoolean();
        if (thisValue == otherValue)
            return 0;
        else if (thisValue && !otherValue)
            return 1;
        else
            return -1;
    }

    public boolean compare(int op, DataValueDescriptor other, boolean orderedNulls, boolean unknownRV) {
        if (!orderedNulls) {
            if (this.isNull() || other.isNull())
                return unknownRV;
        }
        return super.compare(op, other, orderedNulls, unknownRV);
    }

    @Override
    public DataValueDescriptor getNewNull() {
        return new SQLBoolean();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeBoolean(value);
    }


    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readBoolean();
        isnull = false;
    }

    @Override
    public void restoreToNull() {

        value = false;
        isnull = true;
    }
}
