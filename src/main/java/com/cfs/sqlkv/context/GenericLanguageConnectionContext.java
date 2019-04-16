package com.cfs.sqlkv.context;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.factory.TypeCompilerFactory;
import com.cfs.sqlkv.compile.parse.ParseException;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.compile.sql.PreparedStatement;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.DataValueFactoryImpl;
import com.cfs.sqlkv.factory.GenericLanguageFactory;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.temp.Temp;

import java.util.ArrayList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 15:20
 */
public class GenericLanguageConnectionContext extends ContextImpl implements LanguageConnectionContext{

    protected LanguageConnectionFactory connFactory;

    protected TypeCompilerFactory tcf;

    private Database db;

    protected GenericLanguageFactory langFactory;

    protected DataValueFactory dataFactory = new DataValueFactoryImpl();

    private final TransactionManager transactionController;

    public GenericLanguageConnectionContext(ContextManager cm, TransactionManager tranCtrl,
                    GenericLanguageFactory lf, LanguageConnectionFactory lcf, Database db,
                    String userName, int instanceNumber, String drdaID, String dbname)   {
        super(cm,LanguageConnectionContext.CONTEXT_ID);
        this.db = db;
        this.connFactory = lcf;
        this.langFactory = lf;
        this.transactionController = tranCtrl;
    }


    @Override
    public LanguageConnectionFactory getLanguageConnectionFactory() {
        return connFactory;
    }

    @Override
    public CompilerContext pushCompilerContext() {
        return pushCompilerContext(null);
    }


    /**
     * 获取编译上下文
     * */
    @Override
    public CompilerContext pushCompilerContext(SchemaDescriptor sd) {
        CompilerContext cc;
        boolean firstCompilerContext = false;
        ContextManager contextManager = getContextManager();


        /**
         * 先获取编译上下文,不存在的话创建一个
         * */
        cc = (CompilerContext) (contextManager.getContext(CompilerContext.CONTEXT_ID));
        if (cc == null) {
            firstCompilerContext = true;
        }
        /**
         *如果当前编译上下文为空或者在使用 则新创建一个
         * */
        if (cc == null || cc.getInUse()) {
            cc = new CompilerContextImpl(getContextManager(), this, tcf);
            if (firstCompilerContext) {
                cc.firstOnStack();
            }
        }else {
            cc.resetContext();
        }

        cc.setInUse(true);
        return  cc;
    }

    /**
     * 根据语言连接上下文获取StatementContext
     * 根据"StatementContext"来获取StatementContext上下文
     * */
    @Override
    public StatementContext getStatementContext() {
        return (StatementContext) getContextManager().getContext(Context.LANG_STATEMENT);
    }

    @Override
    public DataDictionary getDataDictionary() {
        return getDatabase().getDataDictionary();
    }

    /**
     * 获取相应的数据库
     * */
    public Database getDatabase() {
        return db;
    }



    protected int bindCount;
    @Override
    public int incrementBindCount(){
        bindCount++;
        return bindCount;
    }


