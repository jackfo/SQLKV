package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.catalog.TypedFormat;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.FormatableBitSet;
import com.cfs.sqlkv.io.StoredFormatIds;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.service.monitor.SQLKVObserver;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.store.access.raw.log.LogInstant;
import com.cfs.sqlkv.transaction.Transaction;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public abstract class BasePage implements Page, SQLKVObserver, TypedFormat, Cacheable {

    public static final byte VALID_PAGE = 1;
    public static final byte INVALID_PAGE = 2;

    protected BaseContainerHandle owner;

    protected PageKey identity;

    /**在中止期间嵌套保持嵌套的次数*/
    private int nestedLatch;

    /**
     * 初始后的相关状态
     * */
    public static final int INIT_PAGE_REUSE = 0x1;
    public static final int INIT_PAGE_OVERFLOW = 0x2;
    public static final int INIT_PAGE_REUSE_RECORDID = 0x4;

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

    @Override
    public RecordHandle insertAtSlot(int slot, Object[] row, FormatableBitSet validColumns, LogicalUndo undo, byte insertFlag, int overflowThreshold) throws StandardException {
        return null;
    }

    /**
     * 初始化页面
     * */
    public void initPage(int initFlag, long pageOffset) throws StandardException{
        Transaction transaction = owner.getTransaction();
        owner.getActionSet().actionInitPage(transaction, this, initFlag, getTypeFormatId(), pageOffset);
    }

    public void initPage(LogInstant instant, byte status, int recordId, boolean overflow, boolean reuse){
        //日志行为记录
        if (reuse) {
//            cleanPage();
//            super.cleanPageForReuse();
        }
    }


    @Override
    public Cacheable createIdentity(Object key, Object createParameter) throws StandardException {
        initialize();
        PageKey newIdentity = (PageKey) key;
        PageCreationArgs createArgs = (PageCreationArgs) createParameter;
        int formatId = createArgs.getFormatId();
        if (formatId == -1) {
            throw new RuntimeException("unknow page format_id");
        }

        //如果格式Id不是AllocPage需要做一个转化
        if (formatId != getTypeFormatId()) {

            //return changeInstanceTo(formatId, newIdentity).createIdentity(key, createParameter);
        }

        return null;
    }

    private CachedPage changeInstanceTo(int fid, PageKey newIdentity) throws StandardException{

    }


    /**
     *
     * */
    private void writeExtent(int offset) throws IOException {
        rawDataOut.setPosition(offset);
        extent.writeExternal(logicalDataOut);
    }


    protected void initialize() {
    }

    private int recordCount;

    /**
     * 初始化非删除的数据的记录行
     * 1.检测页状态 如果为真 则返回0
     * 2.获取删除记录行的数据
     *
     * */
    protected int internalNonDeletedRecordCount(){
        if (pageStatus != VALID_PAGE){
            return 0;
        }
        int deletedCount = internalDeletedRecordCount();
        if (deletedCount == -1) {
            int count = 0;
            int	maxSlot = recordCount;
            for (int slot = FIRST_SLOT_NUMBER ; slot < maxSlot; slot++) {
                if (!isDeletedOnPage(slot)){
                    count++;
                }
            }
            return count;
        }else{
            return recordCount - deletedCount;
        }
    }

    /**
     * 获取删除行记录数
     * */
    protected abstract int internalDeletedRecordCount();

    // no need to check for slot on page, call already checked
    protected final boolean isDeletedOnPage(int slot) {
        return getHeaderAtSlot(slot).isDeleted();
    }

    public final StoredRecordHeader getHeaderAtSlot(int slot) {
        if (slot < headers.length) {
            StoredRecordHeader rh = headers[slot];
            return((rh != null) ? rh : recordHeaderOnDemand(slot));
        } else {
            return recordHeaderOnDemand(slot);
        }
    }

    /**
     * 给指定的slot创建记录头
     * 创建一个新的记录头对象，对其进行初始化，然后将其添加到此页面上的缓存记录头数组中
     *
     * @return 返回当前创建的记录头
     * */
    public abstract StoredRecordHeader recordHeaderOnDemand(int slot);
}
