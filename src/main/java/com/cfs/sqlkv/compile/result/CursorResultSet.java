package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.store.access.heap.TableRowLocation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 17:15
 */
public interface CursorResultSet {

    public TableRowLocation getRowLocation();
}
