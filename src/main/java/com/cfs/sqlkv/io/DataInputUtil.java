package com.cfs.sqlkv.io;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-01 21:09
 */
public class DataInputUtil {
    public static void skipFully(DataInput in, int skippedBytes) throws IOException {
        if (in == null) {
            throw new NullPointerException();
        }

        while (skippedBytes > 0) {
            int skipped = in.skipBytes(skippedBytes);
            if (skipped == 0) {
                in.readByte();
                skipped++;
            }
            skippedBytes -= skipped;
        }
    }
}
