package com.cfs.sqlkv.client;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 13:00
 */
public class SQL {

    public static final String module = SQL.class.getName();

    Connection theConnection = null;

    SQLResult executeImmediate(String stmt) throws SQLException{
        Statement statement = null;
        //检测是否存在连接
        haveConnection();
        statement = theConnection.createStatement();
        statement.execute(stmt);
        return new SQLStatementResult(statement,false);
    }

    void haveConnection() {
        if(theConnection==null){
            throw SQLKVException.objectWasNull("theConnection",module);
        }
    }
}
