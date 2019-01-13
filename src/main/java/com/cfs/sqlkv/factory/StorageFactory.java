package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.io.StorageFile;

public interface StorageFactory {

    /**
     * 决定数据库是否只是只读数据库
     * @return 如果是只读数据库返回真
     */
    public boolean isReadOnlyDatabase();

    /**
     *确定存储是否支持随机访问
     * */
    public boolean supportsRandomAccess();

    /***/
    public StorageFile getTempDir();

    /**获取路径分隔符*/
    public char getSeparator();

    public StorageFile newStorageFile( String path);

    public StorageFile newStorageFile( String directoryName, String fileName);

    public StorageFile newStorageFile( StorageFile directoryName, String fileName);


}
