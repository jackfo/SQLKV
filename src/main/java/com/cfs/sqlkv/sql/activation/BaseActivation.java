package com.cfs.sqlkv.sql.activation;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.factory.ClassFactory;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.sql.GenericPreparedStatement;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.GenericExecutionFactory;
import com.cfs.sqlkv.factory.GenericLanguageFactory;
import com.cfs.sqlkv.factory.GenericResultSetFactory;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;
import com.cfs.sqlkv.loader.ClassInspector;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 17:58
 */
public class BaseActivation implements Activation{

    private	LanguageConnectionContext	lcc;
    protected ContextManager cm;
    protected GenericPreparedStatement preStmt;
    protected ResultSet resultSet;
    protected ParameterValueSet pvs;

    private TableDescriptor ddlTableDescriptor;

    public BaseActivation(LanguageConnectionContext languageConnectionContext){
        lcc = (LanguageConnectionContext) languageConnectionContext.getContextManager().getContext(LanguageConnectionContext.CONTEXT_ID);
    }


    public BaseActivation(LanguageConnectionContext languageConnectionContext,GenericPreparedStatement genericPreparedStatement){
        this.lcc = (LanguageConnectionContext) languageConnectionContext.getContextManager().getContext(LanguageConnectionContext.CONTEXT_ID);
        this.preStmt = genericPreparedStatement;
    }


    @Override
    public GenericPreparedStatement getPreparedStatement() {
        return preStmt;
    }

    @Override
    public LanguageConnectionContext getLanguageConnectionContext() {
        return	lcc;
    }

    @Override
    public ParameterValueSet getParameterValueSet() {
        if (pvs == null){
            setParameterValueSet(0, false);
        }
        return pvs;
    }

    protected final void setParameterValueSet(int paramCount, boolean hasReturnParam) {
        GenericLanguageFactory genericLanguageFactory = lcc.getLanguageFactory();
        LanguageConnectionFactory languageConnectionFactory = lcc.getLanguageConnectionFactory();
        ClassFactory classFactory = languageConnectionFactory.getClassFactory();
        ClassInspector classInspector = classFactory.getClassInspector();
        pvs = genericLanguageFactory.newParameterValueSet(classInspector,paramCount, hasReturnParam);
    }

    /**
     * 执行过程创建结果集
     * */
    @Override
    public ResultSet execute() throws StandardException {
        if (resultSet == null) {
            resultSet = createResultSet();
        }
        return resultSet;
    }

    @Override
    public GenericExecutionFactory getExecutionFactory() {
        return getLanguageConnectionContext().getLanguageConnectionFactory().getExecutionFactory();
    }

    @Override
    public ConstantAction getConstantAction() {
        return preStmt.getConstantAction();
    }

    private boolean forCreateTable;
    @Override
    public void setForCreateTable() {
        forCreateTable = true;
    }

    @Override
    public void setDDLTableDescriptor(TableDescriptor td) {
        ddlTableDescriptor = td;
    }

    protected ResultSet createResultSet() throws StandardException {
        return getResultSetFactory().getDDLResultSet(this);
    }

    public final GenericResultSetFactory getResultSetFactory() {
        return getExecutionFactory().getResultSetFactory();
    }

}
