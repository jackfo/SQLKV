package com.cfs.sqlkv.row;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.BaseDataFileFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:52
 */
public class RawStore {

    protected BaseDataFileFactory dataFactory;
    public long getMaxContainerId() throws StandardException {
        return(dataFactory.getMaxContainerId());
    }
}
