package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.context.StatementContext;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:12
 */
abstract class NoRowsResultSetImpl implements ResultSet {


    public final Activation activation;
    public final LanguageConnectionContext lcc;

    public NoRowsResultSetImpl(Activation activation) {
        this.activation = activation;
        lcc = activation.getLanguageConnectionContext();
    }

    public final boolean returnsRows() {
        return false;
    }

    private boolean isOpen;

    public void setup() {
        isOpen = true;
    }

    public boolean isClosed() {
        return !isOpen;
    }

    public ResultDescription getResultDescription() {
        return (ResultDescription) null;
    }

    @Override
    public final ExecRow getNextRow() {
        throw new RuntimeException("getNextRow");
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void close() {

    }
}
