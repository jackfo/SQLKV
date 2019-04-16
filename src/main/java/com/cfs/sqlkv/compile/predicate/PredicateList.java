package com.cfs.sqlkv.compile.predicate;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.RelationalOperator;
import com.cfs.sqlkv.compile.node.AndNode;
import com.cfs.sqlkv.compile.node.QueryTreeNodeVector;
import com.cfs.sqlkv.compile.node.SelectNode;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.compile.table.FromBaseTable;
import com.cfs.sqlkv.factory.ExecutionFactory;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.activation.BaseActivation;
import com.cfs.sqlkv.store.access.Qualifier;

import java.lang.reflect.Modifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 16:07
 */
public class PredicateList extends QueryTreeNodeVector<Predicate> implements OptimizablePredicateList {


    private int numberOfQualifiers;

    public PredicateList(ContextManager cm) {
        super(Predicate.class, cm);
    }

    /**
     * 将表达式添加到当前PredicateList集合
     *
     * @param searchClause 表达式
     */
    public void pullExpressions(int numTables, ValueNode searchClause) {
        if (searchClause != null) {
            AndNode andNode = (AndNode) searchClause;
            Predicate predicate = new Predicate(andNode, getContextManager());
            addPredicate(predicate);
        }

    }


    public void generateQualifiers(ActivationClassBuilder acb, MethodBuilder mb, Optimizable optTable, boolean absolute) {
        String retvalType = Qualifier.class.getName() + "[][]";
        if (numberOfQualifiers == 0) {
            mb.pushNull(retvalType);
            return;
        }
        MethodBuilder consMB = acb.getConstructor();
        MethodBuilder executeMB = acb.getExecuteMethod();
        LocalField qualField = acb.newFieldDeclaration(Modifier.PRIVATE, retvalType);
        executeMB.getField(qualField);
        executeMB.callMethod(VMOpcode.INVOKESTATIC, BaseActivation.class.getName(), "reinitializeQualifiers", "void", 1);
        int num_of_or_conjunctions = 0;
        consMB.pushNewArray(Qualifier.class.getName() + "[]", num_of_or_conjunctions + 1);
        consMB.setField(qualField);
        consMB.getField(qualField);             // 1st arg allocateQualArray
        consMB.push(0);                   // 2nd arg allocateQualArray
        consMB.push(numberOfQualifiers - num_of_or_conjunctions);  // 3rd arg allocateQualArray
        consMB.callMethod(
                VMOpcode.INVOKESTATIC, BaseActivation.class.getName(),
                "allocateQualArray", "void", 3);
        Predicate pred = elementAt(0);
        generateSingleQualifierCode(
                consMB,
                optTable,
                absolute,
                acb,
                pred.getRelop(),
                qualField,
                0,
                0);
        mb.getField(qualField);
    }

    private void generateSingleQualifierCode(
            MethodBuilder consMB, Optimizable optTable,
            boolean absolute, ActivationClassBuilder acb, RelationalOperator or_node,
            LocalField qualField, int array_idx_1, int array_idx_2) {
        consMB.getField(qualField);
        consMB.pushThis();
        consMB.callMethod(
                VMOpcode.INVOKEVIRTUAL,
                BaseActivation.class.getName(),
                "getExecutionFactory", ExecutionFactory.MODULE, 0);

        or_node.generateAbsoluteColumnId(consMB, optTable);

        or_node.generateOperator(consMB, optTable);

        or_node.generateQualMethod(acb, consMB, optTable);

        acb.pushThisAsActivation(consMB);

        or_node.generateOrderedNulls(consMB);


        consMB.callMethod(
                VMOpcode.INVOKEINTERFACE, ExecutionFactory.MODULE,
                "getQualifier", Qualifier.class.getName(), 5);


        consMB.push(array_idx_1);       // third  arg for setQualifier
        consMB.push(array_idx_2);       // fourth arg for setQualifier

        consMB.callMethod(
                VMOpcode.INVOKESTATIC, BaseActivation.class.getName(),
                "setQualifier", "void", 4);
    }

    public void pushUsefulPredicates(Optimizable optTable) {
        for (Predicate p : this) {
            p.clearScanFlags();
        }
        int size = size();
        for (int index = 0; index < size; index++) {
            Predicate pred = elementAt(index);
            pred.markQualifier();
            if (optTable instanceof FromBaseTable) {
                optTable.pushOptPredicate(pred);
                removeOptPredicate(pred);
            }
        }
    }


    public void addPredicate(Predicate predicate) {
        if (predicate.isQualifier())
            numberOfQualifiers++;
        addElement(predicate);
    }


    public void pushExpressionsIntoSelect(SelectNode select) {
        for (int index = size() - 1; index >= 0; index--) {
            Predicate predicate = elementAt(index);
            select.pushExpressionsIntoSelect(predicate);
        }
    }

    public PredicateList getPushablePredicates() {
        PredicateList pushPList = null;
        for (int index = size() - 1; index >= 0; index--) {
            Predicate predicate = elementAt(index);
            if (pushPList == null) {
                pushPList = new PredicateList(getContextManager());
            }
            pushPList.addPredicate(predicate);
            removeElementAt(index);
        }
        return pushPList;
    }

    public OptimizablePredicate getOptPredicate(int index) {
        return elementAt(index);
    }

    public void addOptPredicate(OptimizablePredicate optPredicate) {
        addElement((Predicate) optPredicate);
        if (optPredicate.isQualifier())
            numberOfQualifiers++;
    }

    public final void removeOptPredicate(OptimizablePredicate pred) {
        removeElement((Predicate) pred);
        if (pred.isQualifier())
            numberOfQualifiers--;
    }

    public void setPredicatesAndProperties(OptimizablePredicateList otherList) {
        PredicateList theOtherList = (PredicateList) otherList;
        theOtherList.removeAllElements();
        for (int i = 0; i < size(); i++) {
            theOtherList.addOptPredicate(getOptPredicate(i));
        }
        theOtherList.numberOfQualifiers = numberOfQualifiers;
    }
}
