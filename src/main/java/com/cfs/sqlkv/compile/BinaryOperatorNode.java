package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 23:22
 */
public class BinaryOperatorNode extends ValueNode {
    public String operator;
    public String methodName;
    public ValueNode receiver;

    public final static int PLUS = 1;
    public final static int MINUS = 2;
    public final static int TIMES = 3;
    public final static int DIVIDE = 4;
    public final static int CONCATENATE = 5;
    public final static int EQ = 6;
    public final static int NE = 7;
    public final static int GT = 8;
    public final static int GE = 9;
    public final static int LT = 10;
    public final static int LE = 11;
    public final static int AND = 12;
    public final static int OR = 13;
    public final static int LIKE = 14;

    public ValueNode leftOperand;
    public ValueNode rightOperand;

    public String leftInterfaceType = DataValueDescriptor.class.getName();
    public String rightInterfaceType = DataValueDescriptor.class.getName();
    String resultInterfaceType;

    final int kind;
    final static int K_BASE = 2;

    public BinaryOperatorNode(ContextManager cm) {
        super(cm);
        kind = K_BASE;
    }

    public BinaryOperatorNode(ValueNode leftOperand, ValueNode rightOperand,
                              String operator, String methodName, String leftInterfaceType, String rightInterfaceType, ContextManager cm) {
        super(cm);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.methodName = methodName;
        this.operator = operator;
        this.leftInterfaceType = leftInterfaceType;
        this.rightInterfaceType = rightInterfaceType;
        this.kind = K_BASE;
    }

    public BinaryOperatorNode(
            ValueNode leftOperand,
            ValueNode rightOperand,
            String operator,
            String methodName,
            ContextManager cm) {
        super(cm);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.methodName = methodName;
        this.operator = operator;
        this.kind = K_BASE;
    }

    @Override
    public String toString() {
        return "operator: " + operator + "\n" + "methodName: " + methodName + "\n" +
                super.toString();
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
