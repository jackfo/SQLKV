package com.cfs.sqlkv.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 15:42
 */
public final class InputStreamUtil {

    private static final int SKIP_FRAGMENT_SIZE = Integer.MAX_VALUE;

    public static int readUnsignedByte(InputStream in) throws IOException {
        int b = in.read();
        if (b < 0){
            throw new EOFException();
        }
        return b;
    }

    public static void readFully(InputStream in, byte b[], int offset, int len) throws IOException {
        do {
            int bytesRead = in.read(b, offset, len);
            if (bytesRead < 0){
                throw new EOFException();
            }
            len -= bytesRead;
            offset += bytesRead;
        } while (len != 0);
    }

    public static int readLoop(InputStream in, byte b[], int offset, int len) throws IOException {
        int firstOffset = offset;
        do {
            int bytesRead = in.read(b, offset, len);
            if (bytesRead <= 0){
                break;
            }
            len -= bytesRead;
            offset += bytesRead;
        } while (len != 0);
        return offset - firstOffset;
    }



}
