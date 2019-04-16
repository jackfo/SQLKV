package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.io.DynamicByteArrayOutputStream;
import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;
import com.cfs.sqlkv.transaction.Transaction;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 18:40
 */
public class DirectActions implements PageActions {

    protected DynamicByteArrayOutputStream outBytes;
    protected ArrayInputStream limitIn;

    public DirectActions() {
        outBytes = new DynamicByteArrayOutputStream();
        limitIn = new ArrayInputStream();
    }

    @Override
    public void actionInitPage(Transaction t, BasePage page, int initFlag, int pageFormatId, long pageOffset) {
        //boolean overflowPage = ((initFlag & BasePage.INIT_PAGE_OVERFLOW) != 0);
        //boolean reuse = ((initFlag & BasePage.INIT_PAGE_REUSE) != 0);
        //int nextRecordId = ((initFlag & BasePage.INIT_PAGE_REUSE_RECORDID) == 0) ? page.newRecordId() : RecordId.FIRST_RECORD_ID;
        //page.initPage(null, BasePage.VALID_PAGE, nextRecordId, overflowPage, reuse);

    }

    @Override
    public int actionInsert(Transaction transaction, BasePage page, int slot, int recordId, Object[] row, FormatableBitSet validColumns, LogicalUndo undo, byte insertFlag, int startColumn, boolean isLongColumn, int realStartColumn, DynamicByteArrayOutputStream logBuffer, int realSpaceOnPage, int overflowThreshold) {
        return 0;
    }

    /**
     * 插入行为,将数据插入到对应的行
     */
    @Override
    public int actionInsert(BasePage page, int slot, int recordId, Object[] row) {
        outBytes.reset();
        int startColumn = 0;
        try {
            /**
             * 将数据写入outBytes,之后存储到limitIn,通过limitIn存储到页
             * */
            startColumn = page.logRow(slot, true, recordId, row, null, outBytes, startColumn, (byte) 0, -1, -1, 0);
            limitIn.setData(outBytes.getByteArray());
            limitIn.setPosition(outBytes.getBeginPosition());
            limitIn.setLimit(outBytes.getPosition() - outBytes.getBeginPosition());
            page.storeRecord(slot, true, limitIn);
        } catch (IOException e) {
            throw new RuntimeException("data unexpected exception");
        }
        return startColumn;
    }

    @Override
    public void actionDelete(Transaction transaction, BasePage page, int slot, int recordId, boolean delete) {
        try {
            page.setDeleteStatus(null, slot, delete);
        } catch (IOException e) {
            throw new RuntimeException("DATA_UNEXPECTED_EXCEPTION");
        }
    }

    @Override
    public void actionUpdateField(Transaction transaction, BasePage page, int slot, int recordId, int fieldId, Object newValue) {
        outBytes.reset();
        try {
            page.logColumn(slot, fieldId, newValue, outBytes, 100);
            limitIn.setData(outBytes.getByteArray());
            limitIn.setPosition(outBytes.getBeginPosition());
            limitIn.setLimit(outBytes.getPosition() - outBytes.getBeginPosition());
            page.storeField(null, slot, fieldId, limitIn);
        } catch (IOException ioe) {

            throw new RuntimeException("");
        }
    }

    @Override
    public void actionCopyRows(Transaction transaction, BasePage destPage, BasePage srcPage, int destSlot, int numRows, int srcSlot, int[] recordIds) {

    }

    @Override
    public void actionPurge(Transaction t, BasePage page, int slot, int num_rows, int[] recordIds, boolean logData) {

    }

    @Override
    public int actionUpdate(Transaction transaction, BasePage page, int slot, int recordId, Object[] row, FormatableBitSet validColumns, int realStartColumn, DynamicByteArrayOutputStream logBuffer, int realSpaceOnPage, RecordId headRowHandle) {
        if (logBuffer == null) {
            outBytes.reset();
        } else {
            outBytes = logBuffer;
        }

        try {
            int nextColumn =
                    page.logRow(
                            slot, false, recordId, row, validColumns, outBytes, 0,
                            Page.INSERT_OVERFLOW, realStartColumn,
                            realSpaceOnPage, 100);
            limitIn.setData(outBytes.getByteArray());
            limitIn.setPosition(outBytes.getBeginPosition());
            limitIn.setLimit(outBytes.getPosition() - outBytes.getBeginPosition());
            page.storeRecord( slot, false, limitIn);

            return nextColumn;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
