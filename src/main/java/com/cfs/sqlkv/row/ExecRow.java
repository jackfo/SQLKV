package com.cfs.sqlkv.row;

import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 09:51
 */
public interface ExecRow extends Row {

    /**
     * 返回存储的数据描述
     */
    public DataValueDescriptor[] getRowArray();

    public void setRowArray(DataValueDescriptor[] rowArray);

    public DataValueDescriptor[] getRowArrayClone();

}
