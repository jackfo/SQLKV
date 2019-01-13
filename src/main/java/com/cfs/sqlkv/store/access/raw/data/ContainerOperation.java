package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 19:02
 */
public class ContainerOperation {

    protected byte operation;

    protected ContainerOperation(BaseContainerHandle hdl, byte operation) throws StandardException {
        //super(hdl);
        this.operation = operation;
    }
}
