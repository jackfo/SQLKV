package com.cfs.sqlkv.row;

import com.cfs.sqlkv.common.PersistentService;
import com.cfs.sqlkv.common.context.ContextManager;

import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.transaction.TransactionFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:52
 */
public class RawStoreFactory {
    public static final int PAGE_SIZE_MINIMUM = 1024;


    protected BaseDataFileFactory dataFactory;
    {
        String dataDirectory = System.getProperty(PersistentService.ROOT);
        dataFactory = new BaseDataFileFactory(dataDirectory);
    }

    protected TransactionFactory transactionFactory = new TransactionFactory(this);


    public long getMaxContainerId() {
        return dataFactory.getMaxContainerId();
    }

    public Transaction findUserTransaction(ContextManager contextMgr, String transName) {
        return transactionFactory.findUserTransaction(this, contextMgr, transName);
    }

    public BaseDataFileFactory getDataFactory() {
        return dataFactory;
    }
}
