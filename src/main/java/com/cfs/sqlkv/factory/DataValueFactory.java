package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.sql.types.RefDataValue;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.NumberDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 13:53
 */
public interface DataValueFactory {

    public DataValueDescriptor getNull(int formatId);

    public NumberDataValue getDataValue(Integer value, NumberDataValue previous);

    public NumberDataValue getDataValue(int value, NumberDataValue previous);

    public RefDataValue getDataValue(TableRowLocation value, RefDataValue previous);
}
