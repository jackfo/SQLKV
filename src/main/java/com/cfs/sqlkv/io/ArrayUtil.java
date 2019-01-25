package com.cfs.sqlkv.io;

import java.util.Arrays;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 21:04
 */
public class ArrayUtil {

    public final static byte[] EMPTY_BYTE_ARRAY = {};

    public  static <T> T[] copy( T[] original ) {
        return (original == null) ? null : Arrays.copyOf(original, original.length);
    }

    public  static  byte[] copy( byte[] original ) {
        return (original == null) ? null : (byte[]) original.clone();
    }
}
