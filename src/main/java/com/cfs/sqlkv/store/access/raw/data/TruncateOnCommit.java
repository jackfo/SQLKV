package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description 截断提交
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 12:25
 */
public class TruncateOnCommit extends ContainerHandleActionOnCommit{

    private boolean commitAsWell;

    public TruncateOnCommit(ContainerKey identity, boolean commitAsWell) {
        super(identity);
        this.commitAsWell = commitAsWell;
    }

    /**
     *对容器做截断
     * */
    @Override
    protected void doIt(BaseContainerHandle handle)   {
        handle.container.truncate(handle);
    }


    @Override
    public void update(SQLKVObservable observable, Object extraInfo) {
        if (extraInfo.equals(Transaction.ABORT) || extraInfo.equals(Transaction.SAVEPOINT_ROLLBACK) || (commitAsWell && extraInfo.equals(Transaction.COMMIT))) {
            openContainerAndDoIt((Transaction) observable);
        }

        if (extraInfo.equals(Transaction.COMMIT) || extraInfo.equals(Transaction.ABORT) || extraInfo.equals(identity)) {
            observable.deleteObserver(this);
        }
    }
}
