package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 15:20
 */
public class ContextImpl implements Context{

    private final String myIdName;
    private final ContextManager myContextManager;

    protected ContextImpl(ContextManager cm, String id) {
        myIdName = id;
        myContextManager = cm;
        cm.pushContext(this);
    }

    @Override
    public final ContextManager getContextManager() {
        return myContextManager;
    }

    @Override
    public String getIdName() {
        return myIdName;
    }
}
