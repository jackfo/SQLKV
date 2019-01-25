package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.ArrayInputStream;
import com.cfs.sqlkv.io.ArrayOutputStream;
import com.cfs.sqlkv.io.FormatIdOutputStream;
import com.cfs.sqlkv.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.raw.PageKey;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 *
 * 	+----------+-------------+---+---------+-------------------+-------------+--------+
 * 	| FormatId | page header | N | N bytes | alloc extend rows | slot offset |checksum|
 * 	+----------+-------------+---+---------+-------------------+-------------+--------+
 *
 *  创建容器
 *
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public class StoredPage extends CachedPage {

    protected ArrayInputStream rawDataIn;
    protected ArrayOutputStream rawDataOut;
    protected FormatIdOutputStream logicalDataOut;

    protected static final int PAGE_FORMAT_ID_SIZE = 4;
    protected static final int PAGE_HEADER_OFFSET   = PAGE_FORMAT_ID_SIZE;
    protected static final int PAGE_HEADER_SIZE     = 56;

    /**
     * The page header is a fixed size, 56 bytes, following are variables used
     * to access the fields in the header:
     * <p>
     *  1 byte  boolean isOverflowPage  is page an overflow page
     *  1 byte  byte    pageStatus      page status (field in base page)
     *  8 bytes long    pageVersion     page version (field in base page)
     *  2 bytes ushort  slotsInUse      number of slots in slot offset table
     *  4 bytes integer nextId          next record identifier
     *  4 bytes integer generation      generation number of this page(FUTURE USE)
     *  4 bytes integer prevGeneration  previous generation of page (FUTURE USE)
     *  8 bytes long    bipLocation     the location of the BI page (FUTURE USE)
     *  2 bytes ushort  deletedRowCount number of deleted rows on page.(rel 2.0)
     *  2 bytes long                    spare for future use
     *  4 bytes long                    spare (encryption writes random bytes)
     *  8 bytes long                    spare for future use
     *  8 bytes long                    spare for future use
     *
     *  0表示未分配字段
     *
     **/
    /**是否是溢出页*/
    private boolean isOverflowPage;
    /**槽便宜表的槽数*/
    private int     slotsInUse;
    /**下一个记录的标识*/
    private int     nextId;
    /**当前页自动生成的号码*/
    private int     generation;
    /**前一个自动生成的页号*/
    private int     prevGeneration;
    private long    bipLocation;
     /**在当前页删除的数量*/
    private int     deletedRowCount;


    @Override
    public boolean isOverflowPage() {
        return false;
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
    protected int   spareSpace;
    protected int minimumRecordSize;

    /**合计总空间*/
    protected int totalSpace;

    /**
     * 剩余空间
     * */
    protected int freeSpace     = Integer.MIN_VALUE;
    private   int firstFreeByte = Integer.MIN_VALUE;

    @Override
    protected void initFromData(FileContainer myContainer, PageKey newIdentity)throws StandardException {
        if(myContainer!=null){
            spareSpace          = myContainer.getSpareSpace();
            minimumRecordSize   = myContainer.getMinimumRecordSize();
        }
        try {
            readPageHeader();
            initSlotTable(newIdentity);
        } catch (IOException ioe) {
            throw new RuntimeException(String.format("Unexpected exception on in-memory page %s",identity));
        }
    }

    /**
     * 从Page数组中读取头
     * Overflow
     *
     * */
    private void readPageHeader()throws IOException{
        ArrayInputStream lrdi = rawDataIn;
        //从偏移位置为4开始读取
        lrdi.setPosition(PAGE_HEADER_OFFSET);
        long spare;
        isOverflowPage  = lrdi.readBoolean();
        setPageStatus(lrdi.readByte());
        setPageVersion(lrdi.readLong());
        slotsInUse = lrdi.readUnsignedShort();
        nextId  =  lrdi.readInt();
        generation = lrdi.readInt();
        prevGeneration  =  lrdi.readInt();
        bipLocation  =  lrdi.readLong();
        deletedRowCount =   lrdi.readUnsignedShort() - 1;
        spare           =   lrdi.readUnsignedShort();
        spare           =   lrdi.readInt();
        spare           =   lrdi.readLong();
        spare           =   lrdi.readLong();
    }

    private byte pageStatus;
    public void setPageStatus(byte status) {
        pageStatus = status;
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
}
