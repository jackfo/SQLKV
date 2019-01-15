package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 10:49
 */
public abstract class BaseContainer {

    protected abstract void flushAll() throws StandardException;

    protected void letGo(BaseContainerHandle handle) {
        Transaction transaction = handle.getTransaction();
        handle.getLockingPolicy().unlockContainer(transaction, handle);
    }

    protected boolean use(BaseContainerHandle handle, boolean forUpdate, boolean droppedOK) throws StandardException {

        /**
         * */
        if (forUpdate && !canUpdate()) {
             throw new RuntimeException("数据只读,不支持更新");
        }

        if (!droppedOK && (getDroppedState() || getCommittedDropState())) {
            return false;
        }

        return true;
    }

    /**
     * 容器是否支持更新
     * */
    protected abstract boolean canUpdate();


    /**
     * 容器的删除状态
     * */
    protected boolean	isDropped;
    protected boolean getDroppedState() {
        return isDropped;
    }

    /**
     *
     * */
    protected boolean isCommittedDrop;
    protected boolean getCommittedDropState() {
        return isCommittedDrop;
    }

    /**
     * 容器的标识
     * */
    protected ContainerKey identity;
    public Object getIdentity() {
        return identity;
    }

    public abstract BasePage getPage(BaseContainerHandle handle, long pageNumber, boolean wait) throws StandardException;

    public abstract void setEstimatedRowCount(long count, int flag) throws StandardException;

    public void truncate(BaseContainerHandle handle) throws StandardException { }

    /**
     * 添加页
     * */
    public Page addPage(BaseContainerHandle handle, boolean isOverflow) throws StandardException {

        //TODO:设置内嵌事务
        Transaction transaction = handle.getTransaction().
        int mode = handle.getMode();

        BaseContainerHandle allocHandle = (BaseContainerHandle)ntt.openContainer(identity, (LockingPolicy)null, mode);
    }
}
