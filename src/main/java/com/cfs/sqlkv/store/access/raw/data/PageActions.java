package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.io.DynamicByteArrayOutputStream;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;
import com.cfs.sqlkv.transaction.Transaction;

import java.io.IOException;

public interface PageActions {

    public void actionInitPage(Transaction t, BasePage page, int initFlag, int pageFormatId, long pageOffset)  ;

    public int actionInsert(
            Transaction transaction,
            BasePage page,
            int slot,
            int recordId,
            Object[] row,
            FormatableBitSet validColumns,
            LogicalUndo undo,
            byte insertFlag,
            int startColumn,
            boolean isLongColumn,
            int realStartColumn,
            DynamicByteArrayOutputStream logBuffer,
            int realSpaceOnPage,
            int overflowThreshold)
             ;


    /**
     * @param page     需要插入的页面
     * @param slot     对应的槽位
     * @param recordId 记录标识
     * @param row      插入的行记录
     */
    public int actionInsert(BasePage page, int slot, int recordId, Object[] row)  ;


    public void actionDelete(Transaction transaction, BasePage page, int slot, int recordId, boolean delete)  ;

    public void actionUpdateField(Transaction transaction, BasePage page, int slot, int recordId, int fieldId, Object newValue)  ;

    public void actionCopyRows(Transaction transaction, BasePage destPage, BasePage srcPage, int destSlot, int numRows, int srcSlot, int[] recordIds);

    public void actionPurge(Transaction t, BasePage page, int slot, int num_rows, int[] recordIds, boolean logData)  ;

    public int actionUpdate(Transaction transaction,
                            BasePage page,
                            int slot,
                            int recordId,
                            Object[] row,
                            FormatableBitSet validColumns,
                            int realStartColumn,
                            DynamicByteArrayOutputStream logBuffer,
                            int realSpaceOnPage,
                            RecordId headRowHandle)  ;
}
