package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.ArrayOutputStream;
import com.cfs.sqlkv.service.io.FormatIdOutputStream;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.io.storage.StorageRandomAccessFile;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.transaction.Transaction;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 10:50
 */
public abstract class FileContainer extends BaseContainer implements Cacheable {
    /**
     * 页面缓存
     */
    public final CacheManager pageCache;
    /**
     * 容器缓存
     */
    protected final CacheManager containerCache;
    /**
     * 数据缓存
     */
    protected final BaseDataFileFactory dataFactory;

    protected short initialPages;

    protected boolean canUpdate;

    private int PreAllocThreshold;

    private int PreAllocSize;

    /**
     *
     */
    private long lastUnfilledPage;

    /**
     * 首个分配页页号在具有分配页的时候,进行注入
     */
    protected long firstAllocPageNumber;

    public FileContainer(BaseDataFileFactory factory) {
        dataFactory = factory;
        pageCache = factory.getPageCache();
        containerCache = factory.getContainerCache();
        initContainerHeader(true);
    }


    /**
     * 根据pageNumber获取使用页
     * <p>
     * 前置条件:页面是有效的,所以需要进行检验
     */
    private BasePage getUserPage(BaseContainerHandle handle, long pageNumber, boolean overflowOK) {
        //如果页号小于首页则直接返回空
        if (pageNumber < BaseContainerHandle.FIRST_PAGE_NUMBER) {
            throw new RuntimeException("getUserPage must be greater than 1");
        }
        //如果不是合法页 则返回空
        if (!pageValid(handle, pageNumber)) {
            return null;
        }

        //创建页页对应的键
        PageKey pageSearch = new PageKey(identity, pageNumber);
        BasePage page = (BasePage) pageCache.find(pageSearch);
        if (latchPage(handle, page, false) == null) {
            return null;
        }
        if (page != null) {
            return page;
        }
        return page;
    }

    /**
     *
     */
    protected BasePage latchPage(BaseContainerHandle handle, BasePage foundPage, boolean wait) {
        if (foundPage == null) {
            return null;
        }
        BasePage ret = super.latchPage(handle, foundPage, wait);
        if (ret == null) {
            pageCache.release(foundPage);
        }
        return ret;
    }

    /**
     * 检验页面的合法性
     */
    private boolean pageValid(BaseContainerHandle handle, long pagenum) {
        boolean retval = false;
        synchronized (allocCache) {
            //获取最后一页的页号
            long lastPageNumber = allocCache.getLastPageNumber(handle, firstAllocPageNumber);
            int pageStatus = allocCache.getPageStatus(handle, pagenum, firstAllocPageNumber);
            if (pagenum <= lastPageNumber && pageStatus == AllocExtent.ALLOCATED_PAGE) {
                retval = true;
            }
        }
        return retval;
    }

    /**
     * 获取用户页
     */
    @Override
    public BasePage getPage(BaseContainerHandle handle, long pageNumber, boolean wait) {
        return getUserPage(handle, pageNumber, true);
    }

    /**
     * 日志轨迹操作
     * 设置当前页没有被填满
     */
    protected void trackUnfilledPage(long pagenumber, boolean unfilled) {

    }

    /**
     * 容器剩余的插入空间
     */
    protected int spareSpace;

    protected int getSpareSpace() {
        return spareSpace;
    }

    protected int getMinimumRecordSize() {
        return minimumRecordSize;
    }


    public FileChannel getChannel(StorageRandomAccessFile file) {
        if (file instanceof RandomAccessFile) {
            return ((RandomAccessFile) file).getChannel();
        }
        return null;
    }

    /**
     * Read a page into the supplied array.
     *
     * @param pageNumber 阅读数据的页号
     * @param pageData  the buffer to read data into
     * @param offset -1 normally (not used since offset is computed from
     * pageNumber), but used if pageNumber == -1
     * (getEmbryonicPage)
     * @exception IOException exception reading page
     * @exception StandardException Standard SQLKV error policy
     */

    private byte[] containerInfo;

    public void readHeader(byte[] epage) throws IOException {
        // initialize header from information stored in containerInfo
        AllocPage.readContainerInfo(containerInfo, epage);
        readHeaderFromArray(containerInfo);
    }

