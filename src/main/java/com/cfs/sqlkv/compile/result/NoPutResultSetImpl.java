package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.compile.result.BasicNoPutResultSetImpl;

import com.cfs.sqlkv.engine.execute.RowChanger;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 18:48
 */
public class NoPutResultSetImpl extends BasicNoPutResultSetImpl {

    public final int resultSetNumber;

    public NoPutResultSetImpl(Activation activation,
                              int resultSetNumber,
                              double optimizerEstimatedRowCount,
                              double optimizerEstimatedCost) {
        super(null, activation, optimizerEstimatedRowCount, optimizerEstimatedCost);
        this.resultSetNumber = resultSetNumber;
    }

    public final void setCurrentRow(ExecRow row) {
        activation.setCurrentRow(row, resultSetNumber);
        currentRow = row;
    }

    @Override
    public ExecRow getNextRowCore() {
        return null;
    }


    @Override
    public void openCore() {

    }

    @Override
    public boolean isForUpdate() {
        return false;
    }

    @Override
    public void reopenCore() {

    }

    @Override
    public void setTargetResultSet(TargetResultSet trs) {

    }

    @Override
    public ResultDescription getResultDescription() {
        return activation.getResultDescription();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void close() {

    }

    public void updateRow(ExecRow row, RowChanger rowChanger) {

    }

    public void markRowAsDeleted(){
    }
}
