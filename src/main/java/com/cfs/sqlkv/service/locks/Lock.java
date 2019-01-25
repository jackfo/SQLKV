package com.cfs.sqlkv.service.locks;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 17:22
 */
public class Lock implements Control {

    private final LockSpace space;
    private final Lockable	ref;
    private final Object	qualifier;
    private int count;

    public Lock(LockSpace space, Lockable ref, Object qualifier) {
        super();
        this.space = space;
        this.ref = ref;
        this.qualifier = qualifier;
    }

    public void grant() {
        count++;
        // Tell the object it has been locked by this type of qualifier.
        ref.lockEvent(this);
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
