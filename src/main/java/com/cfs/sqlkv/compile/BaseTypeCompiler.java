package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 13:15
 */
public abstract class BaseTypeCompiler implements TypeCompiler {

    private TypeId correspondingTypeId;

    public void generateNull(MethodBuilder mb, int collationType) {
        int argCount;

        argCount = 1;

        mb.callMethod(VMOpcode.INVOKEINTERFACE, (String) null,
                nullMethodName(),
                interfaceName(),
                argCount);
    }

    public abstract String nullMethodName();

    public void setTypeId(TypeId typeId) {
        correspondingTypeId = typeId;
    }

    public void generateDataValue(MethodBuilder mb, int collationType, LocalField field) {
        String interfaceName = interfaceName();
        if (field == null) {
            mb.pushNull(interfaceName);
        } else {
            mb.getField(field);
        }
        int argCount = 2;
        mb.callMethod(VMOpcode.INVOKEINTERFACE, (String) null,
                dataValueMethodName(),
                interfaceName,
                argCount);

        if (field != null) {
            mb.putField(field);
        }
    }

    public String dataValueMethodName() {
        return "getDataValue";
    }

    public int getStoredFormatIdFromTypeId() {
        return getTypeId().getTypeFormatId();
    }

    public TypeId getTypeId() {
        return correspondingTypeId;
    }
}
