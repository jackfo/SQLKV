package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.Formatable;
import com.cfs.sqlkv.catalog.TypeDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 18:08
 */
public class TypeDescriptorImpl implements TypeDescriptor,Formatable {


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
