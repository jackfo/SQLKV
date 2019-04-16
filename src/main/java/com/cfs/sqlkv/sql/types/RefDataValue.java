package com.cfs.sqlkv.sql.types;

import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 13:38
 */
public interface RefDataValue extends DataValueDescriptor {

    public void setValue(TableRowLocation theValue);
}
