package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.ContextImpl;
import com.cfs.sqlkv.factory.GenericExecutionFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-16 14:22
 */
public class GenericExecutionContext extends ContextImpl {

    public static final String CONTEXT_ID = "ExecutionContext";

    public GenericExecutionContext(ContextManager cm, GenericExecutionFactory ef) {
        super(cm, GenericExecutionContext.CONTEXT_ID);
        execFactory = ef;
    }

    private GenericExecutionFactory execFactory;

    public GenericExecutionFactory getExecutionFactory() {
        return execFactory;
    }
}
