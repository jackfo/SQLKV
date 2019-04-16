package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.service.io.CompressedNumber;
import com.cfs.sqlkv.store.access.raw.PageKey;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author zhengxiaokang
 * @Description 1 bytes 状态
 * RECORD_DELETED 用于表示记录已经被删除
 * RECORD_OVERFLOW 表示记录已经溢出,它会指向溢出的页和ID
 * RECORD_HAS_FIRST_FIELD
 * RECORD_VALID_MASK
 * 记录标识
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-24 15:23
 */
public final class StoredRecordHeader {

    /**
     * 存储记录头的状态
     * */
    private static final byte RECORD_DELETED = 0x01;
    private static final byte RECORD_OVERFLOW = 0x02;
    private static final byte RECORD_HAS_FIRST_FIELD = 0x04;
    private static final byte RECORD_VALID_MASK = 0x0f;

    /**
     * 记录的标识
     */
    protected int id;

    /**
     * 记录的状态
     */
    private byte status;

    /**
     * 每行的字段数量
     */
    protected int numberFields;

    protected RecordId recordId;

    public static final int MAX_OVERFLOW_ONLY_REC_SIZE =
            1 +                                     // stored status byte
                    CompressedNumber.MAX_INT_STORED_SIZE + // max stored record id size
                    CompressedNumber.MAX_LONG_STORED_SIZE + // max stored overflow page
                    CompressedNumber.MAX_INT_STORED_SIZE;   // max stored overflow id

    private static class OverflowInfo {
        /**
         * 如果溢出
         * 这是页面overflowPage上行的id，可以找到该行的下一个位置
         * 在这种情况下，此页面上没有 “真实” 字段
         * 这种情况会出现在 如果已更新行以使真实的第一个字段不再适合头页
         */
        private int overflowId;
        /**
         * 行可以被发现的列
         */
        private long overflowPage;
        private int firstField;

        private OverflowInfo() {
        }

        private OverflowInfo(OverflowInfo from) {
            overflowId = from.overflowId;
            overflowPage = from.overflowPage;
            firstField = from.firstField;
        }
    }

    private OverflowInfo overflow;

    public StoredRecordHeader() {
    }

    public StoredRecordHeader(StoredRecordHeader from) {
        this.status = from.status;
        this.id = from.id;
        this.numberFields = from.numberFields;
        recordId = null;
        if (from.overflow != null) {
            overflow = new OverflowInfo(from.overflow);
        }
    }

    public StoredRecordHeader(int id, int numberFields) {
        setId(id);
        setNumberFields(numberFields);
    }

    public StoredRecordHeader(byte data[], int offset) {
        read(data, offset);
    }

    public final void setId(int id) {
        this.id = id;
    }


    public final void setNumberFields(int numberFields) {
        this.numberFields = numberFields;
    }


    /**
     * 将记录写入到相应位置,无溢出页状态下共9个字节
     * 第一个字节是记录状态
     * 2-5字节是记录的标识
     * 6-9个字节是记录字段的个数
     * */
    public int write(OutputStream out) throws IOException {
        //写入当前页的状态
        int len = 1;
        out.write(status);
        //写入当前页的标识
        len += CompressedNumber.writeInt(out, id);
        if (hasOverflow()) {
            len += CompressedNumber.writeLong(out, overflow.overflowPage);
            len += CompressedNumber.writeInt(out, overflow.overflowId);
        }

        /**
         * 如果当前页记录了首个字段
         * 那么记录溢出页的首个字段
         * */
        if (hasFirstField()) {
            len += CompressedNumber.writeInt(out, overflow.firstField);
        }
        /**
         * 如果没有溢出或者当前页是溢出且记录首个字段
         * 那么在当前页记录下所有的字段数目
         * */
        if (!hasOverflow() || hasFirstField()) {
            len += CompressedNumber.writeInt(out, numberFields);
        }

        return len;
    }

    public void read(java.io.ObjectInput in) throws IOException {
        int s = in.read();
        //如果没有读取到当前页的状态则需要扔出异常
        if (s < 0) {
            throw new RuntimeException("StoredRecordHeader read status have problem");
        }
        status = (byte) s;

        //读取当前页的标识
        id = CompressedNumber.readInt(in);

        if (hasOverflow() || hasFirstField()) {
            overflow = new OverflowInfo();
        } else {
            overflow = null;
        }
        /**
         * 如果当前页是溢出页,那么需要读取锁指向页以及它的Id
         * */
        if (hasOverflow()) {
            overflow.overflowPage = CompressedNumber.readLong(in);
            overflow.overflowId = CompressedNumber.readInt(in);
        }
        /**
         *如果当前页记录了首个字段,则需要将字段添加到溢出页
         * */
        if (hasFirstField()) {
            overflow.firstField = CompressedNumber.readInt(in);
        }

        if (!hasOverflow() || hasFirstField()) {
            numberFields = CompressedNumber.readInt(in);
        } else {
            numberFields = 0;
        }
        recordId = null;
    }

