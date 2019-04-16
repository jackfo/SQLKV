package com.cfs.sqlkv.store.access.conglomerate;


import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 13:00
 */
public interface ScanController {

    public static final int GE = 1;
    public static final int GT = -1;

    public int fetchNextGroup(DataValueDescriptor[][] row_array, TableRowLocation[] rowloc_array);

    public boolean fetchNext(DataValueDescriptor[] destRow);

    public void fetch(DataValueDescriptor[] destRow);

    public void close();

    public TableRowLocation newRowLocationTemplate();

    public void fetchLocation(TableRowLocation destRowLocation);
}
