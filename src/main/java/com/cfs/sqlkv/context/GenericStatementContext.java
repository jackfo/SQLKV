package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.ResultSet;

import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 21:09
 */
public class GenericStatementContext extends ContextImpl implements StatementContext{

    private SQLSessionContext sqlSessionContext;

    private	final LanguageConnectionContext lcc;

    private boolean inUse = true;

    public GenericStatementContext(LanguageConnectionContext lcc){
        super(lcc.getContextManager(), Context.LANG_STATEMENT);
        this.lcc = lcc;
    }

    private Activation activation;

    @Override
    public SQLSessionContext getSQLSessionContext() {
        return sqlSessionContext;
    }

    @Override
    public void setActivation(Activation a) {
        activation = a;
    }

    @Override
    public	boolean	onStack() {
        return inUse;
    }

    @Override
    public void setTopResultSet(ResultSet topResultSet, NoPutResultSet[] subqueryTrackingArray)   {

    }

    @Override
    public NoPutResultSet[] getSubqueryTrackingArray()   {
        return new NoPutResultSet[0];
    }

    @Override
    public void pushMe() {

    }
}
