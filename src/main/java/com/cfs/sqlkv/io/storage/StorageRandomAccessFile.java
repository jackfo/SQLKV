package com.cfs.sqlkv.io.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-24 19:53
 */
public interface StorageRandomAccessFile extends DataInput, DataOutput {

    public void seek(long newFilePointer) throws IOException;

    /**同步刷新数据*/
    public void sync() throws IOException;

}
