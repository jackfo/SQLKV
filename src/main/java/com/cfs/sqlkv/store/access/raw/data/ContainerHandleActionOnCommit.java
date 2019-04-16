package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 12:29
 */
public abstract class ContainerHandleActionOnCommit extends ContainerActionOnCommit {

    public ContainerHandleActionOnCommit(ContainerKey identity) {
        super(identity);
    }

    /**
     * 将容器打开之后做一些处理
     */
    public void openContainerAndDoIt(Transaction transaction) {
        BaseContainerHandle handle = null;
        try {
            handle = transaction.openContainer(identity);
            if (handle != null) {

                doIt(handle);

            }

        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }


    protected abstract void doIt(BaseContainerHandle handle);

}
