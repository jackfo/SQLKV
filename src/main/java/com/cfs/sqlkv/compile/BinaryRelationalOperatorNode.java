package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.Orderable;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.FromList;
import com.cfs.sqlkv.compile.node.FromTable;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.compile.predicate.Optimizable;
import com.cfs.sqlkv.compile.predicate.PredicateList;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 11:28
 */
public class BinaryRelationalOperatorNode extends BinaryOperatorNode implements RelationalOperator {

    public final static int K_EQUALS = 0;
    public final static int K_GREATER_EQUALS = 1;
    public final static int K_GREATER_THAN = 2;
    public final static int K_LESS_EQUALS = 3;
    public final static int K_LESS_THAN = 4;
    public final static int K_NOT_EQUALS = 5;

    public final int kind;
    private int relOpType;

    public BinaryRelationalOperatorNode(int kind, ValueNode leftOperand, ValueNode rightOperand, ContextManager cm) {
        super(leftOperand, rightOperand, getOperatorName(kind), getMethodName(kind), cm);
        this.kind = kind;
        constructorMinion();
    }

    private void constructorMinion() {
        this.relOpType = getRelOpType(this.kind);
    }


    private int getRelOpType(int op) {
        switch (op) {
            case K_EQUALS:
                return RelationalOperator.EQUALS_RELOP;
            case K_GREATER_EQUALS:
                return RelationalOperator.GREATER_EQUALS_RELOP;
            case K_GREATER_THAN:
                return RelationalOperator.GREATER_THAN_RELOP;
            case K_LESS_EQUALS:
                return RelationalOperator.LESS_EQUALS_RELOP;
            case K_LESS_THAN:
                return RelationalOperator.LESS_THAN_RELOP;
            case K_NOT_EQUALS:
                return RelationalOperator.NOT_EQUALS_RELOP;
            default:
                return 0;
        }
    }

    private static String getOperatorName(int kind) {
        String operatorName = "";
        switch (kind) {
            case K_EQUALS:
                operatorName = "=";
                break;
            case K_GREATER_EQUALS:
                operatorName = ">=";
                break;
            case K_GREATER_THAN:
                operatorName = ">";
                break;
            case K_LESS_EQUALS:
                operatorName = "<=";
                break;
            case K_LESS_THAN:
                operatorName = "<";
                break;
            case K_NOT_EQUALS:
                operatorName = "<>";
                break;
            default:
                break;
        }

        return operatorName;
    }


    private static String getMethodName(int kind) {
        String methodName = "";
        switch (kind) {
            case K_EQUALS:
                methodName = "equals";
                break;
            case K_GREATER_EQUALS:
                methodName = "greaterOrEquals";
                break;
            case K_GREATER_THAN:
                methodName = "greaterThan";
                break;
            case K_LESS_EQUALS:
                methodName = "lessOrEquals";
                break;
            case K_LESS_THAN:
                methodName = "lessThan";
                break;
            case K_NOT_EQUALS:
                methodName = "notEquals";
                break;
            default:
                break;
        }
        return methodName;
    }

    @Override
    public ValueNode bindExpression(FromList fromList) {
        leftOperand = leftOperand.bindExpression(fromList);
        rightOperand = rightOperand.bindExpression(fromList);
        boolean nullableResult = leftOperand.getTypeServices().isNullable() || rightOperand.getTypeServices().isNullable();
        setType(new DataTypeDescriptor(TypeId.BOOLEAN_ID, nullableResult));
        return this;
    }

    @Override
    public ValueNode preprocess(int numTables, FromList outerFromList, PredicateList outerPredicateList) {
        leftOperand = leftOperand.preprocess(numTables, outerFromList, outerPredicateList);
        rightOperand = rightOperand.preprocess(numTables, outerFromList, outerPredicateList);
        return this;
    }

    @Override
    public void generateAbsoluteColumnId(MethodBuilder mb, Optimizable optTable) {
        int columnPosition = getAbsoluteColumnPosition(optTable);
        mb.push(columnPosition);
    }


    private int getAbsoluteColumnPosition(Optimizable optTable) {
        ColumnReference cr;
        int columnPosition;
        cr = (ColumnReference) leftOperand;
        columnPosition = cr.getSource().getColumnPosition();
        return columnPosition - 1;
    }

    @Override
    public void generateOperator(MethodBuilder mb, Optimizable optTable) {
        switch (relOpType) {
            case RelationalOperator.EQUALS_RELOP:
                mb.push(Orderable.ORDER_OP_EQUALS);
                break;

            case RelationalOperator.NOT_EQUALS_RELOP:
                mb.push(Orderable.ORDER_OP_EQUALS);
                break;

            case RelationalOperator.LESS_THAN_RELOP:
            case RelationalOperator.GREATER_EQUALS_RELOP:
                mb.push(Orderable.ORDER_OP_LESSTHAN);
                break;
            case RelationalOperator.LESS_EQUALS_RELOP:
            case RelationalOperator.GREATER_THAN_RELOP:
                mb.push(Orderable.ORDER_OP_LESSOREQUALS);

        }
    }

    @Override
    public void generateQualMethod(ActivationClassBuilder acb, MethodBuilder mb, Optimizable optTable) {
        MethodBuilder qualMethod = acb.newUserExprFun();
        rightOperand.generateExpression(acb, qualMethod);
        qualMethod.methodReturn();
        qualMethod.complete();
        acb.pushMethodReference(mb, qualMethod);
    }

    @Override
    public void generateOrderedNulls(MethodBuilder mb) {
        mb.push(false);
    }
}
