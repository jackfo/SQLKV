package com.cfs.sqlkv.type;


import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.service.io.ArrayInputStream;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:53
 */
public abstract class DataType implements DataValueDescriptor {

    public int getInt() {
        throw new RuntimeException(String.format("data value is not receivable as a int"));
    }

    public void readExternalFromArray(ArrayInputStream in) throws IOException, ClassNotFoundException {
        readExternal(in);
    }

    @Override
    public long getLong() {
        throw new RuntimeException("dataType can't convert to long");
    }

    @Override
    public boolean getBoolean() {
        throw new RuntimeException("dataType can't convert to Boolean");
    }

    public byte getByte() {
        throw new RuntimeException("dataType can't convert to byte");
    }

    public short getShort() {
        throw new RuntimeException("dataType can't convert to short");
    }

    public void setValue(long theValue) {
        runtimeException("setValue");
    }

    public final void setValue(DataValueDescriptor dvd) {
        if (dvd.isNull()) {
            setToNull();
            return;
        }
        setFrom(dvd);

    }

    public float getFloat() {
        throw new RuntimeException("dataType can't convert to float");
    }


    public double getDouble() {
        throw new RuntimeException("dataType can't convert to double");
    }

    public byte[] getBytes() {
        throw new RuntimeException("dataType can't convert to byte");
    }

    public void runtimeException(String message) {
        throw new RuntimeException(message);
    }

    protected void setFrom(DataValueDescriptor dvd) {
        runtimeException("setFrom");
    }


    public void setToNull() {
        restoreToNull();
    }

    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        throw new RuntimeException("需要在子类实现");
    }

    public int typePrecedence() {
        return -1;
    }

    public boolean compare(int op, DataValueDescriptor other, boolean orderedNulls, boolean unknownRV) {
        if (typePrecedence() < other.typePrecedence()) {
            return other.compare(flip(op), this, orderedNulls, unknownRV);
        }

        int result = compare(other);

        switch (op) {
            case ORDER_OP_LESSTHAN:
                return (result < 0);   // this <  other
            case ORDER_OP_EQUALS:
                return (result == 0);  // this == other
            case ORDER_OP_LESSOREQUALS:
                return (result <= 0);  // this <= other
            // flipped operators
            case ORDER_OP_GREATERTHAN:
                return (result > 0);   // this > other
            case ORDER_OP_GREATEROREQUALS:
                return (result >= 0);  // this >= other
            default:
                return false;
        }
    }

    protected static int flip(int operator) {
        switch (operator) {
            case ORDER_OP_LESSTHAN:
                // < -> >
                return ORDER_OP_GREATERTHAN;
            case ORDER_OP_LESSOREQUALS:
                // <= -> >=
                return ORDER_OP_GREATEROREQUALS;
            case ORDER_OP_EQUALS:
                // = -> =
                return ORDER_OP_EQUALS;
            default:


                return operator;
        }
    }

    public void normalize(DataTypeDescriptor desiredType, DataValueDescriptor source) {
        ((DataValueDescriptor) this).setValue(source);
    }

}
