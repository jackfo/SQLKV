package com.cfs.sqlkv.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-13 15:07
 */
public class FormatableBitSet implements Formatable {

    private byte[]	value;
    private	byte	bitsInLastByte;
    private transient int lengthAsBits;

    private final void checkPosition(int p) {
        if (p < 0 || lengthAsBits <= p) {
            throw new IllegalArgumentException("Bit position "+p+ " is outside the legal range");
        }
    }

    private static int udiv8(int i) { return (i>>3); }
    private static byte umod8(int i) { return (byte)(i&0x7); }
    private static int umul8(int i) { return (i<<3); }

    public FormatableBitSet() {
        value = ArrayUtil.EMPTY_BYTE_ARRAY;
    }

    public FormatableBitSet(int numBits) {
        if (numBits < 0) {
            throw new
                    IllegalArgumentException("Bit set size "+ numBits +
                    " is not allowed");
        }
        initializeBits(numBits);
    }
    private void initializeBits(int numBits) {
        int numBytes = numBytesFromBits(numBits);

        // the byte array is zero'ed out by the new operator
        value = new byte[numBytes];
        bitsInLastByte = numBitsInLastByte(numBits);
        lengthAsBits = numBits;
    }

    public FormatableBitSet(byte[] newValue) {
        value = ArrayUtil.copy(newValue);
        bitsInLastByte = 8;
        //根据长度计算出其字节所占位数,如 h,e,l,l,o  == 5*8=40
        lengthAsBits = calculateLength(newValue.length);
    }

    /**
     * 计算出总位数中最后一个字节所占的位数
     * */
    private static byte numBitsInLastByte(int bits) {
        if (bits == 0){
            return 0;
        }
        byte lastbits = umod8(bits);
        return (lastbits != 0 ? lastbits : 8);
    }

    private int calculateLength(int realByteLength) {
        if (realByteLength == 0) {
            return 0;
        }
        return ((realByteLength - 1) * 8) + bitsInLastByte;
    }

    /**
     * 计算存储当前长度所需要的字节数
     * */
    private static int numBytesFromBits(int bits) {
        return (bits + 7) >> 3;
    }


    /**
     * 返回当前类格式ID
     * */
    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.BITIMPL_V01_ID;
    }

    /**
     * 第一步写入其位数长度
     * 第二步写入byte类型长度
     * */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(getLength());
        int byteLen = getLengthInBytes();
        if (byteLen > 0) {
            out.write(value, 0, byteLen);
        }
    }

    /**
     * 获取字节的长度
     *
     * @return 这个值的长度、
     */
    public int getLength() {
        return lengthAsBits;
    }

    /**
     * 获取其字节长度
     * */
    public int getLengthInBytes() {
        return FormatableBitSet.numBytesFromBits(lengthAsBits);
    }

    /**
     * 1.读取当前字节所占位数
     * 2.根据位数计算出字节长度
     * 3.构建对应长度的字节数据
     * 4.读取相应的内容
     * */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int lenInBits;
        int lenInBytes;
        //读取当前字节所占位数
        lenInBits = in.readInt();
        //根据位数计算出字节长度
        lenInBytes = FormatableBitSet.numBytesFromBits(lenInBits);
        value = new byte[lenInBytes];
        in.readFully(value);
        bitsInLastByte = numBitsInLastByte(lenInBits);
        lengthAsBits = lenInBits;
    }
}
