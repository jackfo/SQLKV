package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.engine.EmbedResultSet;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.ScanController;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 18:49
 */
public class BulkTableScanResultSet extends TableScanResultSet implements CursorResultSet {

    private int curRowPosition;
    private int numRowsInArray;


    private TableRowLocation[] rowLocations;
    private DataValueDescriptor[][] rowArray;
    protected boolean fetchRowLocations = false;
    public long openTime;
    private int baseColumnCount;
    private int resultColumnCount;

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
    public BulkTableScanResultSet(long conglomId,
                           Heap scoci, Activation activation,
                           int resultRowTemplate,
                           int resultSetNumber,
                           GeneratedMethod startKeyGetter, int startSearchOperator,
                           GeneratedMethod stopKeyGetter, int stopSearchOperator,
                           boolean sameStartStopPosition,
                           Qualifier[][] qualifiers,
                           String tableName,
                           String userSuppliedOptimizerOverrides,
                           String indexName,
                           boolean isConstraint,
                           boolean forUpdate,
                           int colRefItem,
                           int indexColItem,
                           int lockMode,
                           boolean tableLocked,
                           int isolationLevel,
                           int rowsPerRead,
                           boolean disableForHoldable,
                           boolean oneRowScan,
                           double optimizerEstimatedRowCount,
                           double optimizerEstimatedCost) {
        super(conglomId,
                scoci,
                activation,
                resultRowTemplate,
                resultSetNumber,
                startKeyGetter,
                startSearchOperator,
                stopKeyGetter,
                stopSearchOperator,
                sameStartStopPosition,
                qualifiers,
                tableName,
                userSuppliedOptimizerOverrides,
                indexName,
                isConstraint,
                forUpdate,
                colRefItem,
                indexColItem,
                lockMode,
                tableLocked,
                isolationLevel,
                rowsPerRead,
                oneRowScan,
                optimizerEstimatedRowCount,
                optimizerEstimatedCost);

        setRowLocationsState();
        if (fetchRowLocations) {
            resultColumnCount = accessedCols == null ? candidate.nColumns() : accessedCols.getNumBitsSet();
            baseColumnCount = candidate.nColumns() - 1;
            candidate.setRowArray(lopOffRowLocation());
            if (accessedCols == null) {
                accessedCols = new FormatableBitSet(baseColumnCount);
                for (int i = 0; i < baseColumnCount; i++) {
                    accessedCols.set(i);
                }
            } else {
                FormatableBitSet newCols = new FormatableBitSet(baseColumnCount);
                for (int i = 0; i < baseColumnCount; i++) {
                    if (accessedCols.isSet(i)) {
                        newCols.set(i);
                    }
                }
                accessedCols = newCols;
            }
        }
    }

    public BulkTableScanResultSet(Heap heap,long conglomId,Activation activation, int resultSetNumber, int resultRowTemplate,int rowsPerRead,String tableNmae,Qualifier[][] qualifiers) {


        this(conglomId, null, activation, resultRowTemplate, resultSetNumber,
                null, 0, null, 0, false, qualifiers, tableNmae, null, null, false, false, 0, 0, 0, false, 0, rowsPerRead, false, false, 0,0);
        this.heap = heap;
    }


    @Override
    public void openCore() {
        super.openCore();
        beginTime = System.currentTimeMillis();
        rowArray = new DataValueDescriptor[rowsPerRead][];
        if (fetchRowLocations) {
            rowLocations = new TableRowLocation[rowsPerRead];
        }
        rowArray[0] = candidate.getRowArrayClone();
        numRowsInArray = 0;
        curRowPosition = -1;
        openTime += getElapsedMillis(beginTime);
    }

    private DataValueDescriptor[] lopOffRowLocation(){
        DataValueDescriptor[] temp = candidate.getRowArrayClone();
        int count = temp.length - 1;
        DataValueDescriptor[] result = new DataValueDescriptor[count];
        for (int i = 0; i < count; i++) {
            result[i] = temp[i];
        }
        return result;
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

    public int numOpens;
    public int rowsSeen;

    protected long rowsThisScan;

    @Override
    public ExecRow getNextRowCore() {
        ExecRow result = null;
        beginTime = System.currentTimeMillis();
        if (isOpen && scanControllerOpened) {
            currentRow = getCompactRow(candidate, accessedCols, isKeyed);
        }

        if (curRowPosition >= numRowsInArray - 1) {
            reloadArray();
        }
        if (++curRowPosition < numRowsInArray) {
            //设置候选行的数据描述
            candidate.setRowArray(rowArray[curRowPosition]);
            //将候选行的数据描述设置到当前行
            currentRow = setCompactRow(candidate, currentRow);
            rowsSeen++;
            rowsThisScan++;
            result = currentRow;
        }
        setCurrentRow(result);
        return result;
    }


    private int reloadArray() {
        curRowPosition = -1;
        numRowsInArray = scanController.fetchNextGroup(rowArray, rowLocations);
        return numRowsInArray;
    }


    @Override
    public boolean canGetInstantaneousLocks() {
        return false;
    }


    @Override
    public boolean isClosed() {
        return false;
    }
}
