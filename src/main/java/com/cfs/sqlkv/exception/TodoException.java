package com.cfs.sqlkv.exception;

/**
 * @Description
 * @auther zhengxiaokang
 * @Email zhengxiaokang@qq.com
 * @create 2018-12-15 11:41
 */
public class TodoException extends Exception{

    public static final Exception e = new TodoException("尚未进行实现");

    private TodoException(String message){
       super(message);
    }

}
