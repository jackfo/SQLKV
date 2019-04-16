package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Descriptionb
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 12:26
 */
public class DropOnCommit extends ContainerActionOnCommit {
    protected boolean isStreamContainer = false;


    public DropOnCommit(ContainerKey identity) {
        super(identity);
    }

    public DropOnCommit(ContainerKey identity, boolean isStreamContainer) {
        super(identity);
        this.isStreamContainer = isStreamContainer;
    }


    /**
     * 如果事务是提交或者中止状态
     */
    @Override
    public void update(SQLKVObservable observable, Object extraInfo) {
        if (extraInfo.equals(Transaction.COMMIT) || extraInfo.equals(Transaction.ABORT)) {
            Transaction transaction = (Transaction) observable;

            if (this.isStreamContainer) {
                transaction.dropStreamContainer(identity.getSegmentId(), identity.getContainerId());
            } else {
                transaction.dropContainer(identity);
            }


            observable.deleteObserver(this);
        }
    }
}
