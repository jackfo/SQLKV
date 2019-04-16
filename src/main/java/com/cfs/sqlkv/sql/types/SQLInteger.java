package com.cfs.sqlkv.sql.types;


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
public class SQLInteger extends NumberDataType {

    private int value;
    private boolean isnull;

    public SQLInteger() {
        isnull = true;
    }

    public SQLInteger(int val) {
        value = val;
    }

    public SQLInteger(char val) {
        value = val;
    }

    public SQLInteger(Integer obj) {
        isnull = (obj == null);
        if (!isnull) {
            value = obj.intValue();
        }
    }

    @Override
    public boolean isNull() {
        return isnull;
    }

    @Override
    public void restoreToNull() {
        value = 0;
        isnull = true;
    }

    @Override
    public String getString() {
        if (isNull()) {
            return null;
        } else {
            return Integer.toString(value);
        }
    }

    @Override
    public DataValueDescriptor getNewNull() {
        return new SQLInteger();
    }

    @Override
    public int getInt() {
        return value;
    }

    @Override
    public Object getObject() {
        if (isNull())
            return null;
        else
            return value;
    }


    @Override
    public int typeCompare(DataValueDescriptor arg) {
        int thisValue = this.getInt();
        int otherValue = arg.getInt();
        if (thisValue == otherValue) {
            return 0;
        } else if (thisValue > otherValue) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.SQL_INTEGER_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readInt();
        isnull = false;
    }

    @Override
    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        SQLInteger nsi = new SQLInteger(value);
        nsi.isnull = isnull;
        return nsi;
    }
}
