package com.cfs.sqlkv.jdbc;

import java.sql.SQLException;

/**
 * @Description 创建相应的SQL异常
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-25 19:08
 */
public class SQLExceptionFactory extends ExceptionFactory{

    @Override
    public SQLException getSQLException(String message, String messageId, SQLException next, int severity, Throwable cause, Object... args) {
        throw new RuntimeException("SQL语句出现异常");
    }

    @Override
    public SQLException getSQLException(String messageId, SQLException next, Throwable cause, Object... args) {
        throw new RuntimeException("SQL语句出现异常");
    }
}
