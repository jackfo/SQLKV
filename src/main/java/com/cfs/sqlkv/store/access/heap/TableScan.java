package com.cfs.sqlkv.store.access.heap;

import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.conglomerate.GenericScanController;
import com.cfs.sqlkv.store.access.conglomerate.RowPosition;
import com.cfs.sqlkv.store.access.conglomerate.ScanManager;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-26 17:44
 */
public class TableScan extends GenericScanController implements ScanManager {

    public TableScan() {
    }

    private DataValueDescriptor[][] fetchNext_one_slot_array = new DataValueDescriptor[1][];


    /**
     * 获取扫描的下一条数据
     */
    @Override
    public boolean fetchNext(DataValueDescriptor[] fetch_row) {
        if (fetch_row == null) {
            fetchNext_one_slot_array[0] = RowUtil.EMPTY_ROW;
        } else {
            fetchNext_one_slot_array[0] = fetch_row;
        }
        if (fetchRows(fetchNext_one_slot_array, null, null, 1, null) == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public int fetchNextGroup(DataValueDescriptor[][] row_array, TableRowLocation[] rowloc_array) {
        int returnValue = fetchRows(row_array, rowloc_array, null, row_array.length, null);
        return returnValue;
    }

    protected void setRowLocationArray(TableRowLocation[] rowloc_array, int index, RowPosition pos) {
        if (rowloc_array[index] == null) {
            rowloc_array[index] = new TableRowLocation(pos.current_rh);
        } else {
             rowloc_array[index].setFrom(pos.current_rh);
        }
    }


    @Override
    public void fetchLocation(TableRowLocation destRowLocation) {
        if (open_table.getContainer() == null || scan_position.current_rh == null) {
           throw new RuntimeException("HEAP_SCAN_NOT_POSITIONED");
        }
        TableRowLocation hrl = destRowLocation;
        hrl.setFrom(scan_position.current_rh);
    }
}
