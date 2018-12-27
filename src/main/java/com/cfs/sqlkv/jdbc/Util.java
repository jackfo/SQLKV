package com.cfs.sqlkv.jdbc;

import java.sql.SQLException;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 17:03
 */
public class Util {

    public static SQLException generateCsSQLException(String error, Object... args) {
        return generateCsSQLException(error, null, args);
    }

    static SQLException generateCsSQLException(String error, Throwable t, Object... args) {
        return ExceptionFactory.getInstance().getSQLException(
                error, (SQLException) null, t, args);
    }

}
