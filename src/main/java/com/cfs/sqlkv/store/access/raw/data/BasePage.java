package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.common.SQLState;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.service.monitor.SQLKVObserver;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public abstract class BasePage implements Page, SQLKVObserver{

    public static final byte VALID_PAGE = 1;
    public static final byte INVALID_PAGE = 2;

    protected BaseContainerHandle owner;

    protected PageKey identity;

    /**在中止期间嵌套保持嵌套的次数*/
    private int nestedLatch;

    /**当前页是否*/
    protected boolean inClean;

    void setExclusive(BaseContainerHandle baseContainerHandle) throws StandardException{
        //根据
        Transaction transaction = baseContainerHandle.getTransaction();
        synchronized (this){
            if ((owner != null) && (transaction == owner.getTransaction())) {
                if (transaction.inAbort()) {
                    nestedLatch++;
                    return;
                }
                throw new RuntimeException(String.format("page s% attempted latched twice"));
            }

            /**
             *一直尝试获取当前为当前页的owner
             * */
            while (owner!=null){
                try {
                    wait();
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }
            }

            preLatch(baseContainerHandle);


            while(inClean){
                try {
                    wait();
                }
                catch (InterruptedException ie){
                    Thread.interrupted();
                }
            }

            preLatch = false;
        }
    }

    boolean setExclusiveNoWait(BaseContainerHandle requester) throws StandardException {

        Transaction t = requester.getTransaction();
        synchronized (this) {
            if ((owner != null) && (t == owner.getTransaction())) {
                if (t.inAbort()) {
                    nestedLatch++;
                    return true;
                }
            }
            if (owner == null) {
                preLatch(requester);
            } else {
                return false;
            }
            while (inClean) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }
            }
            preLatch = false;
        }
        return true;
    }


    /**
     *抓住此页之前的操作
     * */
    protected boolean preLatch;
    private void preLatch(BaseContainerHandle baseContainerHandle) {
        owner = baseContainerHandle;
        // make sure the latch is released if the container is closed
        baseContainerHandle.addObserver(this);
        preLatch = true;
    }

    @Override
    public void update(SQLKVObservable observable, Object extraInfo) {
        releaseExclusive();
    }

    /**
     * 释放当前页
     * */
    protected synchronized void releaseExclusive() {
        if (nestedLatch > 0) {
            nestedLatch--;
            return;
        }
        owner.deleteObserver(this);
        owner = null;
        notifyAll();
    }

    /**
     * 如果当前页面是溢出页面返回真,如果不是返回false
     * 对于没有溢出页面的返回false
     * */
    public abstract boolean isOverflowPage();

    private byte pageStatus;
    public byte getPageStatus() {
        return pageStatus;
    }

    @Override
    public void unlatch() {
        releaseExclusive();
    }
}
