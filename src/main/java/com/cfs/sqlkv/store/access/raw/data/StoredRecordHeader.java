package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.io.CompressedNumber;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author zhengxiaokang
 * @Description 1 bytes 状态
 *                      RECORD_DELETED 用于表示记录已经被删除
 *                      RECORD_OVERFLOW 表示记录已经溢出,它会指向溢出的页和ID
 *                      RECORD_HAS_FIRST_FIELD
 *                      RECORD_VALID_MASK
 *                      记录标识
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-24 15:23
 */
public final class StoredRecordHeader {

    private static final byte RECORD_DELETED = 0x01;
    private static final byte RECORD_OVERFLOW = 0x02;
    private static final byte RECORD_HAS_FIRST_FIELD = 0x04;
    private static final byte RECORD_VALID_MASK = 0x0f;

    /**记录的标识*/
    protected int id;

    /**记录的状态*/
    private byte status;

    /**每行的字段数量*/
    protected int numberFields;

    protected RecordHandle handle;

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
         * */
        private long overflowPage;
        private int firstField;
        private OverflowInfo() { }
        private OverflowInfo(OverflowInfo from) {
            overflowId = from.overflowId;
            overflowPage = from.overflowPage;
            firstField = from.firstField;
        }
    }

    private OverflowInfo overflow;

    public StoredRecordHeader() { }

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


    public int write(OutputStream out) throws IOException {
        //写入当前页的状态
        int len = 1;
        out.write(status);
        //写入当前页的标识
        len += CompressedNumber.writeInt(out, id);
        if (hasOverflow()) {
            // if overflow bit is set, then write the overflow pointer info.
            len += CompressedNumber.writeLong(out, overflow.overflowPage);
            len += CompressedNumber.writeInt(out, overflow.overflowId);
        }

        /**
         * 如果当前页记录了首个字段
         * 那么记录溢出页的首个字段
         * */
        if(hasFirstField()){
            len += CompressedNumber.writeInt(out, overflow.firstField);
        }
        /**
         * 如果没有溢出或者当前页是溢出且记录首个字段
         * 那么在当前页记录下所有的字段数目
         * */
        if (!hasOverflow() || hasFirstField()){
            len += CompressedNumber.writeInt(out, numberFields);
        }

        return len;
    }

    public void read(java.io.ObjectInput in) throws IOException{
        int s = in.read();
        //如果没有读取到当前页的状态则需要扔出异常
        if(s<0){
            throw new EOFException();
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
            overflow.overflowId   = CompressedNumber.readInt(in);
        }
        /**
         *如果当前页记录了首个字段,则需要将字段添加到溢出页
         * */
        if (hasFirstField()) {
            overflow.firstField = CompressedNumber.readInt(in);
        }

        if (!hasOverflow() || hasFirstField()){
            numberFields = CompressedNumber.readInt(in);
        } else{
            numberFields = 0;
        }
        handle = null;
    }

    /**
     *
     * */
    private void read(byte[]  data, int offset){
        status = data[offset++];
        int value = data[offset++];
        /**
         * 涉及id的存储方式
         * */
        if ((value & ~0x3f) == 0) {
            id = value;
        }else if ((value & 0x80) == 0) {
            // value is stored in 2 bytes.  only use low 6 bits from 1st byte.
            id = (((value & 0x3f) << 8) | (data[offset++] & 0xff));
        } else {
            // value is stored in 4 bytes.  only use low 7 bits from 1st byte.
            id = ((value& 0x7f) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) <<  8) | ((data[offset++] & 0xff));
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
        }else{
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

    }
    /**
     * 根据页的状态检测当前页是否溢出
     * */
    public final boolean hasOverflow() {
        return ((status & RECORD_OVERFLOW) == RECORD_OVERFLOW);
    }

    /**
     * 当前-页是否记录了首个字段
     * */
    protected final boolean hasFirstField() {
        return ((status & RECORD_HAS_FIRST_FIELD) == RECORD_HAS_FIRST_FIELD);
    }
}
