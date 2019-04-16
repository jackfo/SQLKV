package com.cfs.sqlkv.store.access;


import com.cfs.sqlkv.engine.execute.RowUtil;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.GenericController;
import com.cfs.sqlkv.store.access.conglomerate.OpenConglomerateScratchSpace;
import com.cfs.sqlkv.store.access.conglomerate.RowPosition;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-01 21:17
 */
public abstract class GenericConglomerateController extends GenericController implements ConglomerateController {

    public void close() {
    }


    @Override
    public boolean fetch(TableRowLocation tableRowLocation, DataValueDescriptor[] row, FormatableBitSet validColumns) {
        OpenConglomerateScratchSpace openConglomerateScratchSpace = open_table.getRuntimeMem();
        RowPosition pos = openConglomerateScratchSpace.get_scratch_row_position();
        getRowPositionFromRowLocation(tableRowLocation, pos);
        FetchDescriptor fetchDescriptor = new FetchDescriptor(row.length);
        if (!open_table.latchPage(pos)) {
            return false;
        }
        RecordId recordId = pos.current_page.fetchFromSlot(pos.current_rh, pos.current_slot, row, fetchDescriptor, false);
        return recordId != null ? true : false;
    }


    @Override
    public boolean replace(TableRowLocation loc, DataValueDescriptor[] row, FormatableBitSet validColumns) {
        RowPosition pos = open_table.getRuntimeMem().get_scratch_row_position();
        getRowPositionFromRowLocation(loc, pos);
        if (!open_table.latchPage(pos)) {
            return false;
        }
        if (pos.current_rh == null) {
            pos.current_rh = pos.current_page.fetchFromSlot(null, pos.current_slot, RowUtil.EMPTY_ROW,
                    RowUtil.EMPTY_ROW_FETCH_DESCRIPTOR, true);
        }
        boolean ret_val = true;
        if (pos.current_page.isDeletedAtSlot(pos.current_slot)) {
            ret_val = false;
        } else {
            pos.current_page.updateAtSlot(pos.current_slot, row, validColumns);
        }
        pos.current_page.unlatch();
        return ret_val;
    }

    protected void getRowPositionFromRowLocation(TableRowLocation row_loc, RowPosition pos) {
        throw new RuntimeException("The feature is not implemented.");
    }

    @Override
    public boolean delete(TableRowLocation loc) {
        RowPosition pos = open_table.getRuntimeMem().get_scratch_row_position();

        getRowPositionFromRowLocation(loc, pos);

        if (!open_table.latchPage(pos)) {
            return false;
        }
        if (pos.current_rh == null) {
            pos.current_rh = pos.current_page.fetchFromSlot(null, pos.current_slot, RowUtil.EMPTY_ROW, RowUtil.EMPTY_ROW_FETCH_DESCRIPTOR, true);
        }
        boolean ret_val = true;
        if (pos.current_page.isDeletedAtSlot(pos.current_slot)) {
            ret_val = false;
        } else {
            pos.current_page.deleteAtSlot(pos.current_slot, true);
        }
        pos.current_page.unlatch();
        return ret_val;
    }
}
