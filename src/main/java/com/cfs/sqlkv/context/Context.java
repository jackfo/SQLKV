package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 15:20
 */
public interface Context {

    String LANG_STATEMENT = "StatementContext";

    public ContextManager getContextManager();

    public String getIdName();
}
