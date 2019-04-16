package com.cfs.sqlkv.row;


import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 09:50
 */
public interface Row {

    /**设置列当前位置的值*/
    void setColumn (int position, DataValueDescriptor value);

    public int nColumns();

    DataValueDescriptor	getColumn (int position)  ;
}
