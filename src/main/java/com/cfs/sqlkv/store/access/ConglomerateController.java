package com.cfs.sqlkv.store.access;


import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-04 20:08
 */
public interface ConglomerateController {

    int ROWISDUPLICATE = 1;

    void insertAndFetchLocation(DataValueDescriptor[] row, TableRowLocation destRowLocation);

    int insert(DataValueDescriptor[] row);

    void close();


    boolean fetch(TableRowLocation loc, DataValueDescriptor[] destRow, FormatableBitSet validColumns);

    TableRowLocation newRowLocationTemplate();

    boolean replace(TableRowLocation loc, DataValueDescriptor[] row, FormatableBitSet validColumns);

    boolean delete(TableRowLocation loc);
}
