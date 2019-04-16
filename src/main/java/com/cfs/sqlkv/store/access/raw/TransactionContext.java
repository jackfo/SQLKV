package com.cfs.sqlkv.store.access.raw;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.ContextImpl;
import com.cfs.sqlkv.row.RawStoreFactory;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-14 20:22
 */
public class TransactionContext extends ContextImpl {
    private Transaction transaction;
    private RawStoreFactory factory;
    private	boolean   abortAll;

    public TransactionContext(ContextManager cm,String name,Transaction transaction,boolean abortAll, RawStoreFactory rawStore){
        super(cm, name);
        this.transaction = transaction;
        this.abortAll = abortAll;
        this.factory = factory;
        transaction.transactionContext = this;
    }

    public RawStoreFactory getFactory() {
        return factory;
    }

    public Transaction getTransaction() {
        return transaction;
    }


}
