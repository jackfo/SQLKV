package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.engine.execute.InsertConstantAction;

import com.cfs.sqlkv.engine.execute.RowChanger;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-02 21:14
 */
public class InsertResultSet extends DMLWriteResultSet implements TargetResultSet {

    protected DataDictionary dd;
    InsertConstantAction constants;
    public long rowCount;
    private int numOpens;
    private final String schemaName;
    private final String tableName;
    protected ResultDescription resultDescription;
    private ExecRow row;
    private boolean firstExecute;
    private RowChanger rowChanger;
    private long heapConglom;
    private TransactionManager transactionManager;


    /**
     * 判定是否是批量插入
     */
    protected boolean bulkInsert;


    public InsertResultSet(NoPutResultSet resultSet, String schemaName, String tableName, Activation activation) {
        super(activation);
        sourceResultSet = resultSet;
        this.schemaName = schemaName;
        this.tableName = tableName;
        resultDescription = sourceResultSet.getResultDescription();
        constants = (InsertConstantAction) constantAction;
        heapConglom = constants.conglomId;
        resultDescription = sourceResultSet.getResultDescription();
    }

    @Override
    public void open() {
        setup();
        firstExecute = (rowChanger == null);
        dd = lcc.getDataDictionary();
        rowCount = 0L;
        if (numOpens++ == 0) {
            sourceResultSet.openCore();
        } else {
            sourceResultSet.reopenCore();
        }
        row = getNextRowCore(sourceResultSet);
        normalInsertCore(lcc, firstExecute);
    }


    @Override
    public boolean isClosed() {
        return false;
    }


    private void normalInsertCore(LanguageConnectionContext lcc, boolean firstExecute) {
        if (firstExecute) {
            Conglomerate conglomerate = constants.conglomerate;
            rowChanger = new RowChanger(heapConglom, openConglomerateScratchSpace, conglomerate, 0, null,transactionManager, activation);
        }
        rowChanger.open();
        while (row != null) {
            rowChanger.insertRow(row, false);
            rowCount++;
            row = getNextRowCore(sourceResultSet);
        }
    }

    @Override
    protected ExecRow getNextRowCore(NoPutResultSet source) {
        ExecRow nextRow = super.getNextRowCore(source);
        return nextRow;
    }
}
