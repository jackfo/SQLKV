package com.cfs.sqlkv.sql.types;


import com.cfs.sqlkv.service.io.ArrayInputStream;
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
public class SQLBit extends DataType {
    @Override
    public void readExternalFromArray(ArrayInputStream ais) throws IOException, ClassNotFoundException {

    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void restoreToNull() {

    }

    @Override
    public String getString()   {
        return null;
    }

    @Override
    public DataValueDescriptor getNewNull() {
        return null;
    }

    @Override
    public int getInt()   {
        return 0;
    }

    @Override
    public Object getObject()   {
        return null;
    }

    @Override
    public int compare(DataValueDescriptor other)   {
        return 0;
    }

    @Override
    public int getTypeFormatId() {
        return 0;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
