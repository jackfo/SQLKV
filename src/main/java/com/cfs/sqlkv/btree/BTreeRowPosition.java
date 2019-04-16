package com.cfs.sqlkv.btree;

import com.cfs.sqlkv.btree.controlrow.LeafControlRow;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.RowPosition;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 16:26
 */
public class BTreeRowPosition extends RowPosition {

    public DataValueDescriptor[] current_positionKey;
    public LeafControlRow current_leaf;
    protected LeafControlRow next_leaf;
    public DataValueDescriptor[] current_lock_template;
    public TableRowLocation current_lock_row_loc;

    private final BTreeScan parent;
    private DataValueDescriptor[] positionKey_template;
    private FetchDescriptor savedFetchDescriptor;

    public BTreeRowPosition(BTreeScan parent) {
        this.parent = parent;
    }

    public void init() {
        super.init();
        current_leaf = null;
        current_positionKey = null;
    }

    public final void unlatch() {
        if (current_leaf != null) {
            current_leaf.release();
            current_leaf = null;
        }
        current_slot = Page.INVALID_SLOT_NUMBER;
    }
    public DataValueDescriptor[] getKeyTemplate()   {
        if (positionKey_template == null) {
            positionKey_template = parent.getRuntimeMem().get_row_for_export(parent.getRawTran());
        }
        return positionKey_template;
    }

    public FetchDescriptor getFetchDescriptorForSaveKey(int[] vcols, int fullLength) {
        if (savedFetchDescriptor == null) {
            FormatableBitSet columns = new FormatableBitSet(fullLength);
            for (int i = 0; i < vcols.length; i++) {
                if (vcols[i] == 0) {
                    columns.set(i);
                }
            }
            for (int i = vcols.length; i < fullLength; i++) {
                columns.set(i);
            }
            savedFetchDescriptor = new FetchDescriptor(fullLength, columns, null);
        }
        return savedFetchDescriptor;
    }

}
