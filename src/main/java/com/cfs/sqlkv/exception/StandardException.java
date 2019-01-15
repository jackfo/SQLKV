package com.cfs.sqlkv.exception;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 20:26
 */
public class StandardException extends Exception {

    public static StandardException newException(String messageId, Object... args) {
        return newException(messageId, (Throwable) null, args);
    }
}
