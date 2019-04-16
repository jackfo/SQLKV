package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.FromList;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.compile.result.CursorResultSet;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.types.RefDataValue;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.lang.reflect.Modifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 13:35
 */
public class CurrentRowLocationNode extends ValueNode {

    public CurrentRowLocationNode(ContextManager cm) {
        super(cm);
    }

    @Override
    public ValueNode bindExpression(FromList fromList) {
        setType(new DataTypeDescriptor(TypeId.getBuiltInTypeId(TypeId.REF_NAME), false));
        return this;
    }

    @Override
    public void generateExpression(ActivationClassBuilder acb, MethodBuilder mbex) {
        MethodBuilder mb = acb.newGeneratedFun(DataValueDescriptor.class.getName(), Modifier.PROTECTED);
        LocalField field = acb.newFieldDeclaration(Modifier.PRIVATE, RefDataValue.class.getName());
        mb.pushThis();
        mb.getField( null, acb.getRowLocationScanResultSetName(), CursorResultSet.class.getName());
        mb.callMethod(VMOpcode.INVOKEINTERFACE,null, "getRowLocation", TableRowLocation.class.getName(), 0);
        acb.generateDataValue(mb, getTypeCompiler(), getTypeServices().getCollationType(), field);
        mb.putField(field);
        mb.methodReturn();
        mb.complete();
        mbex.pushThis();
        mbex.callMethod(VMOpcode.INVOKEVIRTUAL, null, mb.getName(), DataValueDescriptor.class.getName(), 0);
    }
}
