package com.cfs.sqlkv.sql.types;

import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 15:27
 */
public interface BooleanDataValue extends DataValueDescriptor {

    public boolean getBoolean();

    public BooleanDataValue and(BooleanDataValue otherValue);

    public BooleanDataValue or(BooleanDataValue otherValue);

    public BooleanDataValue is(BooleanDataValue otherValue);

    public boolean equals(boolean value);
}
