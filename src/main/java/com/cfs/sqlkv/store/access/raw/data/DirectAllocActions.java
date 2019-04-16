package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 18:42
 */
public class DirectAllocActions implements AllocationActions {
    @Override
    public void actionAllocatePage(Transaction t, BasePage allocPage, long pageNumber, int doStatus, int undoStatus)   {
    }
}
