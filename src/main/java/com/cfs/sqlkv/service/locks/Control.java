package com.cfs.sqlkv.service.locks;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 20:16
 */
public interface Control {

    public Lockable getLockable();

    public LockControl getLockControl();

    public Lock getLock(LockSpace lockSpace, Object qualifier);
}
