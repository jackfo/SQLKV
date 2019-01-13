package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.store.RAMTransaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 16:22
 */
public class RAMTransactionContext extends ContextImpl{

    private RAMTransaction transaction;

    public RAMTransactionContext(ContextManager cm, String context_id, RAMTransaction theTransaction, boolean abortAll) throws StandardException {
        super(cm, context_id);
        transaction = theTransaction;
    }

    public RAMTransaction getTransaction() {
        return transaction;
    }

}
