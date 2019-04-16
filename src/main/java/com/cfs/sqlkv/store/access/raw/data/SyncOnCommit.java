package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 12:26
 */
public class SyncOnCommit extends ContainerHandleActionOnCommit{

    public SyncOnCommit(ContainerKey identity) {
        super(identity);
    }

    @Override
    protected void doIt(BaseContainerHandle handle)   {
        handle.container.flushAll();
    }

    @Override
    public void update(SQLKVObservable observable, Object extraInfo) {
        if (extraInfo.equals(Transaction.COMMIT)) {
            openContainerAndDoIt((Transaction)observable);
        }
        /**
         * 如果是提交或者终止
         * 则删除这个观察者
         * */
        if (extraInfo.equals(Transaction.COMMIT) || extraInfo.equals(Transaction.ABORT) || extraInfo.equals(identity)) {
            observable.deleteObserver(this);
        }
    }
}
