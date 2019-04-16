package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.BinaryOperatorNode;
import com.cfs.sqlkv.sql.types.BooleanDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 15:24
 */
public abstract class BinaryLogicalOperatorNode extends BinaryOperatorNode {

    public static final String BooleanDataValueClassName = BooleanDataValue.class.getName();
    public boolean shortCircuitValue;

    public BinaryLogicalOperatorNode(ValueNode leftOperand, ValueNode rightOperand, String methodName, ContextManager cm) {
        super(leftOperand, rightOperand, methodName, methodName, BooleanDataValueClassName, BooleanDataValueClassName, cm);
    }

    @Override
    public ValueNode bindExpression(FromList fromList) {
        super.bindExpression(fromList);
        return this;
    }

    public DataTypeDescriptor resolveLogicalBinaryOperator(DataTypeDescriptor leftType, DataTypeDescriptor rightType) {
        if ((!(leftType.getTypeId().isBooleanTypeId())) || (!(rightType.getTypeId().isBooleanTypeId()))) {
            throw new RuntimeException("value must be boolean");
        }
        return leftType.getNullabilityType(leftType.isNullable() || rightType.isNullable());
    }
}