    protected static final int formatIdInteger = StoredFormatIds.RAW_STORE_SINGLE_CONTAINER_FILE;

    /**
     * 从字节数组中读取containerInfo容器Header数组必须由writeHeaderFromArray写入或写入相同的格式
     */
    private void readHeaderFromArray(byte[] a) throws IOException {
        ArrayInputStream inStream = new ArrayInputStream(a);
        inStream.setLimit(CONTAINER_INFO_SIZE);
        int fid = inStream.readInt();
        if (fid != formatIdInteger) {
            throw new RuntimeException(String.format("Unknown container format at container %s : %s", getIdentity(), fid));
        }
        int status = inStream.readInt();
        pageSize = inStream.readInt();
        spareSpace = inStream.readInt();
        minimumRecordSize = inStream.readInt();
        initialPages = inStream.readShort();
        PreAllocSize = inStream.readShort();
        firstAllocPageNumber = inStream.readLong();
        firstAllocPageOffset = inStream.readLong();
        containerVersion = inStream.readLong();
        estimatedRowCount = inStream.readLong();
        if (initialPages == 0) {
            initialPages = 1;
        }
        allocCache.reset();
    }

    /**
     * 写入容器头部到文件
     */
    protected void writeHeader(Object identity, StorageRandomAccessFile file, boolean create, byte[] epage) throws IOException {
        //将容器信息写入containerInfo字节数组
        writeHeaderToArray(containerInfo);
        //将containerInfo信息转移到epage 供下面写入到页面
        try {
            AllocPage.writeContainerInfo(containerInfo, epage, create);
        } catch (Exception se) {
            throw new RuntimeException(String.format("Write of container information to page 0 of container 4% failed.  See nested error for more information.", identity));
        }
        //写数据到文件
        writeAtOffset(file, epage, FIRST_ALLOC_PAGE_OFFSET);
    }

    /**
     * 将数据写入pageData
     */
    protected void writeHeader(Object identity, byte[] pageData) throws IOException {
        writeHeaderToArray(containerInfo);
        AllocPage.writeContainerInfo(containerInfo, pageData, false);
    }

    public abstract void createContainer(ContainerKey newIdentity);

    protected void fillInIdentity(ContainerKey key) {
        identity = key;
    }

    protected boolean preDirty;
    protected boolean isDirty;

    protected void setDirty(boolean dirty) {
        synchronized (this) {
            preDirty = false;
            isDirty = dirty;
            notifyAll();
        }
    }

    protected Cacheable createIdent(ContainerKey newIdentity, Object createParameter) {
        if (createParameter != this) {
            initContainerHeader(true);
        }
        createContainer(newIdentity);
        setDirty(true);
        fillInIdentity(newIdentity);
        return this;
    }

    /**
     * 初始化容器基本信息
     * */
    private void initContainerHeader(boolean changeContainer) {
        if (containerInfo == null) {
            containerInfo = new byte[CONTAINER_INFO_SIZE];
        }

        if (allocCache == null) {
            allocCache = new AllocationCache();
        } else {
            allocCache.reset();
        }

        //首个分配页,开始设置为不合法的页号
        firstAllocPageNumber = BaseContainerHandle.INVALID_PAGE_NUMBER;
        initializeLastInsertedPage(1);
        lastUnfilledPage = BaseContainerHandle.INVALID_PAGE_NUMBER;
        lastAllocatedPage = BaseContainerHandle.INVALID_PAGE_NUMBER;
    }

    private static final int FILE_DROPPED = 0x1;
    private static final int FILE_COMMITTED_DROP = 0x2;
    private static final int FILE_REUSABLE_RECORDID = 0x8;


    /**
     * 设置最后插入页的页号
     */
    private synchronized void setLastInsertedPage(long val) {
        lastInsertedPage[lastInsertedPage_index] = val;
    }


    /**
     * Write a sequence of bytes at the given offset in a file. This method
     * is not thread safe, so the caller must make sure that no other thread
     * is performing operations that may change current position in the file.
     *
     * @param file   the file to write to
     * @param bytes  the bytes to write
     * @param offset the offset to start writing at
     * @throws IOException if an I/O error occurs while writing
     * @ SQLKV Standard error policy
     */
    public void writeAtOffset(StorageRandomAccessFile file, byte[] bytes, long offset) throws IOException {
        file.seek(offset);
        file.write(bytes);
    }

