package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 12:29
 */
public abstract class ContainerHandleActionOnCommit extends ContainerActionOnCommit{

    public ContainerHandleActionOnCommit(ContainerKey identity) {
        super(identity);
    }

    /**
     * 将容器打开之后做一些处理
     * */
    public void openContainerAndDoIt(Transaction transaction) {
        BaseContainerHandle handle = null;
        try {
            handle = transaction.openContainer(identity,  null, BaseContainerHandle.MODE_FORUPDATE | BaseContainerHandle.MODE_NO_ACTIONS_ON_COMMIT);
            if (handle != null) {
                try {
                    doIt(handle);
                } catch (StandardException se) {
                    transaction.setObserverException(se);
                }
            }

        } catch (StandardException se) {
            // if we get this exception, then the container is readonly.
            // no problem if we can't open an closed temp container.
            if (identity.getSegmentId()  != BaseContainerHandle.TEMPORARY_SEGMENT){
                transaction.setObserverException(se);
            }
        } finally {
            if (handle != null){
                handle.close();
            }
        }
    }



    protected abstract void doIt(BaseContainerHandle handle) throws StandardException;

}
