package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.predicate.PredicateList;

import java.util.List;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 15:38
 */
public class AndNode extends BinaryLogicalOperatorNode {
    public AndNode(ValueNode leftOperand, ValueNode rightOperand, ContextManager cm) {
        super(leftOperand, rightOperand, "and", cm);
        this.shortCircuitValue = false;
    }

    public AndNode(ValueNode leftOperand, ValueNode rightOperand, String methodName, ContextManager cm) {
        super(leftOperand, rightOperand, methodName, cm);
        this.shortCircuitValue = false;
    }

    @Override
    public ValueNode bindExpression(FromList fromList) {
        super.bindExpression(fromList);
        postBindFixup();
        return this;
    }

    public void postBindFixup() {
        DataTypeDescriptor dataTypeDescriptor = resolveLogicalBinaryOperator(leftOperand.getTypeServices(), rightOperand.getTypeServices());
        setType(dataTypeDescriptor);
    }

    @Override
    public ValueNode preprocess(int numTables, FromList outerFromList, PredicateList outerPredicateList) {
        leftOperand = leftOperand.preprocess(numTables, outerFromList, outerPredicateList);

        //目前设定右边恒为真,值实现where操作 不实现and和or的相关操作
        rightOperand = rightOperand.preprocess(numTables, outerFromList, outerPredicateList);
        return this;
    }

    public ValueNode getLeftOperand() {
        return leftOperand;
    }
}
