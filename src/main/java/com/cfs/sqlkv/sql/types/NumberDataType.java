package com.cfs.sqlkv.sql.types;

import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.NumberDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 12:38
 */
public abstract class NumberDataType extends DataType implements NumberDataValue {

    public final int compare(DataValueDescriptor arg) {
        if (typePrecedence() < arg.typePrecedence()) {
            return -(arg.compare(this));
        }
        boolean thisNull, otherNull;

        thisNull = this.isNull();
        otherNull = arg.isNull();
        if (thisNull || otherNull) {
            if (!thisNull)        // otherNull must be true
                return -1;
            if (!otherNull)        // thisNull must be true
                return 1;
            return 0;
        }

        return typeCompare(arg);
    }

    public abstract int typeCompare(DataValueDescriptor arg);

    @Override
    public void setValue(Number theValue) {
        if (objectNull(theValue))
            return;
        setValue(theValue.intValue());
    }

    protected final boolean objectNull(Object o) {
        if (o == null) {
            restoreToNull();
            return true;
        }
        return false;
    }
}
