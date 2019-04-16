package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.engine.execute.RowChanger;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 17:15
 */
public interface NoPutResultSet extends ResultSet {
    public void openCore();

    public boolean isForUpdate();

    public void reopenCore();

    public void setTargetResultSet(TargetResultSet trs);

    public ExecRow getNextRowCore();

    public Activation getActivation();

    public void updateRow(ExecRow row, RowChanger rowChanger);

    /**标记行删除*/
    public void markRowAsDeleted();
}
