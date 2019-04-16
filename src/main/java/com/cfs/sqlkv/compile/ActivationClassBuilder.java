package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.*;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.bytecode.BCJava;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.ClassBuilder;
import com.cfs.sqlkv.service.compiler.JavaFactory;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.loader.GeneratedByteCode;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.sql.activation.BaseActivation;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.util.ByteArray;
import com.cfs.sqlkv.util.CurrentDatetime;

import java.io.Serializable;
import java.lang.reflect.Modifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-07 18:45
 */
public class ActivationClassBuilder {

    public static final String currentDatetimeFieldName = "cdt";
    public ClassBuilder cb;
    public GeneratedClass gc;
    public int nextExprNum;
    public int nextNonFastExpr;
    public int nextFieldNum;
    public MethodBuilder constructor;
    public CompilerContext myCompCtx;
    public MethodBuilder executeMethod;
    protected LocalField cdtField;
    private String currentRowScanResultSetName;

    public ActivationClassBuilder(String superClass, String className, CompilerContext cc) {
        int modifiers = Modifier.PUBLIC | Modifier.FINAL;
        myCompCtx = cc;
        JavaFactory javaFactory = new BCJava();
        if (className == null) {
            className = myCompCtx.getUniqueClassName();
        }
        cb = javaFactory.newClassBuilder(myCompCtx.getClassFactory(),
                "com.cfs.sqlkv.exe.", modifiers,
                className, superClass);

        beginConstructor();

    }

    /**
     * 创建类的构造器
     */
    private void beginConstructor() {
        MethodBuilder realConstructor = cb.newConstructorBuilder(Modifier.PUBLIC);
        realConstructor.callSuper();
        realConstructor.methodReturn();
        realConstructor.complete();
        constructor = cb.newMethodBuilder(Modifier.PUBLIC, "void", "postConstructor");
    }

    public void finishConstructor() {
        int numResultSets;
        setNumSubqueries();
        numResultSets = getRowCount();
        if (numResultSets >= 1) {
            addNewArrayOfRows(numResultSets);
        }
        constructor.methodReturn();
        constructor.complete();
    }


    public int getRowCount() {
        return myCompCtx.getNumResultSets();
    }

    private void addNewArrayOfRows(int numResultSets) {
        constructor.pushThis();
        constructor.pushNewArray(ExecRow.class.getName(), numResultSets);
        constructor.putField(BaseActivation.class.getName(), "row", ExecRow.class.getName() + "[]");
        constructor.endStatement();
    }

    /**
     * 设置子查询
     */
    public void setNumSubqueries() {
        //int numSubqueries = myCompCtx.getNumSubquerys();
        int numSubqueries = 0;
        if (numSubqueries == 0)
            return;
        /**
         * 生成的代码是 numSubqueries = x;
         * */
        constructor.pushThis();
        constructor.push(numSubqueries);
        constructor.putField(BaseActivation.class.getName(), "numSubqueries", "int");
        constructor.endStatement();
    }

    public MethodBuilder getConstructor() {
        return constructor;
    }

    public ClassBuilder getClassBuilder() {
        return cb;
    }

    public MethodBuilder getExecuteMethod() {
        if (executeMethod == null) {
            executeMethod = cb.newMethodBuilder(Modifier.PROTECTED, "void", "reinit");
        }
        return executeMethod;
    }

    public LocalField newFieldDeclaration(int modifiers, String type, String name) {
        return cb.addField(type, name, modifiers);
    }

    public LocalField newFieldDeclaration(int modifiers, String type) {
        return cb.addField(type, newFieldName(), modifiers);
    }

    /**
     * 创建新的字段名
     * */
    private String newFieldName() {
        return "e".concat(Integer.toString(nextFieldNum++));
    }

    public MethodBuilder newGeneratedFun(String returnType, int modifiers) {
        return newGeneratedFun(returnType, modifiers, (String[]) null);
    }

    public MethodBuilder newGeneratedFun(String returnType, int modifiers, String[] params) {
        String exprName = "g".concat(Integer.toString(nextNonFastExpr++));
        return newGeneratedFun(exprName, returnType, modifiers, params);
    }

    private MethodBuilder newGeneratedFun(String exprName, String returnType, int modifiers, String[] params) {
        MethodBuilder exprMethod;
        if (params == null) {
            exprMethod = cb.newMethodBuilder(modifiers, returnType, exprName);
        } else {
            exprMethod = cb.newMethodBuilder(modifiers, returnType,
                    exprName, params);
        }

        return exprMethod;
    }

    public MethodBuilder newExprFun() {
        String exprName = "e".concat(Integer.toString(nextExprNum++));
        return newGeneratedFun(exprName, "java.lang.Object", Modifier.PUBLIC, (String[]) null);
    }

