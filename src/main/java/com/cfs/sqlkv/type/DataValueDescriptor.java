package com.cfs.sqlkv.type;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.Orderable;

import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.Storable;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 21:00
 */
public interface DataValueDescriptor extends Storable, Orderable {

    /**
     * 从流中获取数据描述
     */
    public void readExternalFromArray(ArrayInputStream ais) throws IOException, ClassNotFoundException;

    public boolean isNull();

    public DataValueDescriptor getNewNull();

    public Object getObject();

    public int compare(DataValueDescriptor other);

    public void setValue(long theValue)  ;

    public void setValue(DataValueDescriptor theValue)  ;

    public abstract DataValueDescriptor cloneValue(boolean forceMaterialization);

    int typePrecedence();

    boolean compare(int op, DataValueDescriptor other, boolean orderedNulls, boolean unknownRV);

    public boolean getBoolean();

    public String getString();

    public int getInt();

    public long getLong();

    public double getDouble();

    public float getFloat();

    public short getShort();

    public byte getByte();

    public void setToNull();
    public void normalize(DataTypeDescriptor dtd, DataValueDescriptor source);

}
