package com.cfs.sqlkv.common;

import com.cfs.sqlkv.service.io.StoredFormatIds;

import java.io.*;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:41
 */
public class UUID implements Externalizable {

    static final String NULL = "NULL";

    private long majorId;
    private long timemillis;
    private int sequence;

    public UUID(String uuidString) {
        StringReader sr = new StringReader(uuidString);
        sequence = (int) readMSB(sr);
        long ltimemillis = readMSB(sr) << 32;
        ltimemillis += readMSB(sr) << 16;
        ltimemillis += readMSB(sr);
        timemillis = ltimemillis;
        majorId = readMSB(sr);
    }

    public UUID(long majorId, long timemillis, int sequence) {
        this.majorId = majorId;
        this.timemillis = timemillis;
        this.sequence = sequence;
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(majorId);
        out.writeLong(timemillis);
        out.writeInt(sequence);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        majorId = in.readLong();
        timemillis = in.readLong();
        sequence = in.readInt();
    }

    private static long readMSB(StringReader sr) {
        long value = 0;
        try {
            int c;
            while ((c = sr.read()) != -1) {
                if (c == '-')
                    break;
                value <<= 4;
                int nibble;
                if (c <= '9')
                    nibble = c - '0';
                else if (c <= 'F')
                    nibble = c - 'A' + 10;
                else
                    nibble = c - 'a' + 10;
                value += nibble;
            }
        } catch (Exception e) {
        }

        return value;
    }

    public int getTypeFormatId() {
        return StoredFormatIds.BASIC_UUID;
    }

    private static void writeMSB(char[] data, int offset, long value, int nbytes) {
        for (int i = nbytes - 1; i >= 0; i--) {
            long b = (value & (255L << (8 * i))) >>> (8 * i);

            int c = (int) ((b & 0xf0) >> 4);
            data[offset++] = (char) (c < 10 ? c + '0' : (c - 10) + 'a');
            c = (int) (b & 0x0f);
            data[offset++] = (char) (c < 10 ? c + '0' : (c - 10) + 'a');
        }
    }

    public String toString() {
        return stringWorkhorse('-');
    }

    public String stringWorkhorse(char separator) {
        char[] data = new char[36];

        writeMSB(data, 0, (long) sequence, 4);

        int offset = 8;
        if (separator != 0) data[offset++] = separator;

        long ltimemillis = timemillis;
        writeMSB(data, offset, (ltimemillis & 0x0000ffff00000000L) >>> 32, 2);
        offset += 4;
        if (separator != 0) data[offset++] = separator;
        writeMSB(data, offset, (ltimemillis & 0x00000000ffff0000L) >>> 16, 2);
        offset += 4;
        if (separator != 0) data[offset++] = separator;
        writeMSB(data, offset, (ltimemillis & 0x000000000000ffffL), 2);
        offset += 4;
        if (separator != 0) data[offset++] = separator;
        writeMSB(data, offset, majorId, 6);
        offset += 12;

        return new String(data, 0, offset);
    }

}
