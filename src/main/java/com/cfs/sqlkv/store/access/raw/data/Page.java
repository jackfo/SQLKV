package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.btree.ControlRow;

import com.cfs.sqlkv.io.DynamicByteArrayOutputStream;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.log.LogInstant;

import java.io.IOException;
import java.io.ObjectInput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public interface Page {

    static final byte INSERT_INITIAL = (byte) 0x00;    // init the flag
    static final byte INSERT_DEFAULT = (byte) 0x01;    // default flag
    static final byte INSERT_UNDO_WITH_PURGE = (byte) 0x02;    // purge row on undo
    static final byte INSERT_CONDITIONAL = (byte) 0x04;    // conditional
    static final byte INSERT_OVERFLOW = (byte) 0x08;    // insert with
    static final byte INSERT_FOR_SPLIT = (byte) 0x10;    // rawStoreFactory only

    /**
     * 第一个槽位
     */
    public static final int FIRST_SLOT_NUMBER = 0;

    /**
     * 不合法的槽位
     */
    public static final int INVALID_SLOT_NUMBER = -1;

    public void unlatch();

    /**
     * 插入记录在指定的槽位
     *
     * @param slot         指定的槽位
     * @param row          数据的行版本
     * @param undo         if logical undo may be necessary, a function pointer
     *                     to the access code where the logical undo logic
     *                     resides. Null if logical undo is not necessary.
     * @param validColumns a bit map of which columns in the row is valid.
     *                     ValidColumns will not be changed by RawStoreFactory.
     * @param insertFlag   if INSERT_UNDO_WITH_PURGE set, then the undo of this
     *                     insert will purge the row rather than mark it as
     *                     deleted, which is the default bahavior for
     *                     insertAtSlot and insert.
     */
    RecordId insertAtSlot(int slot, Object[] row, FormatableBitSet validColumns, LogicalUndo undo, byte insertFlag, int overflowThreshold)  ;

    /**
     * 插入一条记录到页面
     * <p>
     * 锁策略
     * <p>
     * lockRecordForWrite()==》openContainer()==》inserted
     *
     * @param row          数据列的版本
     * @param validColumns a bit map of which columns in the row is valid.
     *                     ValidColumns will not be changed by RawStoreFactory.
     * @param insertFlag   see values for insertFlag below.
     * @return A RecordHandle representing the new record.
     * @  if the container was not opened in update
     *                           mode, or if the row cannot fit on the page, or if the row is null
     **/
    RecordId insert(Object[] row, FormatableBitSet validColumns, byte insertFlag, int overflowThreshold)  ;

    int getSlotNumber(RecordId handle)  ;

    /**
     * 返回当前页的记录数,返回值包括已经删除的页
     */
    public int recordCount()  ;

    /**
     * 获取位于传入插槽中的记录
     *
     * @param rh           行对应的记录
     * @param slot         槽号
     * @param row          将记录中的信息装填到行
     * @param fetchDesc    记录需要获取行的信息
     * @param ignoreDelete
     */
    public RecordId fetchFromSlot(RecordId rh, int slot, Object[] row, FetchDescriptor fetchDesc, boolean ignoreDelete)  ;

    public int logRow(
            int slot,
            boolean forInsert,
            int recordId,
            Object[] row,
            FormatableBitSet validColumns,
            DynamicByteArrayOutputStream out,
            int startColumn,
            byte insertFlag,
            int realStartColumn,
            int realSpaceOnPage,
            int overflowThreshold) throws IOException;

    public long getPageNumber();

    /**
     * 获取指定槽位的记录标识
     */
    public RecordId getRecordIdAtSlot(int slot)  ;

    public boolean isDeletedAtSlot(int slot)  ;

    public RecordId deleteAtSlot(int slot, boolean delete)  ;

    public RecordId updateFieldAtSlot(int slot, int fieldId, Object newValue)  ;


    public int nonDeletedRecordCount()  ;

    /**
     * 检查是否有空间插入记录
     */
    boolean spaceForInsert(Object[] row, FormatableBitSet validColumns, int overflowThreshold)  ;

    public void copyAndPurge(Page destPage, int src_slot, int num_rows, int dest_slot);

    public RecordId updateAtSlot(int slot, Object[] row, FormatableBitSet validColumns);

    public void setRepositionNeeded();

    public void purgeAtSlot(int slot, int numpurges, boolean needDataLogged)  ;

    public RecordId fetchFieldFromSlot(int slot, int fieldId, Object column)  ;

    public void setControlRow(ControlRow controlRow);
    public ControlRow getControlRow();
}
