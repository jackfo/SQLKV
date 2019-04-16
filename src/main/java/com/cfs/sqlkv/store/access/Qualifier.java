package com.cfs.sqlkv.store.access;

import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description 用于限定列的结构
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 20:41
 */
public interface Qualifier {

    public static final int VARIANT = 0;
    public static final int SCAN_INVARIANT = 1;
    public static final int QUERY_INVARIANT = 2;
    public static final int CONSTANT = 3;

    public int getColumnId();

    public DataValueDescriptor getOrderable();

    public int getOperator();

    public void reinitialize();

    public void clearOrderableCache();
}
