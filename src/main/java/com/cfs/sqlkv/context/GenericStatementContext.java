package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 21:09
 */
public class GenericStatementContext extends ContextImpl implements StatementContext {

    private SQLSessionContext sqlSessionContext;

    private	final LanguageConnectionContext lcc;

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
}
