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
public class SQLLongint extends DataType {

    private boolean isnull;
    private long value;

    public SQLLongint() {
        isnull = true;
    }

    public SQLLongint(long val) {
        value = val;
    }

    public SQLLongint(Long obj) {
        isnull = obj == null;
        if (!isnull) {
            value = obj.longValue();
        }
    }


    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String getString()   {
        if (isNull()) {
            return null;
        } else {
            return Long.toString(value);
        }
    }

    @Override
    public void restoreToNull() {
        value = 0;
        isnull = true;
    }


    @Override
    public DataValueDescriptor getNewNull() {
        return new SQLLongint();
    }

    @Override
    public int getInt()   {
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new RuntimeException("data's value is greater than Integer.MAX_VALUE");
        }
        return (int) value;
    }

    @Override
    public Object getObject()   {
        if (isNull()) {
            return null;
        } else {
            return value;
        }
    }

    @Override
    public int compare(DataValueDescriptor other)   {
        return 0;
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.SQL_LONGINT_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readLong();
        isnull = false;
    }

    @Override
    public long getLong() {
        return value;
    }
}
