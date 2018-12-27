package com.cfs.sqlkv.client;

import java.sql.Statement;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 13:11
 */
public class SQLStatementResult implements SQLResult{

    Statement statement;
    boolean closeWhenDone;

    public SQLStatementResult(Statement statement,boolean c){
        this.statement = statement;
        closeWhenDone = c;
    }
}
