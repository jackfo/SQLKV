package com.cfs.sqlkv.store.access.raw;


import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.transaction.Transaction;

public interface LockingPolicy {

    /**
     * 无论什么情况都没有锁 隔离参数会被忽略
     */
    static final int MODE_NONE = 0;

    /**
     *行级锁
     */
    static final int MODE_RECORD = 1;

    /**
     * 容器级别锁,即表级锁
     */
    static final int MODE_CONTAINER = 2;

    /**
     * 当一个容器开始的时候进行调用
     * @param transaction 锁关联的事务
     * @param container   锁住的容器
     *
     * @return 容器被锁住则返回真,返之返回false
     * */
    public boolean lockContainer(Transaction transaction, BaseContainerHandle container, boolean waitForLock, boolean forUpdate)  ;

    /**解锁容器*/
    public void unlockContainer(Transaction t, BaseContainerHandle container);

    public int getMode();
}