    @Override
    public PreparedStatement prepareInternalStatement(SchemaDescriptor compilationSchema, String sqlText, boolean isForReadOnly, boolean allowInternalSyntax)   {
        try {
            return connFactory.getStatement(compilationSchema, sqlText, isForReadOnly).prepare(this, false);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("");
    }

    @Override
    public SchemaDescriptor getDefaultSchema() {
        return Temp.schemaDescriptor;
        //return getCurrentSQLSessionContext().getDefaultSchema();
    }

    private int     statementDepth;
    @Override
    public int getStatementDepth() {
        return statementDepth;
    }


    /**
     *
     * */
    private final StatementContext[] statementContexts = new StatementContext[2];

    private SQLSessionContext topLevelSSC;

    @Override
    public StatementContext pushStatementContext(boolean isAtomic, boolean isForReadOnly, String stmtText, ParameterValueSet pvs, boolean rollbackParentContext, long timeoutMillis) {
        int parentStatementDepth = statementDepth;
        boolean  inTrigger = false;
        boolean  parentIsAtomic = false;
        StatementContext statementContext = statementContexts[0];

        if(statementContext==null){
            //表明statement没有进行过程初始化
            statementContext = statementContexts[0] = new GenericStatementContext(this);
        }else if(statementDepth>0){
            /**
             * 在statementDepth的深度大于0的时候 标识statementContexts[0]已经被使用
             * 那么深度为1直接放在数组下标为1的为位置将statementContexts[0设置为父级statementContext
             * 如果深度大于1则是将tatementContexts[1]设置为父级statementContext
             * */
             StatementContext    parentStatementContext;
            if (statementDepth == 1){
                statementContext = statementContexts[1];
                if (statementContext == null){
                    statementContext = statementContexts[1] = new GenericStatementContext(this);
                } else{
                    statementContext.pushMe();
                }
                parentStatementContext = statementContexts[0];
            }else{
                parentStatementContext = getStatementContext();
                statementContext = new GenericStatementContext(this);
            }
        }
        incrementStatementDepth();
        return statementContext;
    }

    private void incrementStatementDepth() { statementDepth++; }

    @Override
    public void initialize()   {
         setDefaultSchema(initDefaultSchemaDescriptor());
    }

    @Override
    public TransactionManager getTransactionCompile() {
        return transactionController;
    }

    @Override
    public void addActivation(Activation a) {
    }

    public DataValueFactory getDataValueFactory() {
        return dataFactory;
    }

    /**
     * 设置默认模式
     * */
    public void setDefaultSchema(SchemaDescriptor sd)   {
        if (sd == null) {
            sd = getInitialDefaultSchemaDescriptor();
        }
        getCurrentSQLSessionContext().setDefaultSchema(sd);
    }


    private SQLSessionContext getCurrentSQLSessionContext(){
        StatementContext ctx = getStatementContext();
        SQLSessionContext curr;
        if(ctx==null){
            curr = getTopLevelSQLSessionContext();
        }else{

            curr =ctx.getSQLSessionContext();
        }
        return curr;
    }


    public SQLSessionContext getTopLevelSQLSessionContext() {
        if (topLevelSSC == null) {
            topLevelSSC = new SQLSessionContext(getInitialDefaultSchemaDescriptor());
        }
        return topLevelSSC;
    }

    private SchemaDescriptor getInitialDefaultSchemaDescriptor() {
        return cachedInitialDefaultSchemaDescr;
    }

    private String sessionUser = null;

    public String getSessionUserId() {
        return sessionUser;
    }

    private SchemaDescriptor cachedInitialDefaultSchemaDescr = null;

    /**
     *
     * TODO:模式待实现
     * @return computed initial default schema value for this session
     * @
     */
    protected SchemaDescriptor initDefaultSchemaDescriptor()   {
        if(cachedInitialDefaultSchemaDescr==null){
            DataDictionary dataDictionary = getDataDictionary();
            UUID uuid = new UUID("8000000d-00d0-fd77-3ed8-000a0a0b1900");
            SchemaDescriptor sd = new SchemaDescriptor(dataDictionary, Temp.sessionId,Temp.sessionId,uuid,false);
            Temp.schemaDescriptor =sd;
            cachedInitialDefaultSchemaDescr = sd;
        }
        return cachedInitialDefaultSchemaDescr;
    }

    @Override
    public int decrementBindCount() {
        bindCount--;
        return bindCount;
    }

    @Override
    public GenericLanguageFactory getLanguageFactory() {
        return langFactory;
    }

    @Override
    public int getCurrentIsolationLevel() {
        return 0;
    }

    @Override
    public void popStatementContext(StatementContext statementContext, Throwable error) {

    }

    @Override
    public TransactionManager getTransactionExecute() {
        return transactionController;
    }
}