    /**
     * 写入头部信息
     */
    private void writeHeaderToArray(byte[] a) throws IOException {
        ArrayOutputStream a_out = new ArrayOutputStream(a);
        FormatIdOutputStream outStream = new FormatIdOutputStream(a_out);
        int status = 0;
        if (getDroppedState()) {
            status |= FILE_DROPPED;
        }
        if (getCommittedDropState()) {
            status |= FILE_COMMITTED_DROP;
        }
        if (isReusableRecordId()) {
            status |= FILE_REUSABLE_RECORDID;
        }
        a_out.setPosition(0);
        a_out.setLimit(CONTAINER_INFO_SIZE);
        outStream.writeInt(formatIdInteger);
        outStream.writeInt(status);
        outStream.writeInt(pageSize);
        outStream.writeInt(spareSpace);
        outStream.writeInt(minimumRecordSize);
        outStream.writeShort(initialPages);
        outStream.writeShort(PreAllocSize);
        outStream.writeLong(firstAllocPageNumber);
        outStream.writeLong(firstAllocPageOffset);
        outStream.writeLong(containerVersion);
        outStream.writeLong(estimatedRowCount);
        a_out.clearLimit();
    }


    /**
     * 获取插入页
     */
    @Override
    public BasePage getPageForInsert(BaseContainerHandle handle, int flag) {
        BasePage basePage = null;
        boolean getLastInserted = (flag & BaseContainerHandle.GET_PAGE_UNFILLED) == 0;
        if (getLastInserted) {
            long localLastInsertedPage = getLastInsertedPage();
            //如果最后一页不是无效页
            if (localLastInsertedPage != BaseContainerHandle.INVALID_PAGE_NUMBER) {
                basePage = getInsertablePage(handle, localLastInsertedPage, false, false);
                if (basePage == null) {
                    localLastInsertedPage = getLastInsertedPage();
                    basePage = getInsertablePage(handle, localLastInsertedPage, true, false);
                }

            }
            if (basePage == null) {
                if (localLastInsertedPage == getLastUnfilledPage()) {
                    setLastUnfilledPage(BaseContainerHandle.INVALID_PAGE_NUMBER);
                }
                if (localLastInsertedPage == getLastInsertedPage()) {
                    setLastInsertedPage(BaseContainerHandle.INVALID_PAGE_NUMBER);
                }
            }
        } else {
            long localLastUnfilledPage = getLastUnfilledPage();

            if (localLastUnfilledPage == BaseContainerHandle.INVALID_PAGE_NUMBER || localLastUnfilledPage == getLastInsertedPage()) {
                localLastUnfilledPage = getUnfilledPageNumber(handle, localLastUnfilledPage);
            }
            if (localLastUnfilledPage != BaseContainerHandle.INVALID_PAGE_NUMBER) {
                basePage = getInsertablePage(handle, localLastUnfilledPage, true, false);
                if (basePage == null) {
                    localLastUnfilledPage = getUnfilledPageNumber(handle, localLastUnfilledPage);
                    if (localLastUnfilledPage != BaseContainerHandle.INVALID_PAGE_NUMBER) {
                        basePage = getInsertablePage(handle, localLastUnfilledPage, true, false);
                    }
                }
            }
            if (basePage != null) {
                setLastUnfilledPage(localLastUnfilledPage);
                setLastInsertedPage(localLastUnfilledPage);
            }
        }
        return basePage;
    }

    private synchronized long getLastUnfilledPage() {
        return lastUnfilledPage;
    }

    private synchronized void setLastUnfilledPage(long val) {
        lastUnfilledPage = val;
    }

    private long getUnfilledPageNumber(BaseContainerHandle handle, long pagenum) {
        synchronized (allocCache) {
            return allocCache.getUnfilledPageNumber(handle, firstAllocPageNumber, pagenum);
        }
    }

    /**
     * 根据容器句柄和页号获取相应的插入页
     *
     * @param handle     容器对应的句柄
     * @param pageNumber 页号
     */
    private BasePage getInsertablePage(BaseContainerHandle handle, long pageNumber, boolean wait, boolean overflowOK) {
        if (pageNumber == BaseContainerHandle.INVALID_PAGE_NUMBER) {
            return null;
        }
        BasePage insertPage = getUserPage(handle, pageNumber, overflowOK);
        return insertPage;
    }