    /**
     *
     */
    private void read(byte[] data, int offset) {
        status = data[offset++];
        int value = data[offset++];
        /**
         * 涉及id的存储方式
         * */
        if ((value & ~0x3f) == 0) {
            id = value;
        } else if ((value & 0x80) == 0) {
            // value is stored in 2 bytes.  only use low 6 bits from 1st byte.
            id = (((value & 0x3f) << 8) | (data[offset++] & 0xff));
        } else {
            // value is stored in 4 bytes.  only use low 7 bits from 1st byte.
            id = ((value & 0x7f) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | ((data[offset++] & 0xff));
        }
        /**
         * 既不是溢出页,也不包含首个字段
         * */
        if ((status & (RECORD_OVERFLOW | RECORD_HAS_FIRST_FIELD)) == 0) {
            // usual case, not a record overflow and does not have first field
            overflow = null;
            readNumberFields(data, offset);
        } else if ((status & RECORD_OVERFLOW) == 0) {
            //如果不是溢出页 在这里肯定是首个字段
            overflow = new OverflowInfo();
            offset += readFirstField(data, offset);
            readNumberFields(data, offset);
        } else {
            overflow = new OverflowInfo();
            offset += readOverFlowPage(data, offset);
            offset += readOverFlowId(data, offset);
            /**
             * 如果存在首个字段则记录字段数
             * */
            if (hasFirstField()) {
                offset += readFirstField(data, offset);
                readNumberFields(data, offset);
            } else {
                numberFields = 0;
            }
        }
    }


    private void readNumberFields(byte[] data, int offset) {

        int value = data[offset++];
        if ((value & ~0x3f) == 0) {
            numberFields = value;
        } else if ((value & 0x80) == 0) {
            numberFields = (((value & 0x3f) << 8) | (data[offset] & 0xff));
        } else {
            numberFields =
                    ((value & 0x7f) << 24) |
                            ((data[offset++] & 0xff) << 16) |
                            ((data[offset++] & 0xff) << 8) |
                            ((data[offset] & 0xff));
        }
    }

    /**
     * 根据页的状态检测当前页是否溢出
     */
    public final boolean hasOverflow() {
        return ((status & RECORD_OVERFLOW) == RECORD_OVERFLOW);
    }

    /**
     * 当前-页是否记录了首个字段
     */
    protected final boolean hasFirstField() {
        return ((status & RECORD_HAS_FIRST_FIELD) == RECORD_HAS_FIRST_FIELD);
    }

    public final boolean isDeleted() {
        return ((status & RECORD_DELETED) == RECORD_DELETED);
    }

    public final int getId() {
        return id;
    }

    public int getNumberFields() {
        return numberFields;
    }

    /**
     * 返回记录头的大小
     * 1字节记录的状态
     * 1字节记录的标识
     * 1字节记录的字段数
     */
    public int size() {
        int len;
        if (id <= CompressedNumber.MAX_COMPRESSED_INT_ONE_BYTE) {
            len = 2;
        } else if (id <= CompressedNumber.MAX_COMPRESSED_INT_TWO_BYTES) {
            len = 3;
        } else {
            len = 5;
        }
        if ((status & (RECORD_OVERFLOW | RECORD_HAS_FIRST_FIELD)) == 0) {
            // usual case, not a record overflow and does not have first field
            len +=
                    (numberFields <= CompressedNumber.MAX_COMPRESSED_INT_ONE_BYTE) ?
                            1 :
                            (numberFields <= CompressedNumber.MAX_COMPRESSED_INT_TWO_BYTES) ?
                                    2 : 4;
        } else if ((status & RECORD_OVERFLOW) == 0) {
            // not overflow, and has first field set.
            len += CompressedNumber.sizeInt(numberFields);
            len += CompressedNumber.sizeInt(overflow.firstField);
        } else {
            // is an overflow field

            len += CompressedNumber.sizeLong(overflow.overflowPage);
            len += CompressedNumber.sizeInt(overflow.overflowId);

            if (hasFirstField()) {
                len += CompressedNumber.sizeInt(overflow.firstField);
                len += CompressedNumber.sizeInt(numberFields);
            }
        }
        return len;
    }

    public int getFirstField() {
        return overflow == null ? 0 : overflow.firstField;
    }

    /**
     * 根据pageKey和槽位封装成对应的记录id
     */
    protected RecordId getRecordId(PageKey pageId, int current_slot) {
        if (recordId == null) {
            recordId = new RecordId(pageId, id, current_slot);
        }
        return recordId;
    }

    private int readFirstField(byte[] data, int offset) {
        int value = data[offset++];

        if ((value & ~0x3f) == 0) {
            overflow.firstField = value;
            return 1;
        } else if ((value & 0x80) == 0) {
            overflow.firstField = (((value & 0x3f) << 8) | (data[offset] & 0xff));
            return 2;
        } else {
            // length is stored in 4 bytes.  only use low 7 bits from 1st byte.
            overflow.firstField = ((value & 0x7f) << 24) | ((data[offset++] & 0xff) << 16) |
                    ((data[offset++] & 0xff) << 8) | ((data[offset] & 0xff));

            return 4;
        }
    }

    private int readOverFlowPage(byte[] data, int offset) {
        return 0;
    }

    private int readOverFlowId(byte[] data, int offset) {
        return 0;
    }


    public void setFirstField(int firstField) {
        if (overflow == null) {
            overflow = new OverflowInfo();
        }
        overflow.firstField = firstField;
        status |= RECORD_HAS_FIRST_FIELD;
    }

    public int setDeleted(boolean deleteTrue) {
        int retCode = 0;
        if (deleteTrue) {
            if (!isDeleted()) {
                retCode = 1;
                status |= RECORD_DELETED;
            }
        } else {
            if (isDeleted()) {
                retCode = -1;
                status &= ~RECORD_DELETED;
            }
        }
        return retCode;
    }

}
