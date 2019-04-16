package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 19:28
 */
public class GenericColumnDescriptor implements ResultColumnDescriptor {

    private String name;
    private DataTypeDescriptor type;

    public GenericColumnDescriptor(ResultColumnDescriptor rcd) {
        name = rcd.getName();
        type = rcd.getType();
    }

    @Override
    public DataTypeDescriptor getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }
}
