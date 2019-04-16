package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.CompressedNumber;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.OutputStream;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-28 22:33
 */
public class StoredFieldHeader {
    private static final int FIELD_INITIAL = 0x00;
    public static final int FIELD_NULL = 0x01;
    public static final int FIELD_OVERFLOW = 0x02;
    private static final int FIELD_NOT_NULLABLE = 0x04;
    public static final int FIELD_EXTENSIBLE = 0x08;
    public static final int FIELD_TAGGED = 0x10;
    protected static final int FIELD_FIXED = 0x20;

    public static final int STORED_FIELD_HEADER_STATUS_SIZE = 1;

    public static final int FIELD_NONEXISTENT = (FIELD_NOT_NULLABLE | FIELD_NULL);

    /**
     * 读取字段的状态
     */
    public static final int readStatus(ObjectInput in) throws IOException {
        int status = in.read();
        if (status >= 0) {
            return status;
        } else {
            throw new RuntimeException("status value must greater than zero");
        }
    }

    public static final int readStatus(byte[] page, int offset) {
        return page[offset];
    }

    public final static int setInitial() {
        return FIELD_INITIAL;
    }

    /**
     * 读取字段长度
     * 字段为空的话返回0
     * 字段是一个字节直接返回
     * 字段存在多个字节则进行读取
     */
    public static final int readFieldDataLength(ObjectInput in, int status, int fieldDataSize) throws IOException {
        /**
         * 如果字段不为空
         * */
        if ((status & (FIELD_NULL | FIELD_FIXED)) == 0) {
            return CompressedNumber.readInt(in);
        } else if ((status & FIELD_NULL) != 0) {
            return 0;
        } else {
            int fieldDataLength;
            if (fieldDataSize <= 2) {
                int ch1 = in.read();
                int ch2 = in.read();
                if ((ch1 | ch2) < 0) {
                    throw new EOFException();
                }
                fieldDataLength = ((ch1 << 8) + (ch2 << 0));
            } else {
                throw new RuntimeException("字段设置太长");
            }
            return (fieldDataLength);
        }
    }

    public final static int setFixed(int status, boolean isFixed) {
        if (isFixed) {
            status |= FIELD_FIXED;
        } else {
            status &= ~FIELD_FIXED;
        }
        return status;
    }

    /**
     * 写入字段的状态和长度
     */
    public static final int write(OutputStream out, int status, int fieldDataLength, int fieldDataSize) throws IOException {
        int len = 1;
        out.write(status);
        if (isNull(status)) {
            return len;
        }
        if (isFixed(status)) {
            out.write((fieldDataLength >>> 8) & 0xFF);
            out.write((fieldDataLength >>> 0) & 0xFF);
            len += 2;
        } else {
            len += CompressedNumber.writeInt(out, fieldDataLength);
        }
        return len;
    }

    public static final boolean isNull(int status) {
        return ((status & FIELD_NULL) == FIELD_NULL);
    }

    public static final boolean isFixed(int status) {
        return ((status & FIELD_FIXED) == FIELD_FIXED);
    }

    /**
     * 记录:记录长度+数据
     *
     * @param offset 当前记录的长度所在游标位置
     *               读取记录数据的长度,并将pos设置到字段记录所在的游标
     */
    public static final int readFieldLengthAndSetStreamPosition(byte[] data, int offset, int status, int fieldDataSize, ArrayInputStream ais) throws IOException {
        if ((status & (FIELD_NULL | FIELD_FIXED)) == 0) {
            //既不是空字段也不是复杂字段
            //直接获取字段长度`
            int value = data[offset++];
            //0x3f==>0111111  ~0x3f==>1000000
            if ((value & ~0x3f) == 0) {

                //0x80==>10000000
            } else if ((value & 0x80) == 0) {
                value = ((value & 0x3f) << 8) | (data[offset++] & 0xff);
            } else {
                value = (((value & 0x7f) << 24) | ((data[offset++] & 0xff) << 16) | ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff));
            }
            ais.setPosition(offset);
            return value;
        } else if ((status & FIELD_NULL) != 0) {
            ais.setPosition(offset);
            return 0;
        } else {
            int fieldDataLength;
            if (fieldDataSize <= 2) {
                fieldDataLength = ((data[offset++] & 0xff) << 8) | (data[offset++] & 0xff);
            } else {
                fieldDataLength = data[offset];
                if ((fieldDataLength & ~0x3f) == 0) {

                } else if ((fieldDataLength & 0x80) == 0) {
                    fieldDataLength = (((fieldDataLength & 0x3f) << 8) | (data[offset + 1] & 0xff));
                } else {

                    fieldDataLength = (((fieldDataLength & 0x7f) << 24) | ((data[offset + 1] & 0xff) << 16) |
                            ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff));
                }
                offset = offset + fieldDataSize;
            }
            ais.setPosition(offset);
            return (fieldDataLength);
        }
    }


    public final static int setNull(int status, boolean isNull) {
        if (isNull) {
            status |= FIELD_NULL;
        } else {
            status &= ~FIELD_NULL;
        }
        return status;
    }

    public final static int setOverflow(int status, boolean isOverflow) {
        if (isOverflow) {
            status |= FIELD_OVERFLOW;
        } else {
            status &= ~FIELD_OVERFLOW;
        }
        return status;
    }

    public static final int size(
            int status,
            int fieldDataLength,
            int fieldDataSize) {

        if ((status & (FIELD_NULL | FIELD_FIXED)) == 0) {
            // usual case - not-null, not-fixed

            // WARNING - the following code hand inlined from
            // CompressedNumber for performance.
            //
            // return(CompressedNumber.sizeInt(fieldDataLength) + 1);
            //

            if (fieldDataLength <=
                    CompressedNumber.MAX_COMPRESSED_INT_ONE_BYTE) {
                // compressed form is 1 byte
                return (2);
            } else if (fieldDataLength <=
                    CompressedNumber.MAX_COMPRESSED_INT_TWO_BYTES) {
                // compressed form is 2 bytes
                return (3);
            } else {
                // compressed form is 4 bytes
                return (5);
            }
        } else if ((status & FIELD_NULL) != 0) {
            // field is null

            return (1);
        } else {

            return ((fieldDataSize > 2) ? 5 : 3);
        }
    }

    public static final boolean isOverflow(int status) {
        return ((status & FIELD_OVERFLOW) == FIELD_OVERFLOW);
    }


    public static final int readTotalFieldLength(byte[] data, int offset) {
        if (((data[offset++]) & FIELD_NULL) != FIELD_NULL) {
            int value = data[offset];
            if ((value & ~0x3f) == 0) {
                return (value + 2);
            } else if ((value & 0x80) == 0) {
                return ((((value & 0x3f) << 8) | (data[offset + 1] & 0xff)) + 3);
            } else {
                return ((((value & 0x7f) << 24) |
                        ((data[offset + 1] & 0xff) << 16) |
                        ((data[offset + 2] & 0xff) << 8) |
                        (data[offset + 3] & 0xff)) + 5);
            }
        } else {
            return 1;
        }
    }

    public static final boolean isNonexistent(int status) {
        return ((status & FIELD_NONEXISTENT) == FIELD_NONEXISTENT);
    }

    public final static int setNonexistent(int status) {
        status |= FIELD_NONEXISTENT;
        return status;
    }

}
