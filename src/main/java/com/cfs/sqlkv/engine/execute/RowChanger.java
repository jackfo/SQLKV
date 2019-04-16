package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.OpenConglomerateScratchSpace;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-09 12:41
 */
public class RowChanger {
    public boolean isOpen = false;
    public long heapConglom;
    public OpenConglomerateScratchSpace openConglomerateScratchSpace;
    public Conglomerate conglomerate;
    private final Activation activation;
    public TransactionManager transactionManager;
    private ConglomerateController baseCC;
    private TableRowLocation baseRowLocation;
    private DataValueDescriptor[] sparseRowArray;
    private int[] partialChangedColumnIds;
    public FormatableBitSet changedColumnBitSet;
    public int[] changedColumnIds;

    public RowChanger(long heapConglom, OpenConglomerateScratchSpace openConglomerateScratchSpace,
                      Conglomerate conglomerate, int numberOfColumns, int[] changedColumnIdsInput, TransactionManager transactionManager,
                      Activation activation) {
        this.heapConglom = heapConglom;
        this.openConglomerateScratchSpace = openConglomerateScratchSpace;
        this.conglomerate = conglomerate;
        this.transactionManager = transactionManager;
        this.activation = activation;
        changedColumnBitSet = new FormatableBitSet(numberOfColumns);
        this.changedColumnIds = changedColumnIdsInput;
        if (changedColumnIds != null) {
            sparseRowArray = new DataValueDescriptor[changedColumnIds[changedColumnIds.length - 1] + 1];
            for (int i = 0; i < changedColumnIds.length; i++) {
                changedColumnBitSet.grow(changedColumnIds[i]);
                changedColumnBitSet.set(changedColumnIds[i] - 1);
            }
        }
    }

    public TableRowLocation insertRow(ExecRow baseRow, boolean getRL) {
        baseCC.insert(baseRow.getRowArray());
        return baseRowLocation;
    }

    public void open() {
        LanguageConnectionContext lcc = null;
        if (activation != null) {
            lcc = activation.getLanguageConnectionContext();
        }
        if (transactionManager == null) {
            transactionManager = lcc.getTransactionCompile();
        }
        if (conglomerate != null) {
            baseCC = transactionManager.openConglomerate(conglomerate);
        }
        isOpen = true;
    }

    public void close() {
        if (baseCC != null) {
            baseCC = null;
        }
        isOpen = false;
    }

    public void finish() {

    }

    /**
     * 更新行数据
     */
    public void updateRow(ExecRow oldBaseRow, ExecRow newBaseRow, TableRowLocation baseRowLocation) {
        if (changedColumnBitSet != null) {
            DataValueDescriptor[] baseRowArray = newBaseRow.getRowArray();
            int[] changedColumnArray = (partialChangedColumnIds == null) ?
                    changedColumnIds : partialChangedColumnIds;
            int nextColumnToUpdate = -1;
            for (int i = 0; i < changedColumnArray.length; i++) {
                int copyFrom = changedColumnArray[i] - 1;
                nextColumnToUpdate = changedColumnBitSet.anySetBit(nextColumnToUpdate);
                sparseRowArray[nextColumnToUpdate] = baseRowArray[copyFrom];
            }
        } else {
            sparseRowArray = newBaseRow.getRowArray();
        }
        baseCC.replace(baseRowLocation, sparseRowArray, changedColumnBitSet);
    }

    public void deleteRow(ExecRow baseRow, TableRowLocation baseRowLocation) {
        baseCC.delete(baseRowLocation);
    }
}
