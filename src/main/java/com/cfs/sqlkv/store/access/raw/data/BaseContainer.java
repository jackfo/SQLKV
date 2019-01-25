package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
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
public abstract class BaseContainer implements Lockable{

    protected long			firstAllocPageNumber;
    protected long			firstAllocPageOffset;
    protected long			containerVersion;
    protected long			estimatedRowCount;
    protected LogInstant    lastLogInstant;

    protected int pageSize;
    protected int minimumRecordSize;
    protected final CacheManager pageCache;

    public BaseContainer(BaseDataFileFactory baseDataFileFactory){
        pageCache = baseDataFileFactory.getPageCache();
    }

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
        Transaction transaction = handle.getTransaction().startNestedTopTransaction();
        //获取容器句柄模式
        int mode = handle.getMode();

        //根据identity打开对应的容器
        BaseContainerHandle allocHandle =transaction.openContainer(identity, null, mode);

        if (allocHandle == null){
            throw new RuntimeException("The allocation nested top transaction cannot open the container.");
        }

        //获取锁空间锁住
        LockSpace lockSpace = transaction.getLockSpace();

        //根据事务获取事务工厂 在事务工厂里获取锁工厂
        //transaction.getLockFactory().lockObject(lockSpace, transaction, this, null, LockFactory.WAIT_FOREVER);

        BasePage newPage = null;
        try{
            newPage = newPage(handle, transaction, allocHandle, isOverflow);
        }finally {

        }

    }

    protected AllocationCache allocCache;

    /**
     * 在容器中创建一个新页面
     *
     * 只允许单线程执行到这里,通过锁的方式已经进行实现
     *
     * 添加新页面涉及:
     *    两个事务:用户事务和嵌套顶级事务
     *
     * 第一步:保证能够获得嵌套事务
     *       如果不具备嵌套事务,将容器所对应的事务设置为嵌套事务
     *
     *
     *
     * */
    protected  BasePage newPage(BaseContainerHandle userhandle, Transaction ntt, BaseContainerHandle allocHandle, boolean isOverflow) throws StandardException{

        //判定是否使用了嵌套事务
        boolean useNTT = (ntt != null);

        //如果
        if (!useNTT){
            ntt = userhandle.getTransaction();
        }

        long lastPage;
        long lastPreallocPage;
        long pageNumber = BaseContainerHandle.INVALID_PAGE_NUMBER;

        /**页面的key*/
        PageKey pkey;
        /**是否重新利用*/
        boolean reuse;

        /**是否进行重试*/
        boolean retry;

        AllocPage allocPage = null;
        BasePage page = null;

        try{
            do{
                retry = false;
                synchronized (allocCache){
                    allocPage = findAllocPageForAdd(allocHandle, ntt, startSearch);
                }
            }
        }

    }

    /**
     * 查找或者分配一个可处理的新页面
     * */
    private AllocPage findAllocPageForAdd(BaseContainerHandle allocHandle, Transaction ntt, long lastAllocatedPage){
        AllocPage allocPage = null;
        AllocPage oldAllocPage = null;
        boolean success = false;
        try{
            if (firstAllocPageNumber == BaseContainerHandle.INVALID_PAGE_NUMBER){
                allocPage = makeAllocPage(ntt, allocHandle, FIRST_ALLOC_PAGE_NUMBER, FIRST_ALLOC_PAGE_OFFSET, CONTAINER_INFO_SIZE);
            }
        }
    }


    private AllocPage makeAllocPage(Transaction ntt, BaseContainerHandle handle, long pageNumber, long pageOffset, int containerInfoSize){
        PageCreationArgs createAllocPageArgs = new PageCreationArgs(
                AllocPage.FORMAT_NUMBER, CachedPage.WRITE_SYNC,
                pageSize, 0, minimumRecordSize, containerInfoSize);

        if (pageNumber == FIRST_ALLOC_PAGE_NUMBER){
            firstAllocPageNumber = pageNumber;
            firstAllocPageOffset = pageOffset;
        }

        //创建一个页面Key 根据容器和页号
        PageKey pkey = new PageKey(identity, pageNumber);
        return (AllocPage)initPage(handle, pkey, createAllocPageArgs, pageOffset, false,false);
    }

    /**
     * 初始化一个页面
     *
     * @return 返回一个已经被初始化的页面
     * */
    protected BasePage initPage(BaseContainerHandle allochandle, PageKey pkey,
                                PageCreationArgs createArgs,
                                long pageOffset, boolean reuse,
                                boolean overflow) throws StandardException {

        BasePage page = null;
        boolean releasePage = true;
        try{

            if(reuse){
                /**
                 * 如果是重新利用先从缓存中拿到对应的页
                 * */
                page = (BasePage)pageCache.find(pkey);
                if(page==null){
                    throw new RuntimeException("Cannot find page to reuse."+pkey);
                }
            }else{
                page = (BasePage) pageCache.create(pkey, createArgs);
            }
            releasePage = false;
            //page = latchPage(allochandle, page, true /* may need to wait, track3822 */);

            int initPageFlag = 0;
            if (reuse){
                initPageFlag |= BasePage.INIT_PAGE_REUSE;
            }
            if (overflow){
                initPageFlag |= BasePage.INIT_PAGE_OVERFLOW;
            }
            if (reuse && isReusableRecordId()){
                initPageFlag |= BasePage.INIT_PAGE_REUSE_RECORDID;
            }
            page.initPage(initPageFlag, pageOffset);
            page.setContainerRowCount(estimatedRowCount);
        }finally {
            if (releasePage && page != null)
            {
                // release the new page from cache if it errors
                // out before the exclusive lock is set
                pageCache.release(page);
                page = null;
            }
        }
        return page;

    }

    protected boolean isReusableRecordId = false;
    protected boolean isReusableRecordId() {
        return isReusableRecordId;
    }


    public static final long FIRST_ALLOC_PAGE_NUMBER = 0L;
    public static final long FIRST_ALLOC_PAGE_OFFSET = 0L;

    /**
     The size of the persistently stored container info
     ContainerHeader contains the following information:
     4 bytes int	格式ID
     4 bytes	int	状态
     4 bytes int	页面大小
     4 bytes int	剩余空间
     4 bytes int    最小记录打下
     2 bytes short  初始页面
     2 bytes short  spare1
     8 bytes	long	首个分配页号
     8 bytes	long	首个分配索引
     8 bytes	long	容器版本
     8 bytes long	    estimated number of rows
     8 bytes long	    reusable recordId sequence number
     8 bytes long	    spare3
     8 bytes	long	checksum
     container info size is 80 bytes, with 10 bytes of spare space
     */

    private static final int CONTAINER_FORMAT_ID_SIZE = 4;
    public static final int CHECKSUM_SIZE = 8;
    public static final int CONTAINER_INFO_SIZE = CONTAINER_FORMAT_ID_SIZE+4+4+4+4+2+2+8+8+8+8+CHECKSUM_SIZE+8+8;
}