    //
    private long lastInsertedPage[];
    private int lastInsertedPage_index;

    /**
     * 初始化最后插入的页
     */
    private synchronized void initializeLastInsertedPage(int size) {
        lastInsertedPage = new long[size];
        for (int i = lastInsertedPage.length - 1; i >= 0; i--) {
            lastInsertedPage[i] = BaseContainerHandle.INVALID_PAGE_NUMBER;
        }
        lastInsertedPage_index = 0;
    }

    /**
     * 获取最后的插入页
     * 采用的是轮训的方式
     */
    private synchronized long getLastInsertedPage() {
        if (lastInsertedPage.length == 1) {
            return lastInsertedPage[0];
        } else {
            long ret = lastInsertedPage[lastInsertedPage_index++];
            if (lastInsertedPage_index > (lastInsertedPage.length - 1)) {
                lastInsertedPage_index = 0;
            }
            return ret;
        }
    }

    private long lastAllocatedPage;

    /**
     * 不断尝试找到一个分配页
     * 获取下一个页的页号
     * 获取最后一个页号
     * 根据页号和容器标识创建页面
     * 检测时候是重用的页面
     * 构建页面创建参数
     * 创建新页
     */
    protected BasePage newPage(BaseContainerHandle userHandle, Transaction ntt, BaseContainerHandle allocHandle, boolean isOverflow) {
        PageKey pageKey;
        AllocPage allocPage;
        BasePage basePage;
        boolean retry;
        /**最后的页号*/
        long lastPage;
        long startSearch = lastAllocatedPage;
        long pageNumber = BaseContainerHandle.INVALID_PAGE_NUMBER;
        boolean reuse;
        try {
            do {
                retry = false;
                synchronized (allocCache) {
                    allocPage = findAllocPageForAdd(allocHandle, ntt, startSearch);
                    allocCache.invalidate(allocPage, allocPage.getPageNumber());
                    pageNumber = allocPage.nextFreePageNumber(startSearch);
                    lastPage = allocPage.getLastPagenum();
                    //当前获取页号小于区的最后页
                    reuse = pageNumber <= lastPage;
                    pageKey = new PageKey(identity, pageNumber);
                    lastAllocatedPage = pageNumber;

                    PageCreationArgs createPageArgs = new PageCreationArgs(StoredPage.FORMAT_NUMBER, CachedPage.WRITE_SYNC, pageSize, spareSpace, minimumRecordSize, 0);
                    //获取页面偏移位置
                    long pageOffset = pageNumber * pageSize;
                    basePage = initPage(allocHandle, pageKey, createPageArgs, pageOffset, reuse, isOverflow);

                    //添加当前分配页
                    allocPage.addPage(this, pageNumber, ntt, userHandle);
                    allocPage.setDirty();

                }
            } while (retry == true);

            if (!isOverflow && basePage != null) {
                setLastInsertedPage(pageNumber);
            }
            return basePage;
        } finally {
            //TODO:页面分配失败 暂时什么都不做
        }
    }

    /**
     * 获取分配页,如果分配页不存在创建一个分配页
     */
    private AllocPage findAllocPageForAdd(BaseContainerHandle allocHandle, Transaction ntt, long lastAllocatedPage) {
        AllocPage allocPage = null;
        try {
            /**
             * 如果当前区不存在分配页,创建分配页
             *    如果存在分配页则获取分配页
             * */
            if (firstAllocPageNumber == BaseContainerHandle.INVALID_PAGE_NUMBER) {
                allocPage = makeAllocPage(ntt, allocHandle, FIRST_ALLOC_PAGE_NUMBER, FIRST_ALLOC_PAGE_OFFSET, CONTAINER_INFO_SIZE);
            } else {
                allocPage = (AllocPage) allocHandle.getAllocPage(firstAllocPageNumber);
            }
        } finally {

        }
        return allocPage;
    }

