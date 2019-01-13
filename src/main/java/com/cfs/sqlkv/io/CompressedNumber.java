package com.cfs.sqlkv.io;

import java.io.*;

/**
 * @author zhengxiaokang
 * @Description 将压缩的形式写入到数据
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 15:27
 */
public class CompressedNumber {

    /**整型的最大存储字节*/
    public static final int MAX_INT_STORED_SIZE = 4;
    public static final int writeInt(DataOutput out, int value) throws IOException {
        //小于0的数字无法写入
        if (value < 0){
            throw new IOException();
        }
        if (value <= 0x3f) {
            out.writeByte(value);
            return 1;
        }
        if (value <= 0x3fff) {
            out.writeByte(0x40 | (value >>> 8));
            out.writeByte(value & 0xff);
            return 2;
        }
        out.writeByte(((value >>> 24) | 0x80) & 0xff);
        out.writeByte((value >>> 16) & 0xff);
        out.writeByte((value >>> 8) & 0xff);
        out.writeByte((value) & 0xff);
        return 4;
    }

    public static final int writeInt(OutputStream out, int value) throws IOException {

        if (value < 0)
            throw new IOException();

        if (value <= 0x3f) {

            out.write(value);
            return 1;
        }

        if (value <= 0x3fff) {

            out.write(0x40 | (value >>> 8));
            out.write(value & 0xff);
            return 2;
        }

        out.write(((value >>> 24) | 0x80) & 0xff);
        out.write((value >>> 16) & 0xff);
        out.write((value >>> 8) & 0xff);
        out.write((value) & 0xff);
        return 4;
    }

    public static final int readInt(DataInput in) throws IOException {

        int value = in.readUnsignedByte();

        if ((value & ~0x3f) == 0) {
            // length is stored in this byte, we also know that the 0x80 bit
            // was not set, so no need to mask off the sign extension from
            // the byte to int conversion.

            // account for 1 byte stored length of field + 1 for all returns
            return(value);
        } else if ((value & 0x80) == 0) {
            // length is stored in 2 bytes.  only use low 6 bits from 1st byte.

            // top 8 bits of 2 byte length is stored in this byte, we also
            // know that the 0x80 bit was not set, so no need to mask off the
            // sign extension from the 1st byte to int conversion.  Need to
            // mask the byte in data[offset + 1] to account for possible sign
            // extension.

            return(((value & 0x3f) << 8) | in.readUnsignedByte());
        } else {
            // length is stored in 4 bytes.  only use low 7 bits from 1st byte.
            // top 8 bits of 4 byte length is stored in this byte, we also
            // know that the 0x80 bit was set, so need to mask off the
            // sign extension from the 1st byte to int conversion.  Need to
            // mask the bytes from the next 3 bytes data[offset + 1,2,3] to
            // account for possible sign extension.
            //
            return(
                    ((value & 0x7f)        << 24) |
                            (in.readUnsignedByte() << 16) |
                            (in.readUnsignedByte() <<  8) |
                            (in.readUnsignedByte()      ));
        }
    }


    public static final int readInt(InputStream in) throws IOException {

        int value = InputStreamUtil.readUnsignedByte(in);

        if ((value & ~0x3f) == 0)
        {
            return(value);
        }
        else if ((value & 0x80) == 0)
        {
            return(
                    ((value & 0x3f) << 8) | InputStreamUtil.readUnsignedByte(in));
        }
        else
        {
            return(
                    ((value          & 0x7f)              << 24) |
                            (InputStreamUtil.readUnsignedByte(in) << 16) |
                            (InputStreamUtil.readUnsignedByte(in) <<  8) |
                            (InputStreamUtil.readUnsignedByte(in)      ));
        }
    }

