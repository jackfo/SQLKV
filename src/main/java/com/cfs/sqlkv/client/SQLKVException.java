package com.cfs.sqlkv.client;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 13:05
 */
public class SQLKVException extends RuntimeException {

    private final static String OBJECT_WAS_NULL = "[%s] %s was null";

    public SQLKVException(String message){
        super(message);
    }

    public static SQLKVException objectWasNull(String objectName,String className){
        return new SQLKVException(String.format(OBJECT_WAS_NULL,className,objectName));
    }


}