    public void pushMethodReference(MethodBuilder mb, MethodBuilder exprMethod) {

        mb.pushThis();
        mb.push(exprMethod.getName());
        mb.callMethod(VMOpcode.INVOKEINTERFACE, GeneratedByteCode.class.getName(),
                "getMethod", GeneratedMethod.class.getName(), 1);
    }

    public MethodBuilder newUserExprFun() {
        MethodBuilder mb = newExprFun();
        mb.addThrownException("java.lang.Exception");
        return mb;
    }

    public String getRowLocationScanResultSetName() {
        return currentRowScanResultSetName;
    }

    void getCurrentDateExpression(MethodBuilder mb) {
        // do any needed setup
        LocalField lf = getCurrentSetup();
        mb.getField(lf);
        mb.callMethod(VMOpcode.INVOKEVIRTUAL, (String) null, "getCurrentDate", "java.sql.Date", 0);
    }

    protected LocalField getCurrentSetup() {
        if (cdtField != null)
            return cdtField;
        //    private CurrentDatetime cdt;
        cdtField = newFieldDeclaration(
                Modifier.PRIVATE,
                CurrentDatetime.class.getName(),
                currentDatetimeFieldName);
        //	  cdt = new CurrentDatetime();

        constructor.pushNewStart(CurrentDatetime.class.getName());
        constructor.pushNewComplete(0);
        constructor.setField(cdtField);
        return cdtField;
    }

    protected TypeCompiler getTypeCompiler(TypeId typeId) {
        throw new RuntimeException("");
    }

    public GeneratedClass getGeneratedClass(ByteArray savedBytes) {
        if (gc != null) return gc;
        if (savedBytes != null) {
            ByteArray classBytecode = cb.getClassBytecode();
            savedBytes.setBytes(classBytecode.getArray());
            savedBytes.setLength(classBytecode.getLength());
        }
        gc = cb.getGeneratedClass();
        return gc;
    }

    public void pushThisAsActivation(MethodBuilder mb) {
        mb.pushThis();
        mb.upCast(Activation.class.getName());
    }

    public void generateNull(MethodBuilder mb, TypeCompiler tc, int collationType) {
        throw new RuntimeException("");
    }

    private Object getDVF;

    void pushDataValueFactory(MethodBuilder mb) {
        //生成代码   getDataValueFactory()
        if (getDVF == null) {
            getDVF = mb.describeMethod(VMOpcode.INVOKEVIRTUAL,
                    BaseActivation.class.getName(),
                    "getDataValueFactory",
                    DataValueFactory.class.getName());
        }

        mb.pushThis();
        mb.callMethod(getDVF);
    }

    public void finishExecuteMethod() {

        if (executeMethod != null) {
            executeMethod.methodReturn();
            executeMethod.complete();
        }
    }

    private Object getRSF;

    public void pushGetResultSetFactoryExpression(MethodBuilder mb) {
        // generated Java:
        //	this.getResultSetFactory()
        //
        if (getRSF == null) {
            getRSF = mb.describeMethod(VMOpcode.INVOKEVIRTUAL, BaseActivation.class.getName(),
                    "getResultSetFactory",
                    ResultSetFactory.class.getName());
        }
        mb.pushThis();
        mb.callMethod(getRSF);
    }

    public int addItem(Object o) {
        return myCompCtx.addSavedObject(o);
    }

    private Object getEF;

    public void pushGetExecutionFactoryExpression(MethodBuilder mb) {
        if (getEF == null) {
            getEF = mb.describeMethod(VMOpcode.INVOKEVIRTUAL, BaseActivation.class.getName(),
                    "getExecutionFactory",
                    ExecutionFactory.class.getName());
        }
        // generated Java:
        //	this.getExecutionFactory()
        //
        mb.pushThis();
        mb.callMethod(getEF);
    }

    /**
     * 自动生成数据值
     */
    public void generateDataValue(MethodBuilder mb, TypeCompiler tc, int collationType, LocalField field) {
        pushDataValueFactory(mb);
        mb.swap();
        tc.generateDataValue(mb, collationType, field);
    }

    public String newRowLocationScanResultSetName() {
        currentRowScanResultSetName = newFieldName();
        return currentRowScanResultSetName;
    }

    public void pushColumnReference(MethodBuilder mb, int rsNumber, int colId) {
        mb.pushThis();
        mb.push(rsNumber);
        mb.push(colId);
        mb.callMethod(VMOpcode.INVOKEVIRTUAL, BaseActivation.class.getName(), "getColumnFromRow", DataValueDescriptor.class.getName(), 2);
    }

    public void generateNullWithExpress(MethodBuilder mb, TypeCompiler tc, int collationType) {
        pushDataValueFactory(mb);
        mb.swap();
        mb.cast(tc.interfaceName());
        tc.generateNull(mb, collationType);
    }
}
