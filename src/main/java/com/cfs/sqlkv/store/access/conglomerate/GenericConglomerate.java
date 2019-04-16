package com.cfs.sqlkv.store.access.conglomerate;


import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 13:43
 */
public abstract class GenericConglomerate extends DataType implements Conglomerate {

    public DataValueDescriptor getNewNull() {
        return null;
    }

    public String getString()   {
        throw new RuntimeException(String.format("heap table can't getString"));
    }

    @Override
    public Object getObject()   {
        return this;
    }

    public int compare(DataValueDescriptor other)   {
        throw new RuntimeException("heap not implemented feature");
    }

    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        return null;
    }


}
