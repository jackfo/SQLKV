package com.cfs.sqlkv.service.cache;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-17 19:24
 */
public final class CacheEntry {

    /**重入锁*/
    private final ReentrantLock mutex = new ReentrantLock();

    private Cacheable cacheable;

    /**多少个线程持有这个CacheEntry*/
    private int keepCount;

    private Condition forRemove;

    public void lock() {
        mutex.lock();
    }

    public void unlock() {
        mutex.unlock();
    }


}
