package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.heap.OpenTable;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 12:59
 */
public abstract class GenericController {

    protected OpenTable open_table;
    public void init(OpenTable open_table) {
        this.open_table = open_table;
    }

    public abstract TableRowLocation newRowLocationTemplate();
}
