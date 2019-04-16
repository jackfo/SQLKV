package com.cfs.sqlkv.io.storage;

import com.cfs.sqlkv.io.BaseStorageFactory;
import com.cfs.sqlkv.io.file.DirFile;

import java.io.File;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 09:34
 */
public class DirStorageFactory extends BaseStorageFactory{

    public DirStorageFactory(String dataDirectory) {
        super(dataDirectory);
    }

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
        return File.separatorChar;
    }

    @Override
    public StorageFile newStorageFile(String directoryName, String fileName) {
        return null;
    }

    @Override
    public StorageFile newStorageFile(StorageFile directoryName, String fileName) {
        return null;
    }

    @Override
    public StorageFile newPersistentFile(String path) {
        if( path == null){
            return new DirFile(dataDirectory);
        }
        return new DirFile(dataDirectory, path);
    }
}
