package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.locks.LockFactory;
import com.cfs.sqlkv.service.locks.LockSpace;
import com.cfs.sqlkv.service.locks.Lockable;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.store.access.raw.log.LogInstant;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 10:49
 */
public abstract class BaseContainer extends Lockable {


    protected long firstAllocPageOffset;
    protected long containerVersion;
    protected long estimatedRowCount;
    protected LogInstant lastLogInstant;

    protected int pageSize = 4096;
    protected int minimumRecordSize;


    BaseContainer() {

    }

    protected abstract void flushAll();

    protected void letGo(BaseContainerHandle handle) {
        Transaction transaction = handle.getTransaction();
        handle.getLockingPolicy().unlockContainer(transaction, handle);
    }

    protected boolean use(BaseContainerHandle handle, boolean forUpdate, boolean droppedOK) {
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
     */
    protected abstract boolean canUpdate();


    /**
     * 容器的删除状态
     */
    protected boolean isDropped;

    protected boolean getDroppedState() {
        return isDropped;
    }

    /**
     *
     */
    protected boolean isCommittedDrop;

    protected boolean getCommittedDropState() {
        return isCommittedDrop;
    }

    /**
     * 容器的标识
     */
    protected ContainerKey identity;

    public Object getIdentity() {
        return identity;
    }

    public abstract BasePage getPage(BaseContainerHandle handle, long pageNumber, boolean wait);

    public abstract void setEstimatedRowCount(long count, int flag);

    public void truncate(BaseContainerHandle handle) {
    }

    /**
     * 添加页
     */
    public Page addPage(BaseContainerHandle handle, boolean isOverflow) {
        //TODO:设置内嵌事务
        Transaction transaction = handle.getTransaction().startNestedTopTransaction();
        //获取容器句柄模式
        int mode = handle.getMode();
        //根据identity打开对应的容器
        BaseContainerHandle allocHandle = transaction.openContainer(identity);
        if (allocHandle == null) {
            throw new RuntimeException("The allocation nested top transaction cannot open the container.");
        }
        BasePage newPage = null;
        try {
            newPage = newPage(handle, transaction, allocHandle, isOverflow);
            newPage.writePage(newPage.identity);
        } finally {
            transaction.close();
        }
        return newPage;
    }

    protected AllocationCache allocCache;

    /**
     * 在容器中创建一个新页面
     * <p>
     * 只允许单线程执行到这里,通过锁的方式已经进行实现
     * <p>
     * 添加新页面涉及:
     * 两个事务:用户事务和嵌套顶级事务
     * <p>
     * 第一步:保证能够获得嵌套事务
     * 如果不具备嵌套事务,将容器所对应的事务设置为嵌套事务
     */
    protected abstract BasePage newPage(BaseContainerHandle userhandle, Transaction ntt, BaseContainerHandle allocHandle, boolean isOverflow);

    protected boolean isReusableRecordId = false;

    protected boolean isReusableRecordId() {
        return isReusableRecordId;
    }


    public static final long FIRST_ALLOC_PAGE_NUMBER = 0L;
    public static final long FIRST_ALLOC_PAGE_OFFSET = 0L;

    /**
     * The size of the persistently stored container info
     * ContainerHeader contains the following information:
     * 4 bytes int	格式ID
     * 4 bytes	int	状态
     * 4 bytes int	页面大小
     * 4 bytes int	剩余空间
     * 4 bytes int    最小记录打下
     * 2 bytes short  初始页面
     * 2 bytes short  spare1
     * 8 bytes	long	首个分配页号
     * 8 bytes	long	首个分配索引
     * 8 bytes	long	容器版本
     * 8 bytes long	    estimated number of rows
     * 8 bytes long	    reusable recordId sequence number
     * 8 bytes long	    spare3
     * 8 bytes	long	checksum
     * container info size is 80 bytes, with 10 bytes of spare space
     */

    private static final int CONTAINER_FORMAT_ID_SIZE = 4;
    public static final int CHECKSUM_SIZE = 8;
    public static final int CONTAINER_INFO_SIZE = CONTAINER_FORMAT_ID_SIZE + 4 + 4 + 4 + 4 + 2 + 2 + 8 + 8 + 8 + 8 + CHECKSUM_SIZE + 8 + 8;

    protected abstract BasePage getPageForInsert(BaseContainerHandle handle, int flag);

    protected Page getAllocPage(BaseContainerHandle handle, long pageNumber, boolean wait) {
        return latchPage(handle, getAllocPage(pageNumber), wait);
    }

    /**
     * 根据页号获取分配页
     */
    protected abstract BasePage getAllocPage(long pageNumber);

    protected BasePage latchPage(BaseContainerHandle handle, BasePage foundPage, boolean wait) {
        if (foundPage != null) {
            foundPage.owner = handle;
            //foundPage.setExclusive(handle);
        }
        return foundPage;
    }

    public void clearIdentity() {
        identity = null;
    }

    public long getFirstPageAllocPageNumber() {
        return 0;
    }

    protected void removePage(BaseContainerHandle handle, BasePage page) {
        try {
            deallocatePage(handle, page);
        } finally {
            if (page != null) {
                page.unlatch();
            }
        }
    }


    protected abstract void deallocatePage(BaseContainerHandle userhandle, BasePage page);


    protected Page getFirstPage(BaseContainerHandle handle) {
        return getFirstHeadPage(handle, true);
    }

    public abstract BasePage getFirstHeadPage(BaseContainerHandle handle, boolean wait);

    public Page getNextPage(BaseContainerHandle handle, long pageNumber) {
        return getNextHeadPage(handle, pageNumber, true );
    }

    public abstract BasePage getNextHeadPage(BaseContainerHandle handle, long pageNumber, boolean wait);
}
