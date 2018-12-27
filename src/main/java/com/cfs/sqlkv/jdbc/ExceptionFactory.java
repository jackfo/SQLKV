package com.cfs.sqlkv.jdbc;

import java.sql.SQLException;

/**
 * @Description 用来创建SQL异常的工厂方式
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 17:03
 */
public abstract class ExceptionFactory {

    /**单例工厂模式*/
    private static final ExceptionFactory INSTANCE;
    static {
        String impl = "org.apache.derby.impl.jdbc.SQLExceptionFactory";
        ExceptionFactory factory = null;
        try {
            Class<?> clazz = Class.forName(impl);
            factory = (ExceptionFactory) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        INSTANCE = factory;
    }

    public static ExceptionFactory getInstance() {
        return INSTANCE;
    }

    public abstract SQLException getSQLException(String message, String messageId, SQLException next, int severity, Throwable cause, Object... args);

    public abstract SQLException getSQLException(String messageId, SQLException next, Throwable cause, Object... args);
}
