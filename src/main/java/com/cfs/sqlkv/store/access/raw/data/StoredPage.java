package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.io.*;
import com.cfs.sqlkv.io.DataInputUtil;
import com.cfs.sqlkv.io.DynamicByteArrayOutputStream;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.io.*;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.store.access.raw.log.LogInstant;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.*;
import java.util.Arrays;

/**
 * @author zhengxiaokang
 * @Description +----------+-------------+---+---------+-------------------+-------------+--------+
 * | FormatId | page header | N | N bytes | alloc extend rows | slot offset |checksum|
 * +----------+-------------+---+---------+-------------------+-------------+--------+
 * <p>
 * 创建容器
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public class StoredPage extends CachedPage {


    /**
     * 行存储页
     */
    public static final int FORMAT_NUMBER = StoredFormatIds.RAW_STORE_STORED_PAGE;
    protected static final int COLUMN_NONE = 0;
    protected static final int COLUMN_FIRST = 1;
    protected static final int COLUMN_LONG = 2;
    protected static final int COLUMN_CREATE_NULL = 3;

    /**
     * 行数据输入流
     */
    protected ArrayInputStream rawDataIn;

    protected ArrayOutputStream rawDataOut;
    protected FormatIdOutputStream logicalDataOut;

    /**
     * 页面格式id
     */
    protected static final int PAGE_FORMAT_ID_SIZE = 4;
    protected static final int PAGE_HEADER_OFFSET = PAGE_FORMAT_ID_SIZE;
    /**
     * 是否是溢出页   1
     * 页面状态      1
     * 页面版本      8
     * slot个数     2
     * 下一条记录id  4
     * 删除的记录数  2
     */
    protected static final int PAGE_HEADER_SIZE = 18;

    /**
     * SLOT_SIZE大小记录的是每个嘈位的大小
     * 每一个嘈位条目里面包含记录的偏移量和记录的长度,所以是SLOT_SIZE的两倍
     */
    protected static final int SLOT_SIZE = 2;
    private int slotEntrySize = SLOT_SIZE * 2;

    /**
     * 第一个槽位Entry记录偏移量的位置
     */
    private int slotTableOffsetToFirstEntry;
    /**
     * 第一个嘈位Entry记录首条记录长度的位置
     */
    private int slotTableOffsetToFirstRecordLengthField;

    protected static final int CHECKSUM_SIZE = 8;


    /**
     * 是否是溢出页
     */
    private boolean isOverflowPage;
    /**
     * 槽表正在使用的槽数
     */
    private int slotsInUse;
    /**
     * 下一个记录的标识
     */
    private int nextId;
    /**
     * 在当前页删除的数量
     */
    private int deletedRowCount;

    private boolean headerOutOfDate;


    private int userRowSize;

    /**
     * 初始化存储页
     */
    protected void initialize() {
        super.initialize();
        if (rawDataIn == null) {
            rawDataIn = new ArrayInputStream();
        }
        if (pageData != null) {
            rawDataIn.setData(pageData);
        }

    }


    @Override
    public boolean isOverflowPage() {
        return false;
    }

    @Override
    public void initPage(byte status, int recordId, boolean overflow, boolean reuse) {
        setDirty();
        if (reuse) {
            cleanPage();
            super.cleanPageForReuse();
        }
        headerOutOfDate = true;
        setPageStatus(status);
        isOverflowPage = overflow;
        nextId = recordId;
    }

    @Override
    public int newRecordId() {
        return nextId;
    }

    @Override
    protected int internalDeletedRecordCount() {
        return 0;
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.RAW_STORE_STORED_PAGE;
    }

    /**
     * % of page to keep free for updates.
     * <p>
     * How much of a head page should be reserved as "free" so that the space
     * can be used by update which expands the row without needing to overflow
     * it.  1 means save 1% of the free space for expansion.
     **/
    protected int spareSpace;
    protected int minimumRecordSize;

    /**
     * 合计总空间
     */
    protected int totalSpace;

    /**
     * 剩余空间
     */
    protected int freeSpace = Integer.MIN_VALUE;
    private int firstFreeByte = Integer.MIN_VALUE;

    @Override
    protected void initFromData(FileContainer myContainer, PageKey newIdentity) {
        if (myContainer != null) {
            spareSpace = myContainer.getSpareSpace();
            minimumRecordSize = myContainer.getMinimumRecordSize();
        }
        try {
            readPageHeader();
            initSlotTable(newIdentity);
        } catch (IOException ioe) {
            throw new RuntimeException(String.format("Unexpected exception on in-memory page %s", identity));
        }
    }

    /**
     * 初始化槽表
     */
    private void initSlotTable(PageKey newIdentity) {
        int localSlotsInUse = slotsInUse;
        //初始化头部信息
        initializeHeaders(localSlotsInUse);
        //设置剩余空间,并减去槽表所占用的空间数量
        clearAllSpace();
        freeSpace -= localSlotsInUse * slotEntrySize;
        int lastSlotOnPage = -1;
        int lastRecordOffset = -1;
        try {
            /**
             * 设置页面最后的记录和记录偏移量
             * */
            for (int slot = 0; slot < localSlotsInUse; slot++) {
                //根据槽位获取记录偏移量
                int recordOffset = getRecordOffset(slot);
                if (recordOffset > lastRecordOffset) {
                    lastRecordOffset = recordOffset;
                    lastSlotOnPage = slot;
                }
            }

            /**添加记录数*/
            bumpRecordCount(localSlotsInUse);

            if (lastSlotOnPage != -1) {
                firstFreeByte = lastRecordOffset + getTotalSpace(lastSlotOnPage);
                freeSpace -= firstFreeByte - RECORD_SPACE_OFFSET;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getTotalSpace(int slot) throws IOException {
        rawDataIn.setPosition(getSlotOffset(slot) + SLOT_SIZE);
        return rawDataIn.readUnsignedShort() + rawDataIn.readUnsignedShort();
    }

    /**
     * 从Page数组中读取头
     */
    private void readPageHeader() throws IOException {
        ArrayInputStream lrdi = rawDataIn;
        //从偏移位置为4开始读取
        lrdi.setPosition(PAGE_HEADER_OFFSET);
        isOverflowPage = lrdi.readBoolean();
        setPageStatus(lrdi.readByte());
        setPageVersion(lrdi.readLong());
        slotsInUse = lrdi.readUnsignedShort();
        nextId = lrdi.readInt();
        deletedRowCount = lrdi.readUnsignedShort() - 1;
    }


    private long pageVersion = 0;

    public final void setPageVersion(long v) {
        pageVersion = v;
    }

    @Override
    public StoredRecordHeader recordHeaderOnDemand(int slot) {
        StoredRecordHeader recordHeader = new StoredRecordHeader(pageData, getRecordOffset(slot));
        setHeaderAtSlot(slot, recordHeader);
        return recordHeader;
    }

    /**
     * 在当前方法类,字段的数据将添加到PageData
     */
    private void storeRecordForInsert(int slot, ObjectInput in) throws IOException {
        StoredRecordHeader recordHeader = shiftUp(slot);
        if (recordHeader == null) {
            recordHeader = new StoredRecordHeader();
            setHeaderAtSlot(slot, recordHeader);
        }
        bumpRecordCount(1);
        //从流中读取当前记录头
        recordHeader.read(in);

        if (recordHeader.isDeleted()) {
            deletedRowCount++;
            headerOutOfDate = true;
        }
        if (nextId <= recordHeader.getId()) {
            nextId = recordHeader.getId() + 1;
        }

        int recordOffset = firstFreeByte;
        int offset = recordOffset;

        int numberFields = recordHeader.getNumberFields();

        rawDataOut.setPosition(offset);
        //将数据写入存储头,在这里是写入存储记录标头的状态和标识等
        offset += recordHeader.write(rawDataOut);

        int userData = 0;

        for (int i = 0; i < numberFields; i++) {
            int newFieldStatus = StoredFieldHeader.readStatus(in);
            int newFieldDataLength = StoredFieldHeader.readFieldDataLength(in, newFieldStatus, SLOT_SIZE);
            newFieldStatus = StoredFieldHeader.setFixed(newFieldStatus, false);
            rawDataOut.setPosition(offset);
            offset += StoredFieldHeader.write(rawDataOut, newFieldStatus, newFieldDataLength, SLOT_SIZE);
            //读取数据到pageData中,指定的长度是字段的长度
            if (newFieldDataLength != 0) {
                in.readFully(pageData, offset, newFieldDataLength);
                offset += newFieldDataLength;
                userData += newFieldDataLength;
            }

        }
        int dataWritten = offset - firstFreeByte;
        freeSpace -= dataWritten;
        firstFreeByte += dataWritten;
        int reservedSpace = 0;
        /**
         * 确保我们为记录的用户数据部分保留minimumRecordSize
         * 不包括我们在recordHeader和fieldHeaders上占用的空间
         * */
        if (minimumRecordSize > 0) {
            if (userData < minimumRecordSize) {
                reservedSpace = minimumRecordSize - userData;
                freeSpace -= reservedSpace;
                firstFreeByte += reservedSpace;
            }
        }
        /**
         * 包括行标题，字段标题，用户数据和未使用的保留空间在内的行的总长度必须至少与最坏情况的溢出行指针一样大
         * 这样，总是可以对行块进行扩展更新，在最坏的情况下导致仅使用现有空间将溢出指针放入其他页面上的另一个行段
         * */
        if (isOverflowPage()) {
            int additional_space_needed = StoredRecordHeader.MAX_OVERFLOW_ONLY_REC_SIZE - (dataWritten + reservedSpace);
            if (additional_space_needed > 0) {
                freeSpace -= additional_space_needed;
                firstFreeByte += additional_space_needed;
                reservedSpace += additional_space_needed;
            }
        }

        addSlotEntry(slot, recordOffset, dataWritten, reservedSpace);
        if ((firstFreeByte > getSlotOffset(slot)) || (freeSpace < 0)) {
            throw new RuntimeException(String.format("Unexpected exception on in-memory page %s", getPageId()));
        }

    }


    private void addSlotEntry(int slot, int recordOffset, int recordPortionLength, int reservedSpace) throws IOException {
        int newSlotOffset;
        if (slot < slotsInUse) {
            int startOffset = getSlotOffset(slotsInUse - 1);
            int length = (getSlotOffset(slot) + slotEntrySize) - startOffset;
            newSlotOffset = getSlotOffset(slotsInUse);
            System.arraycopy(pageData, startOffset, pageData, newSlotOffset, length);
        } else {
            newSlotOffset = getSlotOffset(slot);
        }
        freeSpace -= slotEntrySize;
        slotsInUse++;
        headerOutOfDate = true;
        setSlotEntry(slot, recordOffset, recordPortionLength, reservedSpace);
    }


    /**
     * 设置槽位信息 包括记录偏移量和记录长度
     */
    private void setSlotEntry(int slot, int recordOffset, int recordPortionLength, int reservedSpace) throws IOException {
        rawDataOut.setPosition(getSlotOffset(slot));
        logicalDataOut.writeShort(recordOffset);
        logicalDataOut.writeShort(recordPortionLength);
    }

    /**
     * 获取给定插槽条目的页面偏移量
     * 获取插槽条目的页面偏移量，这不是存储在插槽中的记录的偏移量，而是实际插槽的偏移量。
     * <p>
     * 槽表从位于页面最后8个字节的校验和之前的页面末尾的位置向后增长
     *
     * @param slot 要查找的插槽的数组条目
     * @return 给定槽条目的页面偏移量
     **/
    private int getSlotOffset(int slot) {
        return slotTableOffsetToFirstEntry - (slot * slotEntrySize);
    }

    @Override
    public boolean allowInsert() {
        //表示当前页是一个空页
        if (slotsInUse == 0) {
            return true;
        }
        //获取空间
        int spaceAvailable = freeSpace;
        spaceAvailable -= slotEntrySize;
        if ((spaceAvailable < minimumRecordSize) ||
                (spaceAvailable < StoredRecordHeader.MAX_OVERFLOW_ONLY_REC_SIZE)) {
            return false;
        }
        if (((spaceAvailable * 100) / totalSpace) < spareSpace)
            return false;
        return true;
    }

    @Override
    public int newRecordIdAndBump() {
        headerOutOfDate = true;
        return nextId++;
    }

    @Override
    public void storeRecord(int slot, boolean insert, ObjectInput in) throws IOException {
        setDirty();
        if (insert) {
            storeRecordForInsert(slot, in);
        } else {
            storeRecordForUpdate(slot, in);
        }
    }

    private void storeRecordForUpdate(int slot, ObjectInput in) throws IOException {
        StoredRecordHeader recordHeader = getHeaderAtSlot(slot);
        StoredRecordHeader newRecorderHeader = new StoredRecordHeader();
        newRecorderHeader.read(in);
        int oldFieldCount = recordHeader.getNumberFields();
        int newFieldCount = newRecorderHeader.getNumberFields();
        int startField = recordHeader.getFirstField();
        if (newFieldCount < oldFieldCount) {
            int oldDataStartingOffset = getFieldOffset(slot, startField + newFieldCount);
            int deleteLength = getRecordOffset(slot) + getRecordPortionLength(slot) - oldDataStartingOffset;
            updateRecordPortionLength(slot, -(deleteLength), deleteLength);
        }
        int startingOffset = getRecordOffset(slot);
        int newOffset = startingOffset;
        int oldOffset = startingOffset;
        DynamicByteArrayOutputStream newDataToWrite = null;
        rawDataOut.setPosition(newOffset);
        int oldLength = recordHeader.size();
        int newLength = newRecorderHeader.size();
        int unusedSpace = oldLength;
        if (unusedSpace >= newLength) {
            newRecorderHeader.write(rawDataOut);
            newOffset += newLength;
        } else {
            newDataToWrite = new DynamicByteArrayOutputStream(getPageSize());
            newRecorderHeader.write(newDataToWrite);
        }
        oldOffset += oldLength;
        int recordDelta = (newLength - oldLength);
        int oldFieldStatus = 0;
        int oldFieldDataLength = 0;
        int newFieldStatus = 0;
        int newFieldDataLength = 0;

        int oldEndFieldExclusive = startField + oldFieldCount;
        int newEndFieldExclusive = startField + newFieldCount;
        for (int fieldId = startField; fieldId < newEndFieldExclusive; fieldId++) {

            int oldFieldLength = 0;
            if (fieldId < oldEndFieldExclusive) {
                rawDataIn.setPosition(oldOffset);
                oldFieldStatus = StoredFieldHeader.readStatus(rawDataIn);
                oldFieldDataLength = StoredFieldHeader.readFieldDataLength(rawDataIn, oldFieldStatus, SLOT_SIZE);
                oldFieldLength = StoredFieldHeader.size(oldFieldStatus, oldFieldDataLength, SLOT_SIZE)
                        + oldFieldDataLength;
            }

            newFieldStatus = StoredFieldHeader.readStatus(in);
            newFieldDataLength = StoredFieldHeader.readFieldDataLength(in, newFieldStatus, SLOT_SIZE);
            if (StoredFieldHeader.isNonexistent(newFieldStatus) && (fieldId < oldEndFieldExclusive)) {
                if ((newDataToWrite == null) || (newDataToWrite.getUsed() == 0)) {
                    if (newOffset == oldOffset) {
                    } else {
                        System.arraycopy(pageData, oldOffset, pageData, newOffset, oldFieldLength);
                    }
                    newOffset += oldFieldLength;

                } else {
                    // there is data still to be written, just append this field to the
                    // saved data
                    int position = newDataToWrite.getPosition();
                    newDataToWrite.setPosition(position + oldFieldLength);
                    System.arraycopy(pageData, oldOffset, newDataToWrite.getByteArray(), position, oldFieldLength);

                    // attempt to write out some of what we have in the side buffer now.
                    //int copyLength = moveSavedDataToPage(newDataToWrite, unusedSpace, newOffset);
                    //newOffset += copyLength;
                    //unusedSpace -= copyLength;

                }
                oldOffset += oldFieldLength;
                continue;
            }

            newFieldStatus = StoredFieldHeader.setFixed(newFieldStatus, false);

            int newFieldHeaderLength = StoredFieldHeader.size(newFieldStatus, newFieldDataLength, SLOT_SIZE);
            int newFieldLength = newFieldHeaderLength + newFieldDataLength;

            recordDelta += (newFieldLength - oldFieldLength);

            // See if we can write this field now

            oldOffset += oldFieldLength;


            if ((newDataToWrite != null) && (newDataToWrite.getUsed() != 0)) {

                // catch up on the old data if possible
                //int copyLength = moveSavedDataToPage(newDataToWrite, unusedSpace, newOffset);
                //newOffset += copyLength;
                //unusedSpace -= copyLength;
            }

            if (((newDataToWrite == null) || (newDataToWrite.getUsed() == 0))
                    && (unusedSpace >= newFieldHeaderLength)) {

                // can fit the header in
                rawDataOut.setPosition(newOffset);
                newOffset += StoredFieldHeader.write(rawDataOut, newFieldStatus, newFieldDataLength, SLOT_SIZE);
                //unusedSpace -= newFieldHeaderLength;

                if (newFieldDataLength != 0) {

                    // read as much as the field as possible
                    int fieldCopy = unusedSpace >= newFieldDataLength ? newFieldDataLength : unusedSpace;

                    if (fieldCopy != 0) {
                        in.readFully(pageData, newOffset, fieldCopy);

                        newOffset += fieldCopy;
                        //unusedSpace -= fieldCopy;
                    }


                    fieldCopy = newFieldDataLength - fieldCopy;
                    if (fieldCopy != 0) {
                        if (newDataToWrite == null)
                            newDataToWrite = new DynamicByteArrayOutputStream(newFieldLength * 2);


                        int position = newDataToWrite.getPosition();
                        newDataToWrite.setPosition(position + fieldCopy);
                        in.readFully(newDataToWrite.getByteArray(), position, fieldCopy);

                    }
                }
            } else {
                if (newDataToWrite == null)
                    newDataToWrite = new DynamicByteArrayOutputStream(newFieldLength * 2);

                StoredFieldHeader.write(newDataToWrite, newFieldStatus, newFieldDataLength, SLOT_SIZE);

                // save the new field data
                if (newFieldDataLength != 0) {
                    int position = newDataToWrite.getPosition();
                    newDataToWrite.setPosition(position + newFieldDataLength);
                    in.readFully(newDataToWrite.getByteArray(),
                            position, newFieldDataLength);
                }
            }
        }


        if ((newDataToWrite != null) && (newDataToWrite.getUsed() != 0)) {

            // need to shift the later records down ...
            int nextRecordOffset = startingOffset + getTotalSpace(slot);

            int spaceRequiredFromFreeSpace = newDataToWrite.getUsed() - (nextRecordOffset - newOffset);


            if (spaceRequiredFromFreeSpace > freeSpace) {
                throw new RuntimeException("");
            }

            expandPage(nextRecordOffset, spaceRequiredFromFreeSpace);
            unusedSpace += spaceRequiredFromFreeSpace;
            moveSavedDataToPage(newDataToWrite, unusedSpace, newOffset);


        }

        // now reset the length in the slot entry
        updateRecordPortionLength(slot, recordDelta);

        setHeaderAtSlot(slot, newRecorderHeader);

    }

    protected void expandPage(int startOffset, int requiredBytes) throws IOException {
        int totalLength = firstFreeByte - startOffset;
        if (totalLength > 0) {
            System.arraycopy(pageData, startOffset, pageData, startOffset + requiredBytes, totalLength);
            for (int slot = 0; slot < slotsInUse; slot++) {
                int offset = getRecordOffset(slot);
                if (offset >= startOffset) {
                    offset += requiredBytes;
                    setRecordOffset(slot, offset);
                }
            }
        }

        freeSpace -= requiredBytes;
        firstFreeByte += requiredBytes;
    }

    private void setRecordOffset(int slot, int recordOffset) throws IOException {
        rawDataOut.setPosition(getSlotOffset(slot));
        logicalDataOut.writeShort(recordOffset);
    }


    private int moveSavedDataToPage(DynamicByteArrayOutputStream savedData, int unusedSpace, int pageOffset) {
        if (unusedSpace > (savedData.getUsed() / 2)) {
            int copyLength = unusedSpace <= savedData.getUsed() ?
                    unusedSpace : savedData.getUsed();
            System.arraycopy(savedData.getByteArray(), 0,
                    pageData, pageOffset, copyLength);
            savedData.discardLeft(copyLength);

            return copyLength;
        }

        return 0;
    }

    @Override
    protected boolean restoreRecordFromSlot(int slot, Object[] row, FetchDescriptor fetchDesc, RecordId recordId, StoredRecordHeader recordHeader, boolean isHeadRow) {
        try {
            int offset_to_row_data = getRecordOffset(slot) + recordHeader.size();
            ArrayInputStream lrdi = rawDataIn;
            lrdi.setPosition(offset_to_row_data);
            //如果当前记录不是溢出页
            if (!recordHeader.hasOverflow()) {
                //判断当前记录是否是头部记录
                if (isHeadRow) {
                    if (fetchDesc != null && fetchDesc.getQualifierList() != null) {
                        fetchDesc.reset();
                        if (!qualifyRecordFromSlot(row, offset_to_row_data, fetchDesc, recordHeader)) {
                            return (false);
                        } else {
                            lrdi.setPosition(offset_to_row_data);
                        }
                    }
                }
                if (fetchDesc != null) {
                    int fetchLength;
                    if (fetchDesc.getValidColumns() == null) {
                        fetchLength = row.length - 1;
                    } else {
                        fetchLength = fetchDesc.getMaxFetchColumnId();
                    }
                    try {
                        readRecordFromArray(row, fetchLength, fetchDesc.getValidColumnsArray(), null, lrdi, recordHeader, recordId);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        readRecordFromArray(row, row.length - 1, null, null,
                                lrdi, recordHeader, recordId);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 根据槽位获取记录的偏移量
     */
    private int getRecordOffset(int slot) {
        byte[] data = pageData;
        //entry是倒着存放的 获取当前entry偏移量
        int offset = slotTableOffsetToFirstEntry - (slot * slotEntrySize);
        //获取数据记录的偏移量
        return ((data[offset++] & 0xff) << 8) | (data[offset] & 0xff);
    }

    /**
     * 读取记录
     */
    private final boolean readRecordFromArray(Object[] row,
                                              int max_colid,
                                              int[] vCols,
                                              int[] mCols,
                                              ArrayInputStream dataIn,
                                              StoredRecordHeader recordHeader,
                                              RecordId recordToLock) throws IOException, ClassNotFoundException {
        try {
            //获取字段数和首个字段
            int numberFields = recordHeader.getNumberFields();
            int startColumn = recordHeader.getFirstField();
            //如果开始列高于最高列则直接返回真
            if (startColumn > max_colid) {
                return true;
            }
            //页面上最高列
            int highestColumnOnPage = numberFields + startColumn;
            int vColsSize = (vCols == null) ? 0 : vCols.length;
            int offset_to_field_data = dataIn.getPosition();
            for (int columnId = startColumn; columnId <= max_colid; columnId++) {

                //跳过不需要获取的列
                if (((vCols != null) && (!(vColsSize > columnId && (vCols[columnId] != 0)))) || ((mCols != null) && (mCols[columnId] != 0))) {
                    if (columnId < highestColumnOnPage) {

                        offset_to_field_data += StoredFieldHeader.readTotalFieldLength(
                                pageData, offset_to_field_data);
                    }
                    continue;

                }


                if (columnId < highestColumnOnPage) {
                    //列在当前页
                    //读取字段状态
                    int fieldStatus = StoredFieldHeader.readStatus(pageData, offset_to_field_data);
                    //读取字段数据长度
                    int fieldDataLength = StoredFieldHeader.readFieldLengthAndSetStreamPosition(
                            pageData, offset_to_field_data + StoredFieldHeader.STORED_FIELD_HEADER_STATUS_SIZE,
                            fieldStatus, SLOT_SIZE, dataIn);

                    Object column = row[columnId];

                    if ((fieldStatus & StoredFieldHeader.FIELD_NONEXISTENT) != StoredFieldHeader.FIELD_NONEXISTENT) {
                        //是否是溢出字段
                        boolean isOverflow = ((fieldStatus & StoredFieldHeader.FIELD_OVERFLOW) != 0);
                        if (isOverflow) {

                        }
                        //如果当前列是数据描述的实现
                        if (column instanceof DataValueDescriptor) {
                            DataValueDescriptor sColumn = (DataValueDescriptor) column;
                            //字段不为空
                            if ((fieldStatus & StoredFieldHeader.FIELD_NULL) == 0) {
                                if (!isOverflow) {
                                    //设置读取限制
                                    dataIn.setLimit(fieldDataLength);
                                    //inUserCode = dataIn;
                                    //将数据读取到sColumn
                                    sColumn.readExternalFromArray(dataIn);
                                    //inUserCode = null;
                                    int unread = dataIn.clearLimit();
                                    //如果有未读取字节,则跳出
                                    if (unread != 0) {
                                        DataInputUtil.skipFully(dataIn, unread);
                                    }

                                }
                            }
                        } else {
                            if (StoredFieldHeader.isNull(fieldStatus)) {
                                throw new RuntimeException(String.format("Column {0} of row is null, it needs to be set to point to an object", column));
                            }
                        }


                    }
                    //移动到下一个字段开始的游标
                    offset_to_field_data = dataIn.getPosition();
                } else {
                    //columnId < highestColumnOnPage相反
                }
            }//end for

            if ((numberFields + startColumn) > max_colid) {
                return true;
            } else {
                return false;
            }
        } catch (IOException ioe) {
            throw ioe;
        }
    }


    /**
     * 根据构造的页面构建参数
     * 将其传递到当前存储页,并创建pageData流,目的为了装载数据
     */
    protected void createPage(PageKey newIdentity, PageCreationArgs args) {
        spareSpace = args.spareSpace;
        minimumRecordSize = args.minimumRecordSize;
        setPageArray(args.pageSize);
        cleanPage();
        nextId = RecordId.FIRST_RECORD_ID;
        createOutStreams();
    }

    /**
     * 创建一个输出流
     */
    private void createOutStreams() {
        rawDataOut = new ArrayOutputStream();
        rawDataOut.setData(pageData);
        logicalDataOut = new FormatIdOutputStream(rawDataOut);
    }

    private void cleanPage() {
        setDirty();
        //清空数组
        clearSection(0, getPageSize());
        slotsInUse = 0;
        deletedRowCount = 0;
        headerOutOfDate = true;
        clearAllSpace();
    }

    private void clearAllSpace() {
        freeSpace = totalSpace;
        firstFreeByte = getPageSize() - totalSpace - CHECKSUM_SIZE;
    }

    public final int getPageSize() {
        return pageData.length;
    }

    protected final void clearSection(int offset, int length) {
        Arrays.fill(pageData, offset, offset + length, (byte) 0);
    }

    /***/
    protected void setDirty() {
        synchronized (this) {
            isDirty = true;
            preDirty = false;
        }
    }


    /**
     * 将页面缓存数据添加到pageData
     * 一般是在创建页面之后,执行usePageBuffer操作
     */
    protected void usePageBuffer(byte[] pageBuffer) {
        pageData = pageBuffer;
        int pageSize = pageData.length;
        if (rawDataIn != null) {
            rawDataIn.setData(pageData);
        }
        slotEntrySize = 2 * SLOT_SIZE;
        initSpace();
        //第一个槽位偏移量4096-8-4=4084
        slotTableOffsetToFirstEntry = (pageSize - CHECKSUM_SIZE - slotEntrySize);
        slotTableOffsetToFirstRecordLengthField = slotTableOffsetToFirstEntry + SLOT_SIZE;
        if (rawDataOut != null) {
            rawDataOut.setData(pageData);
        }
    }

    /**
     * 更新记录的长度
     *
     * @param slot  代表这个第几个槽位即第几条记录
     * @param delta 当前槽位改变的次数
     */
    private void updateRecordPortionLength(int slot, int delta, int reservedDelta) throws IOException {
        rawDataOut.setPosition(slotTableOffsetToFirstRecordLengthField - (slot * slotEntrySize));
        logicalDataOut.writeShort(getRecordPortionLength(slot) + delta);
    }

    private void updateRecordPortionLength(int slot, int delta) throws IOException {
        rawDataOut.setPosition(slotTableOffsetToFirstRecordLengthField - (slot * slotEntrySize));
        logicalDataOut.writeShort(getRecordPortionLength(slot) + delta);
    }

    /**
     * 获取记录的长度
     *
     * @param slot 代表这个第几个槽位即第几条记录
     */
    protected int getRecordPortionLength(int slot) throws IOException {
        ArrayInputStream lrdi = rawDataIn;
        lrdi.setPosition(slotTableOffsetToFirstRecordLengthField - (slot * slotEntrySize));
        return lrdi.readUnsignedShort();
    }

    private void initSpace() {
        totalSpace = getMaxFreeSpace();
    }

    /**
     * PAGE_HEADER_OFFSET标识页面头部开始偏移量,加上页面头部大小
     */
    protected static final int RECORD_SPACE_OFFSET = PAGE_HEADER_OFFSET + PAGE_HEADER_SIZE;

    /**
     * 获取最大剩余空间
     * 最大剩余空间为减去页面头部空间和校验和
     */
    protected int getMaxFreeSpace() {
        return getPageSize() - RECORD_SPACE_OFFSET - CHECKSUM_SIZE;
    }

    /**
     * 写入格式Id,写入两个字符
     */
    protected void writeFormatId(PageKey identity) {
        try {
            if (rawDataOut == null) {
                createOutStreams();
            }
            rawDataOut.setPosition(0);
            FormatIdUtil.writeFormatIdInteger(logicalDataOut, getTypeFormatId());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(String.format("pageKey write exception::%s", identity));
        }
    }

    /**
     * 更新页面头部信息
     */
    protected void writePage(PageKey identity) {
        try {
            updatePageHeader();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Unexpected exception on in-memory page %s", identity));
        }
    }

    /**
     * 将对应槽位的数据设置为删除状态
     * */
    @Override
    public void setDeleteStatus(LogInstant instant, int slot, boolean delete) throws IOException {
        if (rawDataOut == null){
            createOutStreams();
        }
        setDirty();
        deletedRowCount += super.setDeleteStatus(slot, delete);
        headerOutOfDate = true;
        int offset = getRecordOffset(slot);
        StoredRecordHeader recordHeader = getHeaderAtSlot(slot);
        rawDataOut.setPosition(offset);
        recordHeader.write(logicalDataOut);
    }

    @Override
    public void logColumn(int slot, int fieldId, Object column, DynamicByteArrayOutputStream out, int overflowThreshold) throws IOException {
        int bytesAvailable = freeSpace;
        int beginPosition = -1;

        // space reserved, but not used by the record
        bytesAvailable += 0;

        // The size of the old field is also available for the new field
        rawDataIn.setPosition(getFieldOffset(slot, fieldId));

        int fieldStatus = StoredFieldHeader.readStatus(rawDataIn);
        int fieldDataLength = StoredFieldHeader.readFieldDataLength(rawDataIn, fieldStatus, SLOT_SIZE);

        bytesAvailable += StoredFieldHeader.size(fieldStatus, fieldDataLength, SLOT_SIZE) + fieldDataLength;

        try {
            setOutputStream(out);
            beginPosition = rawDataOut.getPosition();

            Object[] row = new Object[1];
            row[0] = column;
            throw new RuntimeException("");

        } finally {
            rawDataOut.setPosition(beginPosition);
            resetOutputStream();
        }
    }


    private int logColumn(Object[] row, int arrayPosition, DynamicByteArrayOutputStream out, int spaceAvailable, int columnFlag, int overflowThreshold) throws IOException {
        Object column = (row != null ? row[arrayPosition] : null);
        boolean longColumnDone = true;
        int fieldStatus = StoredFieldHeader.setFixed(StoredFieldHeader.setInitial(), true);

        int beginPosition = out.getPosition();
        int columnBeginPosition = 0;
        int headerLength;
        int fieldDataLength = 0;

        if ((column == null) && (columnFlag != COLUMN_CREATE_NULL)) {
            fieldStatus = StoredFieldHeader.setNonexistent(fieldStatus);
            headerLength = StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);
        } else if (columnFlag == COLUMN_CREATE_NULL) {
            fieldStatus = StoredFieldHeader.setNull(fieldStatus, true);
            headerLength = StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);
        } else if (column instanceof DataValueDescriptor) {
            DataValueDescriptor sColumn = (DataValueDescriptor) column;
            boolean isNull = (columnFlag == COLUMN_CREATE_NULL) || sColumn.isNull();
            if (isNull) {
                fieldStatus = StoredFieldHeader.setNull(fieldStatus, true);
            }

            headerLength = StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);

            if (!isNull) {
                try {
                    columnBeginPosition = out.getPosition();
                    sColumn.writeExternal(logicalDataOut);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe.getMessage());
                }
                fieldDataLength = (out.getPosition() - beginPosition) - headerLength;
            }
        } else if (column instanceof RecordId) {
            throw new RuntimeException("不支持溢出页");
        } else {
            headerLength = StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);
            logicalDataOut.writeObject(column);
            fieldDataLength = (out.getPosition() - beginPosition) - headerLength;
        }

        fieldStatus = StoredFieldHeader.setFixed(fieldStatus, false);
        int fieldSizeOnPage = StoredFieldHeader.size(fieldStatus, fieldDataLength, SLOT_SIZE) + fieldDataLength;
        userRowSize += fieldDataLength;
        out.setPosition(beginPosition);
        // We are borrowing this to set the size of our fieldDataLength.
        fieldStatus = StoredFieldHeader.setFixed(fieldStatus, true);
        try {
            headerLength = StoredFieldHeader.write(
                    out, fieldStatus, fieldDataLength, SLOT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.setPosition(beginPosition + fieldDataLength + headerLength);
        spaceAvailable -= fieldSizeOnPage;

        return spaceAvailable;
    }

    /**
     * 更新页面头部信息
     */
    private void updatePageHeader() throws IOException {
        rawDataOut.setPosition(PAGE_HEADER_OFFSET);
        logicalDataOut.writeBoolean(isOverflowPage);
        logicalDataOut.writeByte(getPageStatus());
        logicalDataOut.writeLong(getPageVersion());
        logicalDataOut.writeShort(slotsInUse);
        logicalDataOut.writeInt(nextId);
    }


    @Override
    public int getSlotNumber(RecordId handle) {
        int slot = findRecordById(handle.getId(), handle.getSlotNumberHint());
        return slot;
    }


    @Override
    public int logRow(int slot, boolean forInsert, int recordId, Object[] row,
                      FormatableBitSet validColumns,
                      DynamicByteArrayOutputStream out,
                      int startColumn,
                      byte insertFlag,
                      int realStartColumn,
                      int realSpaceOnPage,
                      int overflowThreshold) throws IOException {

        //如果不是插入,则是更新数据,返回开始列即可
        if (!forInsert) {
            if ((realStartColumn != -1) && realSpaceOnPage == -1) {
                return realStartColumn;
            }
        }


        //获取余下可用空间
        int spaceAvailable = freeSpace;
        int beginPosition = out.getPosition();
        setOutputStream(out);


        if (!forInsert) {
            spaceAvailable += getTotalSpace(slot);
        } else {
            spaceAvailable -= slotEntrySize;
        }

        if (spaceAvailable <= 0) {
            throw new RuntimeException("no space on page");
        }

        if (row == null) {
            throw new RuntimeException("row can't be null");
        }
        try {
            int numberFields = 0;
            StoredRecordHeader recordHeader;
            if (forInsert) {
                recordHeader = new StoredRecordHeader();
            } else {
                recordHeader = new StoredRecordHeader(getHeaderAtSlot(slot));
                startColumn = recordHeader.getFirstField();
            }


            if (validColumns == null) {
                numberFields = row.length - startColumn;
            } else {
                for (int i = validColumns.getLength() - 1; i >= startColumn; i--) {
                    if (validColumns.isSet(i)) {
                        numberFields = i + 1 - startColumn;
                        break;
                    }
                }
            }


            int onPageNumberFields = -1;

            if (forInsert) {
                recordHeader.setId(recordId);
                recordHeader.setNumberFields(numberFields);
            } else {
                //更新记录
                onPageNumberFields = recordHeader.getNumberFields();
                if (numberFields > onPageNumberFields) {
                    recordHeader.setNumberFields(numberFields);
                } else if (numberFields < onPageNumberFields) {
                    if (validColumns == null) {
                        recordHeader.setNumberFields(numberFields);
                    } else {
                        numberFields = onPageNumberFields;
                    }
                }
            }


            int endFieldExclusive = startColumn + numberFields;
            if (realStartColumn >= endFieldExclusive) {
                return -1;
            }
            if ((insertFlag & Page.INSERT_DEFAULT) != Page.INSERT_DEFAULT) {
                recordHeader.setFirstField(startColumn);
            }
            //recordHeader.setFirstField(startColumn);
            int firstColumn = realStartColumn;
            if (realStartColumn == -1) {
                int recordHeaderLength = recordHeader.write(logicalDataOut);
                spaceAvailable -= recordHeaderLength;
                if (spaceAvailable < 0) {
                    throw new RuntimeException("no space on page");
                }
                firstColumn = startColumn;
            }

            boolean monitoringOldFields = false;
            int validColumnsSize = (validColumns == null) ? 0 : validColumns.getLength();
            if (validColumns != null) {
                if (!forInsert) {
                    if ((validColumns != null) && (firstColumn < (startColumn + onPageNumberFields))) {
                        rawDataIn.setPosition(getFieldOffset(slot, firstColumn));
                        monitoringOldFields = true;
                    }
                }
            }

            int recordSize = 0;
            int lastSpaceAvailable = spaceAvailable;
            int columnFlag = COLUMN_FIRST;
            for (int i = firstColumn; i < endFieldExclusive; i++) {
                Object ref = null;
                boolean ignoreColumn = false;
                if ((validColumns == null) || (validColumnsSize > i && validColumns.isSet(i))) {
                    if (i < row.length)
                        ref = row[i];
                } else if (!forInsert) {
                    ignoreColumn = true;
                }
                lastSpaceAvailable = spaceAvailable;
                if (ignoreColumn) {
                    if (i < (startColumn + onPageNumberFields)) {
                        int oldOffset = rawDataIn.getPosition();
                        skipField(rawDataIn);
                        int oldFieldLength = rawDataIn.getPosition() - oldOffset;
                        if (oldFieldLength <= spaceAvailable) {
                            logColumn(null, 0, out, Integer.MAX_VALUE, COLUMN_NONE, overflowThreshold);
                            spaceAvailable -= oldFieldLength;
                        }
                    } else {
                        spaceAvailable = logColumn(null, 0, out, spaceAvailable, COLUMN_CREATE_NULL, overflowThreshold);
                    }
                } else {
                    if (monitoringOldFields && (i < (startColumn + onPageNumberFields))) {
                        skipField(rawDataIn);
                    }
                    if (ref == null) {
                        spaceAvailable = logColumn(null, 0, out, spaceAvailable, columnFlag, overflowThreshold);
                    } else {
                        spaceAvailable = logColumn(row, i, out, spaceAvailable, columnFlag, overflowThreshold);
                    }
                }

                int nextColumn;
                recordSize += (lastSpaceAvailable - spaceAvailable);
                if (lastSpaceAvailable == spaceAvailable) {
                    nextColumn = i;
                } else {
                    nextColumn = endFieldExclusive;
                }

                if (nextColumn < endFieldExclusive) {
                    int actualNumberFields = nextColumn - startColumn;
                    int oldSize = recordHeader.size();
                    recordHeader.setNumberFields(actualNumberFields);
                    int newSize = recordHeader.size();
                    int endPosition = out.getPosition();
                    if (oldSize > newSize) {
                        int delta = oldSize - newSize;
                        out.setBeginPosition(beginPosition + delta);
                        out.setPosition(beginPosition + delta);
                    } else if (newSize > oldSize) {
                        out.setPosition(beginPosition);
                    } else {
                        out.setBeginPosition(beginPosition);
                        out.setPosition(beginPosition);
                    }
                    int realLen = recordHeader.write(logicalDataOut);
                    out.setPosition(endPosition);
                    if (!forInsert) {
                        if (validColumns != null) {
                            throw new RuntimeException("更新失败");
                        }
                    }
                    return (nextColumn);
                }
                columnFlag = COLUMN_NONE;
            }
            out.setBeginPosition(beginPosition);
            startColumn = -1;
        } finally {
            resetOutputStream();
        }
        return startColumn;
    }


    /**
     * 重置逻辑输出流为行数据输出流
     */
    private void resetOutputStream() {
        logicalDataOut.setOutput(rawDataOut);
    }

    /**
     * @param row            列数据来源的行
     * @param arrayPosition  列在数组所对应的位置
     * @param out            存储页所对应的写入流
     * @param spaceAvailable 页剩余可用空间
     */
    public int logColumn(Object[] row, int arrayPosition, DynamicByteArrayOutputStream out, int spaceAvailable) throws IOException {
        Object column = null;
        int headerLength;
        if (row != null) {
            column = row[arrayPosition];
        }
        int fieldStatus = StoredFieldHeader.setFixed(StoredFieldHeader.setInitial(), true);
        int beginPosition = out.getPosition();
        int fieldDataLength = 0;
        if (column instanceof DataValueDescriptor) {
            DataValueDescriptor sColumn = (DataValueDescriptor) column;
            Boolean isNull = sColumn.isNull();
            if (isNull) {
                fieldStatus = StoredFieldHeader.setNull(fieldStatus, true);
            }
            headerLength = StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);
            if (!isNull) {
                try {
                    sColumn.writeExternal(logicalDataOut);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe.getMessage());
                }
                fieldDataLength = (out.getPosition() - beginPosition) - headerLength;
            }
        } else if (column instanceof RecordId) {
            RecordId overflowHandle = (RecordId) column;
            fieldStatus = StoredFieldHeader.setOverflow(fieldStatus, true);
            StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);
            CompressedNumber.writeLong(out, overflowHandle.getPageNumber());
        } else {
            StoredFieldHeader.write(logicalDataOut, fieldStatus, fieldDataLength, SLOT_SIZE);
            logicalDataOut.writeObject(column);
        }

        fieldStatus = StoredFieldHeader.setFixed(fieldStatus, false);
        int fieldSizeOnPage = StoredFieldHeader.size(fieldStatus, fieldDataLength, SLOT_SIZE) + fieldDataLength;
        userRowSize += fieldDataLength;

        out.setPosition(beginPosition);


        fieldStatus = StoredFieldHeader.setFixed(fieldStatus, true);
        headerLength = StoredFieldHeader.write(out, fieldStatus, fieldDataLength, SLOT_SIZE);
        out.setPosition(beginPosition + fieldDataLength + headerLength);

        spaceAvailable -= fieldSizeOnPage;
        return spaceAvailable;
    }

    private void setOutputStream(OutputStream out) {
        if (rawDataOut == null) {
            createOutStreams();
        }
        logicalDataOut.setOutput(out);
    }

    public void storeField(LogInstant instant, int slot, int fieldNumber, ObjectInput in) throws IOException {
        setDirty();
        int offset = getFieldOffset(slot, fieldNumber);

        // get the field header information, the input stream came from the log
        ArrayInputStream lrdi = rawDataIn;
        lrdi.setPosition(offset);
        int oldFieldStatus = StoredFieldHeader.readStatus(lrdi);
        int oldFieldDataLength = StoredFieldHeader.readFieldDataLength(lrdi, oldFieldStatus, SLOT_SIZE);

        int newFieldStatus = StoredFieldHeader.readStatus(in);
        int newFieldDataLength = StoredFieldHeader.readFieldDataLength(in, newFieldStatus, SLOT_SIZE);
        newFieldStatus = StoredFieldHeader.setFixed(newFieldStatus, false);

        int oldFieldLength = StoredFieldHeader.size(oldFieldStatus, oldFieldDataLength, SLOT_SIZE) + oldFieldDataLength;
        int newFieldLength = StoredFieldHeader.size(newFieldStatus, newFieldDataLength, SLOT_SIZE) + newFieldDataLength;

        createSpaceForUpdate(slot, offset, oldFieldLength, newFieldLength);

        rawDataOut.setPosition(offset);
        offset += StoredFieldHeader.write(rawDataOut, newFieldStatus, newFieldDataLength, SLOT_SIZE);

        if (newFieldDataLength != 0)
            in.readFully(pageData, offset, newFieldDataLength);
    }

    @Override
    protected int newRecordId(int recordId) {
        return recordId + 1;
    }

    @Override
    public void doUpdateAtSlot(Transaction transaction, int slot, int id, Object[] row, FormatableBitSet validColumns) {

        RecordId headRecordId = isOverflowPage() ? null : getRecordIdAtSlot(slot);
        if (row == null) {
            owner.getActionSet().actionUpdate(transaction, this, slot, id, row, validColumns, -1, null, -1, headRecordId);
            return;
        }
        int startColumn = RowUtil.nextColumn(row, validColumns, 0);
        if (startColumn == -1) {
            return;
        }
        StoredPage curPage = this;

        StoredRecordHeader storedRecordHeader = curPage.getHeaderAtSlot(slot);
        int startField = storedRecordHeader.getFirstField();
        int endFieldExclusive = startField + storedRecordHeader.getNumberFields();
        long nextPage = -1;
        int realStartColumn = -1;
        int realSpaceOnPage = -1;
        if (!storedRecordHeader.hasOverflow() || ((startColumn >= startField) && (startColumn < endFieldExclusive))) {
            boolean hitLongColumn;
            int nextColumn = -1;
            Object[] savedFields = null;
            DynamicByteArrayOutputStream logBuffer = null;
            nextColumn = owner.getActionSet().actionUpdate(transaction, curPage, slot, id, row, validColumns, realStartColumn, logBuffer, realSpaceOnPage, headRecordId);
            if ((curPage != this) && (curPage != null)) {
                curPage.unlatch();
            }
        }
    }

    private int getFieldOffset(int slot, int fieldNumber) throws IOException {
        int offset = getRecordOffset(slot);
        StoredRecordHeader recordHeader = getHeaderAtSlot(slot);
        int startField = recordHeader.getFirstField();
        ArrayInputStream lrdi = rawDataIn;
        lrdi.setPosition(offset + recordHeader.size());
        for (int i = startField; i < fieldNumber; i++) {
            skipField(lrdi);
        }
        return rawDataIn.getPosition();
    }


    public void skipField(ObjectInput in) throws IOException {

        int fieldStatus = StoredFieldHeader.readStatus(in);
        int fieldDataLength = StoredFieldHeader.readFieldDataLength(in, fieldStatus, SLOT_SIZE);

        if (fieldDataLength != 0) {
            DataInputUtil.skipFully(in, fieldDataLength);
        }
    }

    private void createSpaceForUpdate(int slot, int offset, int oldLength, int newLength) throws IOException {
        if (newLength <= oldLength) {
            int diffLength = oldLength - newLength;
            if (diffLength == 0) {
                return;
            }
            int remainingLength = shiftRemainingData(slot, offset, oldLength, newLength);
            clearSection(offset + newLength + remainingLength, diffLength);
            updateRecordPortionLength(slot, -(diffLength), diffLength);
            return;
        }
        int extraLength = newLength - oldLength;

        //int recordReservedSpace = getReservedCount(slot);
        int reservedDelta = 0;
        //int spaceRequiredFromFreeSpace = extraLength - recordReservedSpace;


//        if (spaceRequiredFromFreeSpace > 0) {
//            int nextRecordOffset = getRecordOffset(slot) + getTotalSpace(slot);
//            //expandPage(nextRecordOffset, spaceRequiredFromFreeSpace);
//            reservedDelta = -(recordReservedSpace);
//        } else {
//            reservedDelta = -(extraLength);
//        }
        int remainingLength = shiftRemainingData(slot, offset, oldLength, newLength);
        updateRecordPortionLength(slot, extraLength, reservedDelta);
    }

    private int shiftRemainingData(int slot, int offset, int oldLength, int newLength) throws IOException {
        int remainingLength = (getRecordOffset(slot) + getRecordPortionLength(slot)) - (offset + oldLength);
        if (remainingLength != 0) {
            System.arraycopy(pageData, offset + oldLength, pageData, offset + newLength, remainingLength);
        }
        return remainingLength;

    }

    public boolean spaceForInsert(Object[] row, FormatableBitSet validColumns, int overflowThreshold) {
        if (slotsInUse == 0) {
            return true;
        }
        if (!allowInsert()) {
            return false;
        }
        DynamicByteArrayOutputStream out = new DynamicByteArrayOutputStream();
        try {
            logRow(0, true, nextId, row, validColumns, out, 0, Page.INSERT_DEFAULT, -1, -1, overflowThreshold);
        } catch (IOException ioe) {
            throw new RuntimeException("DATA_UNEXPECTED_EXCEPTION");
        }
        return true;
    }

    public void purgeRowPieces(Transaction transaction, int slot, RecordId headRowHandle, boolean needDataLogged) {
        purgeColumnChains(transaction, slot, headRowHandle);

    }

    private void purgeColumnChains(Transaction transaction, int slot, RecordId headRowHandle) {
        try {
            StoredRecordHeader recordHeader = getHeaderAtSlot(slot);

            int numberFields = recordHeader.getNumberFields();

            // these reads are always against the page array
            ArrayInputStream lrdi = rawDataIn;

            // position the stream to just after record header.
            int offset = getRecordOffset(slot) + recordHeader.size();
            lrdi.setPosition(offset);

            for (int i = 0; i < numberFields; i++) {
                int fieldStatus = StoredFieldHeader.readStatus(lrdi);
                int fieldLength =
                        StoredFieldHeader.readFieldDataLength(
                                lrdi, fieldStatus, SLOT_SIZE);

                if (!StoredFieldHeader.isOverflow(fieldStatus)) {
                    // skip this field, it is not an long column
                    if (fieldLength != 0) {

                        lrdi.setPosition(lrdi.getPosition() + fieldLength);
                    }
                    continue;
                } else {

                    throw new RuntimeException("");
                }
            }
        } catch (IOException ioe) {
        }
    }


    private final boolean qualifyRecordFromSlot(Object[] row, int offset_to_row_data, FetchDescriptor fetchDesc, StoredRecordHeader recordHeader) {
        boolean row_qualifies = true;
        Qualifier[][] qual_list = fetchDesc.getQualifierList();
        int[] materializedCols = fetchDesc.getMaterializedColumns();
        for (int i = 0; i < qual_list[0].length; i++) {
            Qualifier q = qual_list[0][i];
            int col_id = q.getColumnId();
            if (materializedCols[col_id] == 0) {
                readOneColumnFromPage(row, col_id, offset_to_row_data, recordHeader);
                materializedCols[col_id] = offset_to_row_data;
            }
            row_qualifies = ((DataValueDescriptor) row[col_id]).compare(q.getOperator(), q.getOrderable(), false, false);

            if (!row_qualifies) {

                return false;
            }
        }

        return row_qualifies;
    }


    private final void readOneColumnFromPage(Object[] row, int colid, int offset_to_field_data, StoredRecordHeader recordHeader) {
        ArrayInputStream lrdi = rawDataIn;
        try {
            Object column = row[colid];
            if (colid <= (recordHeader.getNumberFields() - 1)) {
                for (int columnId = colid; columnId > 0; columnId--) {
                    offset_to_field_data +=
                            StoredFieldHeader.readTotalFieldLength(
                                    pageData, offset_to_field_data);
                }
                int fieldStatus = StoredFieldHeader.readStatus(pageData, offset_to_field_data);

                int fieldDataLength = StoredFieldHeader.readFieldLengthAndSetStreamPosition(pageData,
                        offset_to_field_data + StoredFieldHeader.STORED_FIELD_HEADER_STATUS_SIZE,
                        fieldStatus, SLOT_SIZE, lrdi);


                if (!StoredFieldHeader.isNonexistent(fieldStatus)) {
                    boolean isOverflow =
                            StoredFieldHeader.isOverflow(fieldStatus);

                    OverflowInputStream overflowIn = null;

                    if (isOverflow) {
                        throw new RuntimeException("The feature not implement");
                    }

                    // Deal with Storable columns
                    if (column instanceof DataValueDescriptor) {
                        DataValueDescriptor sColumn = (DataValueDescriptor) column;

                        // is the column null ?
                        if (StoredFieldHeader.isNull(fieldStatus)) {
                            sColumn.restoreToNull();
                        } else {
                            if (!isOverflow) {
                                lrdi.setLimit(fieldDataLength);
                                sColumn.readExternalFromArray(lrdi);

                                int unread = lrdi.clearLimit();
                                if (unread != 0)
                                    DataInputUtil.skipFully(lrdi, unread);
                            } else {
                                throw new RuntimeException("The feature not implement");
                            }
                        }
                    } else {
                        if (StoredFieldHeader.isNull(fieldStatus)) {
                            throw new RuntimeException("DATA_NULL_STORABLE_COLUMN");
                        }
                        lrdi.setLimit(fieldDataLength);
                        row[colid] = lrdi.readObject();
                        int unread = lrdi.clearLimit();
                        if (unread != 0)
                            DataInputUtil.skipFully(lrdi, unread);
                    }

                } else {
                    if (column instanceof DataValueDescriptor) {
                        ((DataValueDescriptor) column).restoreToNull();
                    } else {
                        row[colid] = null;
                    }
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
