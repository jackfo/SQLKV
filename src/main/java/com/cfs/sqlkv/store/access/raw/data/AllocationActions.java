package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.transaction.Transaction;

public interface AllocationActions {


    /**
     * 根据页号来设置分配页的状态,以及对区的状态进行处理
     * */
    public void actionAllocatePage(Transaction t, BasePage allocPage, long pageNumber, int doStatus, int undoStatus)  ;
}
