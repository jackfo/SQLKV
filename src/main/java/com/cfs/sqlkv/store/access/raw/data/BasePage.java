package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.btree.ControlRow;

import com.cfs.sqlkv.io.DynamicByteArrayOutputStream;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.io.TypedFormat;
import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.service.monitor.SQLKVObserver;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.store.access.raw.log.LogInstant;
import com.cfs.sqlkv.transaction.Transaction;

import java.io.IOException;
import java.io.ObjectInput;

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

    /**
     * 在中止期间嵌套保持嵌套的次数
     */
    private int nestedLatch;

    private StoredRecordHeader[] headers;

    /**
     * 初始后的相关状态
     */
    public static final int INIT_PAGE_REUSE = 0x1;
    public static final int INIT_PAGE_OVERFLOW = 0x2;
    public static final int INIT_PAGE_REUSE_RECORDID = 0x4;

    /**
     * 当前页是否
     */
    protected boolean inClean;

    void setExclusive(BaseContainerHandle baseContainerHandle) {
        //根据
        Transaction transaction = baseContainerHandle.getTransaction();
        synchronized (this) {
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
            while (owner != null) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }
            }

            preLatch(baseContainerHandle);


            while (inClean) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }
            }

            preLatch = false;
        }
    }

    /**
     * 初始化存储记录头
     */
    protected void initializeHeaders(int numRecords) {
        headers = new StoredRecordHeader[numRecords];
    }

    boolean setExclusiveNoWait(BaseContainerHandle requester) {

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
     * 抓住此页之前的操作
     */
    protected boolean preLatch;

    private void preLatch(BaseContainerHandle baseContainerHandle) {
        owner = baseContainerHandle;
        baseContainerHandle.addObserver(this);
        preLatch = true;
    }

    @Override
    public void update(SQLKVObservable observable, Object extraInfo) {
        releaseExclusive();
    }

    /**
     * 释放当前页
     */
    protected synchronized void releaseExclusive() {

    }

    /**
     * 如果当前页面是溢出页面返回真,如果不是返回false
     * 对于没有溢出页面的返回false
     */
    public abstract boolean isOverflowPage();

    private byte pageStatus;

    public byte getPageStatus() {
        return pageStatus;
    }

    @Override
    public void unlatch() {

    }

    /**
     * 数据插入到对应槽位
     *
     * @pran slot 槽位
     * @pram row 插入行对象
     * @pram validColumns 校验行
     */
    @Override
    public RecordId insertAtSlot(int slot, Object[] row, FormatableBitSet validColumns, LogicalUndo undo, byte insertFlag, int overflowThreshold) {
        if (slot < FIRST_SLOT_NUMBER || slot > recordCount) {
            throw new RuntimeException(String.format("data slot is not on page,slot is %s", slot));
        }
        return insertNoOverFlow(slot, row);
    }

    /**
     * 插入正常页面
     *
     * @param slot 对应的槽位
     * @param row  具体的行记录
     */
    protected RecordId insertNoOverFlow(int slot, Object[] row) {

        //创建记录标识,将其与对应页面的槽位关联起来
        int record_id = newRecordIdAndBump();
        RecordId record = new RecordId(getPageId(), record_id, slot);
        //通过容器句柄获取行为,将数据插入到对应的槽位
        PageActions pageActions = owner.getActionSet();
        pageActions.actionInsert(this, slot, record_id, row);
        return record;
    }

    /**
     * 存储记录
     */
    public abstract void storeRecord(int slot, boolean forInsert, ObjectInput in) throws IOException;


    private long pageVersion = 0;

    public final long getPageVersion() {
        return pageVersion;
    }

    /**
     * 初始化页面
     */
    public void initPage(int initFlag, long pageOffset) {
        //判断当前页是否是溢出页
        boolean overflowPage = (initFlag & BasePage.INIT_PAGE_OVERFLOW) != 0;
        //判断是否是重用页
        boolean reuse = (initFlag & BasePage.INIT_PAGE_REUSE) != 0;
        int nextRecordId;
        if ((initFlag & BasePage.INIT_PAGE_REUSE_RECORDID) == 0) {
            nextRecordId = newRecordId();
        } else {
            nextRecordId = RecordId.FIRST_RECORD_ID;
        }
        initPage(BasePage.VALID_PAGE, nextRecordId, overflowPage, reuse);
        setPageStatus(BasePage.VALID_PAGE);
    }


    protected void cleanPageForReuse() {
        recordCount = 0;
    }

    public abstract void initPage(byte status, int recordId, boolean overflow, boolean reuse);

    public abstract int newRecordId();

    /**
     * 设置页面状态
     */
    public void setPageStatus(byte status) {
        pageStatus = status;
    }

    public void initPage(LogInstant instant, byte status, int recordId, boolean overflow, boolean reuse) {
        //日志行为记录
        if (reuse) {
//            cleanPage();
//            super.cleanPageForReuse();
        }
    }


    /**
     *
     */
    private void writeExtent(int offset) throws IOException {

    }


    protected void initialize() {
        identity = null;
        recordCount = 0;
    }

    private int recordCount;

    /**
     * 初始化非删除的数据的记录行
     * 1.检测页状态 如果为真 则返回0
     * 2.获取删除记录行的数据
     */
    protected int internalNonDeletedRecordCount() {
        if (pageStatus != VALID_PAGE) {
            return 0;
        }
        int deletedCount = internalDeletedRecordCount();
        if (deletedCount == -1) {
            int count = 0;
            int maxSlot = recordCount;
            for (int slot = FIRST_SLOT_NUMBER; slot < maxSlot; slot++) {
                if (!isDeletedOnPage(slot)) {
                    count++;
                }
            }
            return count;
        } else {
            return recordCount - deletedCount;
        }
    }

    /**
     * 获取删除行记录数
     */
    protected abstract int internalDeletedRecordCount();

    // no need to check for slot on page, call already checked
    protected final boolean isDeletedOnPage(int slot) {
        return getHeaderAtSlot(slot).isDeleted();
    }

    public final StoredRecordHeader getHeaderAtSlot(int slot) {
        if (slot < headers.length) {
            StoredRecordHeader rh = headers[slot];
            return ((rh != null) ? rh : recordHeaderOnDemand(slot));
        } else {
            return recordHeaderOnDemand(slot);
        }
    }

    /**
     * 给指定的slot创建记录头
     * 创建一个新的记录头对象，对其进行初始化，然后将其添加到此页面上的缓存记录头数组中
     *
     * @return 返回当前创建的记录头
     */
    public abstract StoredRecordHeader recordHeaderOnDemand(int slot);

    public abstract boolean allowInsert();

    public final RecordId insert(Object[] row, FormatableBitSet validColumns, byte insertFlag, int overflowThreshold) {
        if (((insertFlag & Page.INSERT_DEFAULT) == Page.INSERT_DEFAULT)) {
            return (insertAtSlot(recordCount, row, validColumns, null, insertFlag, overflowThreshold));
        } else {
            return (insertAllowOverflow(recordCount, row, validColumns, 0, insertFlag, overflowThreshold, null));
        }
    }

    public RecordId insertAllowOverflow(int slot, Object[] row, FormatableBitSet validColumns, int startColumn, byte insertFlag, int overflowThreshold, RecordId nextPortionHandle) {
        BasePage curPage = this;
        if (!curPage.allowInsert()) {
            return null;
        }
        RecordId record;
        int recordId = curPage.newRecordIdAndBump();
        record = new RecordId(curPage.getPageId(), recordId, slot);
        owner.getActionSet().actionInsert(curPage, slot, recordId, row);
        return record;
    }

    /**
     * Create a new record identifier, and bump to next recordid.
     * 创建一个记录的标识`
     */
    public abstract int newRecordIdAndBump();

    public final PageKey getPageId() {
        return identity;
    }

    /**
     * 将从指定low槽位向后移动一位,之后会将新的存储记录头添加进去
     * 目的抛弃数组中的“last”条目,会导致记录标头高速缓存未命中
     */
    protected StoredRecordHeader shiftUp(int low) {
        if (low < headers.length) {
            System.arraycopy(headers, low, headers, low + 1, headers.length - (low + 1));
            headers[low] = null;
        }
        return (null);
    }

    protected final void setHeaderAtSlot(int slot, StoredRecordHeader rh) {

        if (slot < headers.length) {
            if (rh != null) {
                headers[slot] = rh;
            }
        } else {
            StoredRecordHeader[] new_headers = new StoredRecordHeader[slot + 1];
            System.arraycopy(headers, 0, new_headers, 0, headers.length);
            headers = new_headers;
            headers[slot] = rh;
        }
    }

    protected final void bumpRecordCount(int number) {
        recordCount += number;
    }

    /**
     * 获取位于传入插槽中的记录
     *
     * @param rh           行对应的记录
     * @param slot         槽号
     * @param row          将记录中的信息装填到行
     * @param fetchDesc
     * @param ignoreDelete
     */
    @Override
    public RecordId fetchFromSlot(RecordId rh, int slot, Object[] row, FetchDescriptor fetchDesc, boolean ignoreDelete) {
        //获取存储行的头记录
        StoredRecordHeader recordHeader = getHeaderAtSlot(slot);
        //获取记录为空,获取页面对应槽的记录
        if (rh == null) {
            rh = recordHeader.getRecordId(getPageId(), slot);
        }
        //如果忽略掉已经删除的记录,则直接返回空
        if (!ignoreDelete && recordHeader.isDeleted()) {
            return null;
        }

        return restoreRecordFromSlot(slot, row, fetchDesc, rh, recordHeader, true) ? rh : null;
    }


    public int findRecordById(int recordId, int slotHint) {

        if (slotHint == FIRST_SLOT_NUMBER) {
            slotHint = recordId - RecordId.FIRST_RECORD_ID;
        }

        int maxSlot = recordCount();

        if ((slotHint > FIRST_SLOT_NUMBER) && (slotHint < maxSlot) && (recordId == getHeaderAtSlot(slotHint).getId())) {
            return (slotHint);
        } else {
            for (int slot = FIRST_SLOT_NUMBER; slot < maxSlot; slot++) {
                if (recordId == getHeaderAtSlot(slot).getId()) {
                    return slot;
                }
            }
        }

        return -1;
    }

    /**
     * 将给定插槽中的记录读入给定行
     */
    protected abstract boolean restoreRecordFromSlot(int slot, Object[] row, FetchDescriptor fetchDesc, RecordId recordId, StoredRecordHeader recordHeader, boolean isHeadRow);

    /**
     * 获取当前页的页号
     */
    public final long getPageNumber() {
        return identity.getPageNumber();
    }

    protected abstract void writePage(PageKey id);

    public void clearIdentity() {
        identity = null;
    }

    public Object getIdentity() {
        return identity;
    }

    public int recordCount() {
        return recordCount;
    }

    public RecordId getRecordIdAtSlot(int slot) {
        StoredRecordHeader storedRecordHeader = getHeaderAtSlot(slot);
        return storedRecordHeader.getRecordId(getPageId(), slot);
    }

    public boolean isDeletedAtSlot(int slot) {
        if (slot >= FIRST_SLOT_NUMBER && slot < recordCount) {
            return isDeletedOnPage(slot);
        }
        throw new RuntimeException("slot not on page, slot is" + slot);
    }

    public RecordId deleteAtSlot(int slot, boolean delete) {

        if (delete) {
            if (isDeletedAtSlot(slot)) {
                throw new RuntimeException("An attempt was made to update a deleted record");
            }

        } else {
            if (!isDeletedAtSlot(slot)) {
                throw new RuntimeException("An attempt was made to undelete a record that is not deleted");
            }
        }

        Transaction t = owner.getTransaction();
        RecordId handle = getRecordIdAtSlot(slot);
        owner.getActionSet().actionDelete(t, this, slot, handle.getId(), delete);
        return handle;
    }

    public RecordId updateFieldAtSlot(int slot, int fieldId, Object newValue) {
        Transaction t = owner.getTransaction();
        RecordId handle = getRecordIdAtSlot(slot);
        owner.getActionSet().actionUpdateField(t, this, slot, handle.getId(), fieldId, newValue);
        return handle;
    }

    public int nonDeletedRecordCount() {
        return internalNonDeletedRecordCount();
    }

    public abstract void setDeleteStatus(LogInstant instant, int slot, boolean delete) throws IOException;

    public int setDeleteStatus(int slot, boolean delete) throws IOException {
        StoredRecordHeader storedRecordHeader = getHeaderAtSlot(slot);
        return storedRecordHeader.setDeleted(delete);
    }

    public abstract void logColumn(int slot, int fieldId, Object column, DynamicByteArrayOutputStream out, int overflowThreshold) throws IOException;


    public abstract void storeField(LogInstant instant, int slot, int fieldId, ObjectInput in) throws IOException;


    public void copyAndPurge(Page destPage, int src_slot, int num_rows, int dest_slot) {

        if (num_rows <= 0) {
            throw new RuntimeException("no data row copy");
        }
        BasePage dpage = (BasePage) destPage;

        //获取当前页标识
        PageKey pageId = getPageId();
        if (!pageId.getContainerId().equals(dpage.getPageId().getContainerId())) {
            throw new RuntimeException("data come from different container");
        }

        int[] recordIds = new int[num_rows];

        Transaction transaction = owner.getTransaction();

        for (int i = 0; i < num_rows; i++) {
            RecordId handle = getRecordIdAtSlot(src_slot + i);
            recordIds[i] = getHeaderAtSlot(src_slot + i).getId();
        }
        dpage.copyInto(this, src_slot, num_rows, dest_slot);


    }


    private void copyInto(BasePage srcPage, int src_slot, int num_rows, int dest_slot) {
        if ((dest_slot < 0) || dest_slot > recordCount) {
            throw new RuntimeException("DATA_SLOT_NOT_ON_PAGE");
        }
        Transaction t = owner.getTransaction();
        int[] recordIds = new int[num_rows];
        PageKey pageId = getPageId();
        for (int i = 0; i < num_rows; i++) {
            if (i == 0) {
                recordIds[i] = newRecordId();
            } else {
                recordIds[i] = newRecordId(recordIds[i - 1]);
            }
            RecordId handle = new RecordId(pageId, recordIds[i], i);
        }
        owner.getActionSet().actionCopyRows(t, this, srcPage, dest_slot, num_rows, src_slot, recordIds);
    }

    protected abstract int newRecordId(int recordId);

    public final RecordId updateAtSlot(int slot, Object[] row, FormatableBitSet validColumns) {
        RecordId recordId = getRecordIdAtSlot(slot);
        Transaction transaction = owner.getTransaction();
        doUpdateAtSlot(transaction, slot, recordId.getId(), row, validColumns);
        return recordId;
    }

    private long repositionNeededAfterVersion;

    public void setRepositionNeeded() {
        repositionNeededAfterVersion = getPageVersion();
    }

    public abstract void doUpdateAtSlot(Transaction transaction, int slot, int id, Object[] row, FormatableBitSet validColumns);


    public void purgeAtSlot(int slot, int numpurges, boolean needDataLogged) {
        if (numpurges <= 0) {
            return;
        }
        if ((slot < 0) || ((slot + numpurges) > recordCount)) {
            throw new RuntimeException("DATA_SLOT_NOT_ON_PAGE");
        }
        Transaction transaction = owner.getTransaction();
        int[] recordIds = new int[numpurges];
        PageKey pageId = getPageId();
        for (int i = 0; i < numpurges; i++) {
            recordIds[i] = getHeaderAtSlot(slot + i).getId();
            RecordId recordId = getRecordIdAtSlot(slot);
            RecordId headRowHandle = getHeaderAtSlot(slot + i).getRecordId(pageId, slot + i);
            purgeRowPieces(transaction, slot + i, headRowHandle, needDataLogged);
        }

        owner.getActionSet().actionPurge(transaction, this, slot, numpurges, recordIds, needDataLogged);
    }

    protected abstract void purgeRowPieces(Transaction t, int slot, RecordId headRowHandle, boolean needDataLogged);


    public final RecordId fetchFieldFromSlot(int slot, int fieldId, Object column) {
        Object[] row = new Object[fieldId + 1];
        row[fieldId] = column;
        FetchDescriptor fetchDesc = new FetchDescriptor(fieldId + 1, fieldId);
        return fetchFromSlot(null, slot, row, fetchDesc, true);
    }


    private ControlRow controlRow;

    public void setControlRow(ControlRow controlRow) {
        if (this.controlRow != null) {
            this.controlRow.controlRowInvalidated();
        }
        this.controlRow = controlRow;
    }

    public ControlRow getControlRow() {
        return this.controlRow;
    }

}
