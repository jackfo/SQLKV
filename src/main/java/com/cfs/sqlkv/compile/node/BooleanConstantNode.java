package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.types.SQLBoolean;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 15:33
 */
public class BooleanConstantNode extends ConstantNode {

    public boolean booleanValue;
    public boolean unknownValue;

    public BooleanConstantNode(ContextManager cm) {
        super(TypeId.BOOLEAN_ID, true, 1, cm);
        setValue(null);
    }

    @Override
    public void generateConstant(ActivationClassBuilder acb, MethodBuilder mb) {
        mb.push(booleanValue);
    }

    public BooleanConstantNode(boolean value, ContextManager cm) {
        super(TypeId.BOOLEAN_ID, false, 1, cm);
        super.setValue(new SQLBoolean(value));
        this.booleanValue = value;
    }

    public BooleanConstantNode(TypeId t, ContextManager cm) {
        super(t, true, 0, cm);
        this.unknownValue = true;
    }

    @Override
    public Object getConstantValueAsObject() {
        return booleanValue ? Boolean.TRUE : Boolean.FALSE;
    }


    public String getValueAsString() {
        if (booleanValue) {
            return "true";
        } else {
            return "false";
        }
    }

    @Override
    public boolean isBooleanTrue()
    {
        return (booleanValue && !unknownValue);
    }
}
