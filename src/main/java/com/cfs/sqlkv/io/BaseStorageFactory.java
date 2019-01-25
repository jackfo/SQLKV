package com.cfs.sqlkv.io;

import com.cfs.sqlkv.factory.StorageFactory;
import com.cfs.sqlkv.io.file.DirFile;
import com.cfs.sqlkv.io.storage.StorageFile;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 09:33
 */
public abstract class BaseStorageFactory implements StorageFactory {

    String home;
    protected StorageFile tempDir;
    protected String tempDirPath;
    protected String dataDirectory;
    protected String separatedDataDirectory;
    protected String uniqueName;
    protected String canonicalName;

    /**
     * 创建持久化文件
     * @param path 文件路径
     * @return
     * */
    public abstract StorageFile newPersistentFile(String path);

    @Override
    public StorageFile newStorageFile( String path) {
        if( path != null && tempDirPath != null && path.startsWith(tempDirPath)){
            return new DirFile( path);
        }
        return newPersistentFile( path);
    }
}
