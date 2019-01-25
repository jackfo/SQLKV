package com.cfs.sqlkv.io.storage;

import com.cfs.sqlkv.io.BaseStorageFactory;
import com.cfs.sqlkv.io.file.DirFile;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 09:34
 */
public class DirStorageFactory extends BaseStorageFactory{
    @Override
    public boolean isReadOnlyDatabase() {
        return false;
    }
    @Override
    public boolean supportsRandomAccess() {
        return false;
    }
    @Override
    public StorageFile getTempDir() {
        return null;
    }

    @Override
    public char getSeparator() {
        return 0;
    }

    @Override
    public StorageFile newPersistentFile(String path) {
        if( path == null){
            return new DirFile(dataDirectory);
        }
        return new DirFile(dataDirectory, path);
    }
}
