package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.Formatable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description DataTypeDescriptor描述一个运行的SQL类型
 *              It consists of a catalog type (TypeDescriptor) and runtime attributes. The list of runtime attributes is:
 *
 *              当前类实现了Formatable。说明可以将自己写入到相应格式流或者从格式流中读取
 *              如果往当前类添加字段,确保你可以从writeExternal()/readExternal()方法中写或者读
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 17:51
 */
public class DataTypeDescriptor implements Formatable {

    public static final DataTypeDescriptor INTEGER = new DataTypeDescriptor(TypeId.INTEGER_ID, true);

    private TypeDescriptorImpl	typeDescriptor;
    private TypeId			typeId;


    public DataTypeDescriptor(TypeId typeId, boolean isNullable) {
        this.typeId = typeId;
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
