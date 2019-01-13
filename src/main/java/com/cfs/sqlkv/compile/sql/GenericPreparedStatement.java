package com.cfs.sqlkv.compile.sql;


import com.cfs.sqlkv.compile.node.CursorNode;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.context.StatementContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.loader.GeneratedClass;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.sql.activation.GenericActivationHolder;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 16:21
 */
public class GenericPreparedStatement implements PreparedStatement{

    public Statement statement;

    protected GeneratedClass activationClass;

    public GenericPreparedStatement() {
    }

    public GenericPreparedStatement(Statement st) {
        this();
        statement = st;
    }

    public boolean isStorable() {
        return false;
    }

    @Override
    public Activation getActivation(LanguageConnectionContext lcc, boolean scrollable) throws StandardException {
        Activation ac;
        synchronized (this) {
            GeneratedClass gc = getActivationClass();
            ac = new GenericActivationHolder(lcc, gc, this, scrollable);
        }
        return ac;
    }

    /**
     *
     * */
    @Override
    public ResultSet execute(Activation activation, boolean forMetaData, long timeoutMillis) throws StandardException {
        return executeStmt(activation, false, forMetaData, timeoutMillis);
    }

    protected int updateMode;

    protected boolean isAtomic;

    private ResultSet executeStmt(Activation activation, boolean rollbackParentContext,
                                  boolean forMetaData, long timeoutMillis)throws StandardException {

        while (true){
            LanguageConnectionContext lccToUse = activation.getLanguageConnectionContext();
            //获取参数结果集
            ParameterValueSet pvs = activation.getParameterValueSet();

            StatementContext statementContext = lccToUse.pushStatementContext(
                    isAtomic, updateMode== CursorNode.READ_ONLY,getSource(), pvs, rollbackParentContext, timeoutMillis);

            statementContext.setActivation(activation);

            com.cfs.sqlkv.compile.result.ResultSet resultSet;
            try {
                resultSet = activation.execute();
                resultSet.open();
                return resultSet;
            } catch (StandardException se) {
                se.printStackTrace();
            }
            //lccToUse.popStatementContext(statementContext, null);
            return null;
        }
    }
    public GeneratedClass getActivationClass() throws StandardException {
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

    protected ConstantAction	executionConstants;
    public ConstantAction getConstantAction(){
        return	executionConstants;
    }

    public final void setConstantAction( ConstantAction constantAction ) {
        executionConstants = constantAction;
    }
}
