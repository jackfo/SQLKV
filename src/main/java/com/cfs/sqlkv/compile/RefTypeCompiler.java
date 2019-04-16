package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.sql.types.RefDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 20:45
 */
public class RefTypeCompiler extends BaseTypeCompiler {

    public String getCorrespondingPrimitiveTypeName() {
        return null;
    }


    public int getCastToCharWidth(DataTypeDescriptor dts) {
        return 0;
    }


    public boolean convertible(TypeId otherType, boolean forDataTypeFunction) {
        return false;
    }


    public boolean compatible(TypeId otherType) {
        return convertible(otherType, false);
    }


    public boolean storable(TypeId otherType, ClassFactory cf) {
        return otherType.isRefTypeId();
    }


    @Override
    public String interfaceName() {
        return RefDataValue.class.getName();
    }

    @Override
    public String nullMethodName() {
        return "getNullRef";
    }
}
