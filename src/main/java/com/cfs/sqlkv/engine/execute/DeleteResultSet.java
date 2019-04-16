package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.compile.result.DMLWriteResultSet;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-16 10:27
 */
public class DeleteResultSet extends DMLWriteResultSet {

    private TransactionManager transactionManager;
    public DeleteConstantAction constants;
    protected NoPutResultSet source;
    private ExecRow row;
    protected RowChanger rc;
    private boolean firstExecute;
    private int numOpens;
    public int	numberOfBaseColumns = 0;

    public DeleteResultSet(NoPutResultSet source, Activation activation) {
        super(activation);
        this.source = source;
        transactionManager = activation.getTransactionManager();
        constants = (DeleteConstantAction) constantAction;
        if (source != null) {
            resultDescription = source.getResultDescription();
        } else {
            resultDescription = constants.resultDescription;
        }
    }

    @Override
    public void open() {
        setup();
        collectAffectedRows();
        cleanUp();
    }

    @Override
    public void setup() {
        super.setup();
        firstExecute = (rc == null);

        if (numOpens++ == 0) {
            source.openCore();
        } else {
            source.reopenCore();
        }
        if (firstExecute) {
            long heapConglom = constants.conglomId;
            Conglomerate conglomerate = constants.conglomerate;
            rc = new RowChanger(heapConglom, openConglomerateScratchSpace, conglomerate, constants.numColumns, null, transactionManager, activation);
        }
        rc.open();
        row = getNextRowCore(source);
        if (resultDescription == null) {
            numberOfBaseColumns = (row == null) ? 0 : row.nColumns();
        } else {
            numberOfBaseColumns = resultDescription.getColumnCount();
        }

    }


    public boolean collectAffectedRows() {
        DataValueDescriptor rlColumn;
        TableRowLocation baseRowLocation;
        boolean rowsFound = false;
        while (row != null) {
            rowsFound = true;
            rlColumn = row.getColumn(row.nColumns());
            baseRowLocation = (TableRowLocation) (rlColumn).getObject();
            rc.deleteRow(row, baseRowLocation);
            source.markRowAsDeleted();
            row = getNextRowCore(source);
        }
        return rowsFound;
    }

}
