package com.cfs.sqlkv.store.access;


import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.service.io.FormatIdUtil;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description 存储格式Id
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-03 15:07
 */
public class StorableFormatId extends DataType {

    private int format_id;

    public StorableFormatId() {
    }

    public StorableFormatId(int value) {
        this.format_id = value;
    }

    public int getValue() {
        return format_id;
    }

    public void setValue(int input_value) {
        this.format_id = input_value;
    }

    public int getTypeFormatId() {
        return StoredFormatIds.ACCESS_FORMAT_ID;
    }

    public boolean isNull() {
        return false;
    }

    @Override
    public String getString()   {
        return null;
    }



    public void writeExternal(ObjectOutput out) throws IOException {
        FormatIdUtil.writeFormatIdInteger(out, format_id);
    }

    public void readExternal(ObjectInput in) throws IOException {
        format_id = FormatIdUtil.readFormatIdInteger(in);
    }

    public void restoreToNull() {
        format_id = 0;
    }

    public Object getObject()   {
        return (this);
    }

    @Override
    public int compare(DataValueDescriptor other)   {
        return 0;
    }

    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        return null;
    }

    public DataValueDescriptor getNewNull() {
        return null;
    }

}
