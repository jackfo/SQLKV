package com.cfs.sqlkv.compile.result;


import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-03 12:18
 */
public class RowResultSet extends NoPutResultSetImpl implements CursorResultSet {
    private boolean next;
    private ExecRow cachedRow;
    private GeneratedMethod row;

    public RowResultSet(Activation activation, int resultSetNumber, double optimizerEstimatedRowCount, double optimizerEstimatedCost) {
        super(activation, resultSetNumber, optimizerEstimatedRowCount, optimizerEstimatedCost);
    }

    public RowResultSet(Activation activation, GeneratedMethod row, int resultSetNumber) {
        super(activation, resultSetNumber, 0, 0);
        this.row = row;
    }

    public int rowsReturned;

    @Override
    public ExecRow getNextRowCore() {
        currentRow = null;
        if (isOpen) {
            if (!next) {
                next = true;
                if (cachedRow != null) {
                    currentRow = cachedRow;
                } else if (row != null) {
                    currentRow = (ExecRow) row.invoke(activation);
                }
                rowsReturned++;
            }
            setCurrentRow(currentRow);
        }
        return currentRow;

    }

    public void openCore() {
        next = false;
        isOpen = true;
    }

    public TableRowLocation getRowLocation() {
        return null;
    }
}
