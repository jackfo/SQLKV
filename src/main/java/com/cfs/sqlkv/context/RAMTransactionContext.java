package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;

import com.cfs.sqlkv.store.JuniorTransaction;
import com.cfs.sqlkv.store.TransactionManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 16:22
 */
public class RAMTransactionContext extends ContextImpl{

    private JuniorTransaction transaction;

    public RAMTransactionContext(ContextManager cm, String context_id, JuniorTransaction theTransaction, boolean abortAll)   {
        super(cm, context_id);
        transaction = theTransaction;
    }

    public JuniorTransaction getTransaction() {
        return transaction;
    }

}
