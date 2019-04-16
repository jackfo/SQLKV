package com.cfs.sqlkv.compile.sql;


import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.compile.node.CursorNode;
import com.cfs.sqlkv.compile.node.StatementNode;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.context.StatementContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;

import com.cfs.sqlkv.service.io.ArrayUtil;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.sql.activation.GenericActivationHolder;
import com.cfs.sqlkv.util.ByteArray;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 16:21
 */
public class GenericPreparedStatement implements ExecPreparedStatement {

    public Statement statement;

    protected Object[] savedObjects;

    protected GeneratedClass activationClass;

    public GenericPreparedStatement() {
    }

    public GenericPreparedStatement(Statement st) {
        this();
        statement = st;
    }


    public final void setSavedObjects(Object[] objects) {
        savedObjects = objects;
    }

    public boolean isStorable() {
        return false;
    }

    @Override
    public Activation getActivation(LanguageConnectionContext lcc, boolean scrollable) {

        Activation ac;
        synchronized (this) {
            GeneratedClass gc = getActivationClass();
            ac = new GenericActivationHolder(lcc, gc, this, scrollable);
        }
        return ac;
    }

    /**
     *
     */
    @Override
    public ResultSet execute(Activation activation, boolean forMetaData, long timeoutMillis) {
        return executeStmt(activation, false, forMetaData, timeoutMillis);
    }

    protected int updateMode;

    protected boolean isAtomic;

    private ResultSet executeStmt(Activation activation, boolean rollbackParentContext,
                                  boolean forMetaData, long timeoutMillis) {

        while (true) {
            LanguageConnectionContext lccToUse = activation.getLanguageConnectionContext();
            //获取参数结果集
            ParameterValueSet pvs = activation.getParameterValueSet();

            StatementContext statementContext = lccToUse.pushStatementContext(
                    isAtomic, updateMode == CursorNode.READ_ONLY, getSource(), pvs, rollbackParentContext, timeoutMillis);
            statementContext.setActivation(activation);

            com.cfs.sqlkv.compile.result.ResultSet resultSet;
            resultSet = activation.execute();
            resultSet.open();
            return resultSet;
        }
    }

    public GeneratedClass getActivationClass() {
        return activationClass;
    }

    public void setActivationClass(GeneratedClass ac) {
        activationClass = ac;
    }

    protected String sourceTxt;

    public String getSource() {
        return (sourceTxt != null) ?
                sourceTxt :
                (statement == null) ?
                        "null" :
                        statement.getSource();
    }

    protected ConstantAction executionConstants;

    public ConstantAction getConstantAction() {
        return executionConstants;
    }

    /**
     * 设置statement的执行行为
     */
    public final void setConstantAction(ConstantAction constantAction) {
        executionConstants = constantAction;
    }

    public ByteArray getByteCodeSaver() {
        return null;
    }

    protected ResultDescription resultDesc;

    public ResultDescription getResultDescription() {
        return resultDesc;
    }

    public void completeCompile(StatementNode qt){
        resultDesc = qt.makeResultDescription();
    }
    /**
     * 在根据节点解析的时候获取
     */
    public final Object getSavedObject(int objectNum) {
        return savedObjects[objectNum];
    }

    protected DataTypeDescriptor[] paramTypeDescriptors;

    public DataTypeDescriptor[] getParameterTypes() {
        return ArrayUtil.copy(paramTypeDescriptors);
    }
}
