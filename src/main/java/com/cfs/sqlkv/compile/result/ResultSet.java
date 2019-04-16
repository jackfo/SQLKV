package com.cfs.sqlkv.compile.result;


import com.cfs.sqlkv.row.ExecRow;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 19:43
 */
public interface ResultSet {

    public void open();

    public ResultDescription getResultDescription();

    boolean isClosed();

    ExecRow getNextRow();

    boolean returnsRows();

    public void cleanUp();

    public void	close();
}
