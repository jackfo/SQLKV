package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public interface Page {

    /**第一个槽位*/
    public static final int FIRST_SLOT_NUMBER   = 0;

    /**不合法的槽位*/
    public static final int INVALID_SLOT_NUMBER = -1;

    public static final byte INSERT_OVERFLOW        = (byte) 0x08;
    public void unlatch();

    /**
     * 插入记录在指定的槽位
     * @param slot  指定的槽位
     * @param row   数据的行版本
     * @param undo          if logical undo may be necessary, a function pointer
     *                      to the access code where the logical undo logic
     *                      resides. Null if logical undo is not necessary.
     * @param validColumns  a bit map of which columns in the row is valid.
     *                      ValidColumns will not be changed by RawStore.
     * @param insertFlag    if INSERT_UNDO_WITH_PURGE set, then the undo of this
     *                      insert will purge the row rather than mark it as
     *                      deleted, which is the default bahavior for
     *                      insertAtSlot and insert.
     * */
    RecordHandle insertAtSlot(int slot,Object[] row, FormatableBitSet validColumns, LogicalUndo undo, byte insertFlag, int overflowThreshold) throws StandardException;
}
