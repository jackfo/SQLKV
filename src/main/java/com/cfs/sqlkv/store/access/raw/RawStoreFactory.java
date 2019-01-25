package com.cfs.sqlkv.store.access.raw;

import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.transaction.TransactionFactory;

/**
 * @author zhengxiaokang
 * @Description 行存储的实现
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 15:28
 */
public class RawStoreFactory {

    protected TransactionFactory transactionFactory;
    protected BaseDataFileFactory dataFactory;

    public static final int PAGE_SIZE_MINIMUM = 1024;
}