    public static final int readInt(byte[] data, int offset) {
        int value = data[offset++];

        if ((value & ~0x3f) == 0) {
            // length is stored in this byte, we also know that the 0x80 bit
            // was not set, so no need to mask off the sign extension from
            // the byte to int conversion.

            return(value);
        }
        else if ((value & 0x80) == 0) {
            // length is stored in 2 bytes.  only use low 6 bits from 1st byte.
            // top 8 bits of 2 byte length is stored in this byte, we also
            // know that the 0x80 bit was not set, so no need to mask off the
            // sign extension from the 1st byte to int conversion.  Need to
            // mask the byte in data[offset + 1] to account for possible sign
            // extension.

            return(((value & 0x3f) << 8) | (data[offset] & 0xff));
        }
        else {
            // length is stored in 4 bytes.  only use low 7 bits from 1st byte.
            // top 8 bits of 4 byte length is stored in this byte, we also
            // know that the 0x80 bit was set, so need to mask off the
            // sign extension from the 1st byte to int conversion.  Need to
            // mask the bytes from the next 3 bytes data[offset + 1,2,3] to
            // account for possible sign extension.
            //
            return(((value & 0x7f) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) <<  8) | ((data[offset]   & 0xff)      ));
        }
    }

    public static final int sizeInt(int value) {
        if (value <= 0x3f) {
            return 1;
        }
        if (value <= 0x3fff) {
            return 2;
        }
        return 4;
    }

    public static final int writeLong(DataOutput out, long value) throws IOException {

        if (value < 0)
            throw new IOException();

        if (value <= 0x3fff) {

            out.writeByte((int) ((value >>> 8) & 0xff));
            out.writeByte((int) ((value      ) & 0xff));
            return 2;
        }

        if (value <= 0x3fffffff) {

            out.writeByte((int) (((value >>> 24) | 0x40) & 0xff));
            out.writeByte((int) ( (value >>> 16) & 0xff));
            out.writeByte((int) ( (value >>>  8) & 0xff));
            out.writeByte((int) ( (value       ) & 0xff));
            return 4;
        }

        out.writeByte((int) (((value >>> 56) | 0x80) & 0xff));
        out.writeByte((int) ( (value >>> 48) & 0xff));
        out.writeByte((int) ( (value >>> 40) & 0xff));
        out.writeByte((int) ( (value >>> 32) & 0xff));
        out.writeByte((int) ( (value >>> 24) & 0xff));
        out.writeByte((int) ( (value >>> 16) & 0xff));
        out.writeByte((int) ( (value >>>  8) & 0xff));
        out.writeByte((int) ( (value       ) & 0xff));
        return 8;
    }



    public static final int writeLong(OutputStream out, long value) throws IOException {

        if (value < 0){
            throw new IOException();
        }

        if (value <= 0x3fff) {
            out.write((int) ((value >>> 8) & 0xff));
            out.write((int) ((value      ) & 0xff));
            return 2;
        }

        if (value <= 0x3fffffff) {
            out.write((int) (((value >>> 24) | 0x40) & 0xff));
            out.write((int) ( (value >>> 16) & 0xff));
            out.write((int) ( (value >>>  8) & 0xff));
            out.write((int) ( (value       ) & 0xff));
            return 4;
        }

        out.write((int) (((value >>> 56) | 0x80) & 0xff));
        out.write((int) ( (value >>> 48) & 0xff));
        out.write((int) ( (value >>> 40) & 0xff));
        out.write((int) ( (value >>> 32) & 0xff));
        out.write((int) ( (value >>> 24) & 0xff));
        out.write((int) ( (value >>> 16) & 0xff));
        out.write((int) ( (value >>>  8) & 0xff));
        out.write((int) ( (value       ) & 0xff));
        return 8;
    }

    /**
     Read a long previously written by writeLong().

     @exception IOException an exception was thrown by a method on in.
     */
    public static final long readLong(DataInput in) throws IOException {

        int int_value = in.readUnsignedByte();

        if ((int_value & ~0x3f) == 0) {
            // test for small case first - assuming this is usual case.
            // this is stored in 2 bytes.

            return((int_value << 8) | in.readUnsignedByte());
        } else if ((int_value & 0x80) == 0) {
            // value is stored in 4 bytes.  only use low 6 bits from 1st byte.

            return(
                    ((int_value & 0x3f)      << 24) |
                            (in.readUnsignedByte()   << 16) |
                            (in.readUnsignedByte()   <<  8) |
                            (in.readUnsignedByte()));
        } else {
            // value is stored in 8 bytes.  only use low 7 bits from 1st byte.
            return(
                    (((long) (int_value & 0x7f)   ) << 56) |
                            (((long) in.readUnsignedByte()) << 48) |
                            (((long) in.readUnsignedByte()) << 40) |
                            (((long) in.readUnsignedByte()) << 32) |
                            (((long) in.readUnsignedByte()) << 24) |
                            (((long) in.readUnsignedByte()) << 16) |
                            (((long) in.readUnsignedByte()) <<  8) |
                            (((long) in.readUnsignedByte())      ));
        }
    }

    /**
     Read a long previously written by writeLong().

     @exception IOException an exception was thrown by a method on in.
     */
    public static final long readLong(InputStream in) throws IOException {

        int int_value = InputStreamUtil.readUnsignedByte(in);

        if ((int_value & ~0x3f) == 0) {
            // test for small case first - assuming this is usual case.
            // this is stored in 2 bytes.

            return((int_value << 8) | InputStreamUtil.readUnsignedByte(in));
        } else if ((int_value & 0x80) == 0) {
            // value is stored in 4 bytes.  only use low 6 bits from 1st byte.

            return(
                    ((int_value      & 0x3f)              << 24) |
                            (InputStreamUtil.readUnsignedByte(in) << 16) |
                            (InputStreamUtil.readUnsignedByte(in) <<  8) |
                            (InputStreamUtil.readUnsignedByte(in)      ));

        } else {
            // value is stored in 8 bytes.  only use low 7 bits from 1st byte.
            long value = int_value;

            return(
                    (((long) (value & 0x7f)                      ) << 56) |
                            (((long) InputStreamUtil.readUnsignedByte(in)) << 48) |
                            (((long) InputStreamUtil.readUnsignedByte(in)) << 40) |
                            (((long) InputStreamUtil.readUnsignedByte(in)) << 32) |
                            (((long) InputStreamUtil.readUnsignedByte(in)) << 24) |
                            (((long) InputStreamUtil.readUnsignedByte(in)) << 16) |
                            (((long) InputStreamUtil.readUnsignedByte(in)) <<  8) |
                            (((long) InputStreamUtil.readUnsignedByte(in))      ));
        }
    }

    public static final long readLong(byte[]  data, int offset) {
        int int_value = data[offset++];

        if ((int_value & ~0x3f) == 0) {
            // test for small case first - assuming this is usual case.
            // this is stored in 2 bytes.

            return((int_value << 8) | (data[offset] & 0xff));
        } else if ((int_value & 0x80) == 0) {
            // value is stored in 4 bytes.  only use low 6 bits from 1st byte.

            return(
                    ((int_value      & 0x3f) << 24) |
                            ((data[offset++] & 0xff) << 16) |
                            ((data[offset++] & 0xff) <<  8) |
                            ((data[offset]   & 0xff)      ));

        } else {
            // value is stored in 8 bytes.  only use low 6 bits from 1st byte.
            return(
                    (((long) (int_value      & 0x7f)) << 56) |
                            (((long) (data[offset++] & 0xff)) << 48) |
                            (((long) (data[offset++] & 0xff)) << 40) |
                            (((long) (data[offset++] & 0xff)) << 32) |
                            (((long) (data[offset++] & 0xff)) << 24) |
                            (((long) (data[offset++] & 0xff)) << 16) |
                            (((long) (data[offset++] & 0xff)) <<  8) |
                            (((long) (data[offset]   & 0xff))      ));

        }
    }

    public static final int sizeLong(long value) {

        if (value <= 0x3fff) {

            return 2;
        }

        if (value <= 0x3fffffff) {
            return 4;
        }

        return 8;
    }

}
