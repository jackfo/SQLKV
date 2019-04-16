package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.engine.execute.WriteCursorConstantAction;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.conglomerate.OpenConglomerateScratchSpace;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-02 21:15
 */
public abstract class DMLWriteResultSet extends NoRowsResultSetImpl {
    public OpenConglomerateScratchSpace openConglomerateScratchSpace;
    protected NoPutResultSet sourceResultSet;

    public ResultDescription resultDescription;

    public DMLWriteResultSet(Activation activation) {
        super(activation);
        this.constantAction = (WriteCursorConstantAction) activation.getConstantAction();
    }


    protected ExecRow getNextRowCore(NoPutResultSet source) {
        ExecRow row = source.getNextRowCore();
        return row;
    }

    @Override
    public ResultDescription getResultDescription() {
        return resultDescription;
    }

    protected WriteCursorConstantAction constantAction;
}
