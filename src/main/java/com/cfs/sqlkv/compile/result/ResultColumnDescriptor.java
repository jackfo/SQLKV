package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;

/**
 * 结果集列信息的描述
 * */
public interface ResultColumnDescriptor {

    /**
     * 获取返回结果列的类型
     * @return 列的数据类型
     * */
    DataTypeDescriptor getType();

    /**
     * 获取返回结果列的名称
     * @return 返回列名
     * */
    String getName();

}