    /**
     * 创建一个分配页
     */
    private AllocPage makeAllocPage(Transaction ntt, BaseContainerHandle handle, long pageNumber, long pageOffset, int containerInfoSize) {
        PageCreationArgs createAllocPageArgs = new PageCreationArgs(
                AllocPage.FORMAT_NUMBER, CachedPage.WRITE_SYNC,
                pageSize, 0, minimumRecordSize, containerInfoSize);

        if (pageNumber == FIRST_ALLOC_PAGE_NUMBER) {
            firstAllocPageNumber = pageNumber;
            firstAllocPageOffset = pageOffset;
        }
        //创建一个页面Key 根据容器和页号
        PageKey pkey = new PageKey(identity, pageNumber);
        return (AllocPage) initPage(handle, pkey, createAllocPageArgs, pageOffset, false, false);
    }

    /**
     * 初始化一个页面
     *
     * @return 返回一个已经被初始化的页面
     */
    protected BasePage initPage(BaseContainerHandle allochandle, PageKey pkey, PageCreationArgs createArgs, long pageOffset, boolean reuse, boolean overflow) {
        BasePage page = null;
        boolean releasePage = true;
        try {
            if (reuse) {
                page = (BasePage) pageCache.find(pkey);
                if (page == null) {
                    throw new RuntimeException("Cannot find page to reuse." + pkey);
                }
            } else {
                page = (BasePage) pageCache.create(pkey, createArgs);
            }
            page = latchPage(allochandle, page, true);
            releasePage = false;
            int initPageFlag = 0;
            if (reuse) {
                initPageFlag |= BasePage.INIT_PAGE_REUSE;
            }
            if (overflow) {
                initPageFlag |= BasePage.INIT_PAGE_OVERFLOW;
            }
            if (reuse && isReusableRecordId()) {
                initPageFlag |= BasePage.INIT_PAGE_REUSE_RECORDID;
            }
            page.initPage(initPageFlag, pageOffset);
        } finally {
            if (releasePage && page != null) {
                // release the new page from cache if it errors
                // out before the exclusive lock is set
                pageCache.release(page);
                page = null;
            }
        }
        return page;

    }

    public Cacheable setIdentity(Object key) {
        ContainerKey newIdentity = (ContainerKey) key;
        return setIdent(newIdentity);
    }

    protected Cacheable setIdent(ContainerKey newIdentity) {
        boolean ok = openContainer(newIdentity);
        initializeLastInsertedPage(1);
        if (ok) {
            fillInIdentity(newIdentity);
            return this;
        } else {
            return null;
        }
    }

    abstract boolean openContainer(ContainerKey newIdentity);


    public Cacheable createIdentity(Object key, Object createParameter) {
        ContainerKey containerKey = (ContainerKey) key;
        return createIdent(containerKey, createParameter);
    }

    protected BasePage getAllocPage(long pageNumber) {
        PageKey pageSearch = new PageKey(identity, pageNumber);
        BasePage page = (BasePage) pageCache.find(pageSearch);
        if (!(page instanceof AllocPage)) {
            throw new RuntimeException("trying to get a user page as an alloc page " + getIdentity() + pageNumber);
        }
        return page;
    }

    /**
     * 容器中写入对应的页
     */
    protected abstract void writePage(long pageNumber, byte[] pageData, boolean syncPage) throws IOException;

    protected int getPageSize() {
        return pageSize;
    }

    /**
     * 读取页号中的数据到PageData
     */
    protected abstract void readPage(long pageNumber, byte[] pageData) throws IOException;

    public boolean isDirty() {
        synchronized (this) {
            return isDirty;
        }
    }

    @Override
    public BasePage getFirstHeadPage(BaseContainerHandle handle, boolean wait) {
        return getNextHeadPage(handle, BaseContainerHandle.FIRST_PAGE_NUMBER - 1, wait);
    }

    public BasePage getNextHeadPage(BaseContainerHandle handle, long pageNumber, boolean wait) {
        long nextNumber;
        while (true) {
            synchronized (allocCache) {
                nextNumber = allocCache.getNextValidPage(handle, pageNumber, firstAllocPageNumber);
            }

            if (nextNumber == BaseContainerHandle.INVALID_PAGE_NUMBER)
                return null;

            BasePage p = getUserPage(handle, nextNumber, false);
            if (p != null)
                return p;
            pageNumber = nextNumber;
        }
    }
}
