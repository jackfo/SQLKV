package com.cfs.sqlkv.service.locks;

/**
 * @author zhengxiaokang
 * @Description 任何需要实现锁定的对象,都需实现当前接口
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 17:16
 */
public class Lockable {


    /**
     *
     Note the fact the object is locked. Performs required actions
     to ensure that unlockEvent() work correctly.
     This method does not actually  perform any locking of the
     object, the locking mechanism is provided by the lock manager.
     <P>
     If the class supports multiple lockers of the object then this method
     will be called once per locker, each with their own qualifier.
     <P>
     Must only be called by the lock manager. Synchronization will be handled
     by the lock manager.
     */
    public void lockEvent(Lock lockInfo){
        throw new RuntimeException("该方法必须被子类实现");
    }
}
