package com.cfs.sqlkv.service.locks;



import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-15 17:19
 */
public class LockTable {

    private final ConcurrentHashMap<Lockable, Entry> locks = new ConcurrentHashMap<>();

    /**
     * 通过指定的lockSpace 锁住对象
     * */
    public Lock lockObject(LockSpace lockSpace, Lockable ref, Object qualifier, int timeout)   {
        LockControl control;
        Lock lockItem;


        Entry entry = getEntry(ref);
        try{

            /**
             *
             * */
            Control gc = entry.control;
            if(gc == null){
                Lock gl = new Lock(lockSpace, ref, qualifier);
                gl.grant();
                entry.control = gl;
                return gl;
            }
            control = gc.getLockControl();
            if (control != gc) {
                entry.control = control;
            }




        }finally {

        }
        return null;
    }

    /**
     * 从锁表获取一个Entry
     * */
    private Entry getEntry(Lockable ref){
        Entry e = locks.get(ref);
        while (true){

            if (e != null){
                //尝试获取锁,
                e.lock();
                if (e.control != null) {
                    return e;
                }
            }else{
                e = new Entry();
                e.lock();
            }

            Entry current = locks.putIfAbsent(ref, e);
            if (current == null) {
                return e;
            }
            e.unlock();
            e = current;
        }
    }

    public static final class Entry {
        /**锁的控制器*/
        public Control control;

        /**排它锁确保只有单线程可以使用它*/
        private final ReentrantLock mutex = new ReentrantLock();

        private Condition deadlockDetection;

        public void lock() {
            mutex.lock();
            while (deadlockDetection != null) {
                deadlockDetection.awaitUninterruptibly();
            }
        }

        public void unlock() {
            mutex.unlock();
        }

        public void lockForDeadlockDetection() {
            mutex.lock();
        }

        public void enterDeadlockDetection() {
            deadlockDetection = mutex.newCondition();
            mutex.unlock();
        }

        public void exitDeadlockDetection() {
            mutex.lock();
            deadlockDetection.signalAll();
            deadlockDetection = null;
        }

    }


}
