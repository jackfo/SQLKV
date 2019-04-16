package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;

import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-31 14:44
 */
public abstract class ConstantNode extends ValueNode {

    public DataValueDescriptor value;

    public ConstantNode(ContextManager cm) {
        super(cm);
    }

    public ConstantNode(TypeId typeId, boolean nullable, int maximumWidth, ContextManager cm) {
        super(cm);
        setType(typeId, nullable, maximumWidth);
    }

    public boolean isNull() {
        return (value == null || value.isNull());
    }

    public void setValue(DataValueDescriptor value) {
        this.value = value;
    }

    @Override
    public void generateExpression(ActivationClassBuilder acb, MethodBuilder mb) {
        if (isNull()) {
            acb.generateNull(mb, getTypeCompiler(), getTypeServices().getCollationType());
        } else {
            generateConstant(acb, mb);
            acb.generateDataValue(mb, getTypeCompiler(), getTypeServices().getCollationType(), null);
        }
    }

    public abstract void generateConstant(ActivationClassBuilder acb, MethodBuilder mb);
}