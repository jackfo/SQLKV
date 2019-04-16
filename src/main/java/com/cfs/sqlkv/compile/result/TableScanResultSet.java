package com.cfs.sqlkv.compile.result;


import com.cfs.sqlkv.engine.execute.GenericQualifier;
import com.cfs.sqlkv.engine.execute.RowChanger;
import com.cfs.sqlkv.row.ExecIndexRow;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.ScanController;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 18:48
 */
public class TableScanResultSet extends ScanResultSet implements CursorResultSet {
    protected boolean qualify;
    public int rowsPerRead;
    protected boolean firstScan = true;
    public Qualifier[][] qualifiers;
    public ScanController scanController;
    /**
     * 是否是主键扫描
     */
    protected boolean isKeyed;

    protected boolean nextDone;

    public Heap heap;
    protected ExecIndexRow startPosition;
    protected ExecIndexRow stopPosition;


    public int startSearchOperator;
    public int stopSearchOperator;

    protected boolean scanControllerOpened;

    public boolean forUpdate;

    public boolean oneRowScan;

    /**
     * @param activation                 激活器
     * @param resultSetNumber            结果集的数目
     * @param resultRowTemplate          identifier of saved object for row template
     * @param lockMode                   lock mode (record or table)
     * @param tableLocked                true if marked as table locked in SYS.SYSTABLES
     * @param isolationLevel             language isolation level for the result set
     * @param colRefItem                 Identifier of saved object for accessedCols,
     *                                   -1 if need to fetch all columns.
     * @param optimizerEstimatedRowCount estimated row count
     * @param optimizerEstimatedCost     estimated cost
     */

    public TableScanResultSet(long conglomId, Heap heap, Activation activation, int resultRowTemplate, int resultSetNumber,
                              GeneratedMethod startKeyGetter, int startSearchOperator, GeneratedMethod stopKeyGetter, int stopSearchOperator,
                              boolean sameStartStopPosition, Qualifier[][] qualifiers, String tableName, String userSuppliedOptimizerOverrides,
                              String indexName, boolean isConstraint, boolean forUpdate, int colRefItem, int indexColItem, int lockMode, boolean tableLocked,
                              int isolationLevel, int rowsPerRead, boolean oneRowScan, double optimizerEstimatedRowCount, double optimizerEstimatedCost) {
        super(activation, resultSetNumber, resultRowTemplate, lockMode, tableLocked, isolationLevel, colRefItem, optimizerEstimatedRowCount, optimizerEstimatedCost);
        this.rowsPerRead = rowsPerRead;
        this.heap = heap;
        this.qualifiers = qualifiers;
        qualify = true;
    }

    public TableScanResultSet(Heap heap, int i, Activation activation, int resultSetNumber, int resultRowTemplate, int rowsPerRead, String noinnner, Qualifier[][] qualifiers) {
        super(activation, resultSetNumber, resultRowTemplate, 0, false, 0, 0, 0, 0);
        this.rowsPerRead = rowsPerRead;
        this.heap = heap;
        this.qualifiers = qualifiers;
        qualify = true;
    }

    @Override
    public void openCore() {
        //获取事务控制器
        TransactionManager tc = activation.getTransactionManager();

        //初始化事务隔离级别
        initIsolationLevel();

        initStartAndStopKey();
        isOpen = true;
        if (firstScan) {
            openScanController(tc);
        }
    }


    public void initStartAndStopKey() {

    }

    protected void openScanController(TransactionManager tc) {
        DataValueDescriptor[] startPositionRow = startPosition == null ? null : startPosition.getRowArray();
        DataValueDescriptor[] stopPositionRow = stopPosition == null ? null : stopPosition.getRowArray();
        if (qualifiers != null) {
            clearOrderableCache(qualifiers);
        }
        if (tc == null) {
            tc = activation.getTransactionManager();
        }
        int openMode = 0;
        if (forUpdate) {
            openMode = TransactionManager.OPENMODE_FORUPDATE;
        }
        scanController = tc.openScan(heap, false, openMode, 0, 0, accessedCols, startPositionRow, startSearchOperator, qualifiers, stopPositionRow, stopSearchOperator);
        scanControllerOpened = true;

    }


    protected void clearOrderableCache(Qualifier[][] qualifiers) {
        if (qualifiers != null) {
            Qualifier qual;
            for (int term = 0; term < qualifiers.length; term++) {
                for (int index = 0; index < qualifiers[term].length; index++) {
                    qual = qualifiers[term][index];
                    qual.clearOrderableCache();
                }
            }
        }
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void close() {
        if (isOpen) {
            clearCurrentRow();
            if (scanController != null) {
                scanController.close();
                scanController = null;
            }
        }

    }

    public void clearCurrentRow() {
        currentRow = null;
    }

    @Override
    public boolean canGetInstantaneousLocks() {
        return false;
    }

    private TableRowLocation rlTemplate;

    @Override
    public TableRowLocation getRowLocation() {
        TableRowLocation rl;
        if (!isOpen) return null;
        if (rlTemplate == null)
            rlTemplate = scanController.newRowLocationTemplate();
        rl = rlTemplate;
        scanController.fetchLocation(rl);
        return rl;
    }

    @Override
    public ExecRow getNextRowCore() {
        if (currentRow == null) {
            currentRow = getCompactRow(candidate, accessedCols, isKeyed);
        }
        ExecRow result = null;
        if (isOpen && !nextDone) {
            nextDone = oneRowScan;
            if (scanControllerOpened) {
                boolean moreRows = true;
                while (true) {
                    if (!(moreRows = loopControl(moreRows))) {
                        break;
                    }
                    result = currentRow;
                    break;

                }
                if (!moreRows) {
                    currentRow = null;
                }
            }

        }
        setCurrentRow(result);
        return result;
    }

    public boolean loopControl(boolean moreRows) {
        return scanController.fetchNext(candidate.getRowArray());
    }
}
