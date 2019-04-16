package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.compile.result.DMLWriteResultSet;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.TargetResultSet;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-12 12:52
 */
public class UpdateResultSet extends DMLWriteResultSet implements TargetResultSet {

    private TransactionManager transactionManager;
    private ExecRow newBaseRow;
    private ExecRow row;
    public UpdateConstantAction constants;
    public NoPutResultSet savedSource;
    private RowChanger rowChanger;
    private GeneratedMethod generationClauses;
    private GeneratedMethod checkGM;
    private long heapConglom;
    private int numberOfBaseColumns;
    private ExecRow oldDeletedRow;
    private int resultWidth;
    private int numOpens;
    public long rowCount;

    public UpdateResultSet(NoPutResultSet source, GeneratedMethod generationClauses, GeneratedMethod checkGM, Activation activation) {
        super(activation);
        transactionManager = activation.getTransactionManager();
        this.sourceResultSet = source;
        this.generationClauses = generationClauses;
        this.checkGM = checkGM;
        constants = (UpdateConstantAction) constantAction;
        heapConglom = constants.conglomId;
        resultDescription = source.getResultDescription();
        resultWidth = resultDescription.getColumnCount();
        numberOfBaseColumns = (resultWidth - 1) / 2;
        newBaseRow = RowUtil.getEmptyValueRow(numberOfBaseColumns, lcc);
    }

    @Override
    public void setup() {
        boolean firstOpen = (rowChanger == null);
        if (firstOpen) {
            Conglomerate conglomerate = constants.conglomerate;
            int numberOfColumns = constants.numColumns;
            int[] changedColumnIds = constants.changedColumnIds;
            rowChanger = new RowChanger(heapConglom, openConglomerateScratchSpace, conglomerate, numberOfColumns,changedColumnIds, transactionManager, activation);
        }
        rowChanger.open();
        if (numOpens++ == 0) {
            sourceResultSet.openCore();
        } else {
            sourceResultSet.reopenCore();
        }

    }

    @Override
    public void open() {
        setup();
        collectAffectedRows();
        rowChanger.finish();
        cleanUp();
    }


    public boolean collectAffectedRows() {
        boolean rowsFound = false;
        row = getNextRowCore(sourceResultSet);
        if (row != null) {
            rowsFound = true;
        }

        /**
         * 对所有满足条件的行数据进行修改
         * */
        while (row != null) {
            TableRowLocation baseRowLocation = (TableRowLocation) (row.getColumn(resultWidth)).getObject();
            RowUtil.copyRefColumns(newBaseRow, row, numberOfBaseColumns, numberOfBaseColumns);
            sourceResultSet.updateRow(newBaseRow, rowChanger);
            rowChanger.updateRow(row, newBaseRow, baseRowLocation);
            rowCount++;
            row = getNextRowCore(sourceResultSet);
        }

        return rowsFound;
    }


    @Override
    public void cleanUp() {
        numOpens = 0;
        if (sourceResultSet != null) {
            sourceResultSet.close();
            // cache source across open()s
        }
        if (rowChanger != null)
            rowChanger.close();
        close();

    }

    @Override
    public void close() {

    }

    @Override
    protected ExecRow getNextRowCore(NoPutResultSet source) {
        ExecRow nextRow = super.getNextRowCore(source);
        return nextRow;
    }


}
