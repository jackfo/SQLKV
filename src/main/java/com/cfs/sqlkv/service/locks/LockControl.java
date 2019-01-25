package com.cfs.sqlkv.service.locks;

import java.util.List;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 18:41
 */
public class LockControl implements Control {
    private final Lockable ref;
    private Lock firstGrant;
    private List<Lock> granted;
    private List<Lock> waiting;
    private Lock lastPossibleSkip;

    protected LockControl(Lock firstLock, Lockable ref) {
        super();
        this.ref = ref;
        firstGrant = firstLock;
    }

    @Override
    public Lockable getLockable() {
        return null;
    }

    @Override
    public LockControl getLockControl() {
        return null;
    }

    @Override
    public Lock getLock(LockSpace lockSpace, Object qualifier) {
        return null;
    }
}
