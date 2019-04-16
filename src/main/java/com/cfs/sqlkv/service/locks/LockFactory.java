package com.cfs.sqlkv.service.locks;



/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 15:31
 */
public class LockFactory {

    public static final int WAIT_FOREVER = -1;

    public static final int TIMED_WAIT = -2;

    public static final int NO_WAIT = 0;

    protected final LockTable lockTable = new LockTable();

    /**
     * 通过锁工厂来锁住对应的对象
     *
     * */
    public boolean lockObject(LockSpace lockSpace, Object group, Lockable ref, Object qualifier, int timeout)   {

        Lock lock = lockTable.lockObject(lockSpace, ref, qualifier, timeout);
        return false;
    }
}
