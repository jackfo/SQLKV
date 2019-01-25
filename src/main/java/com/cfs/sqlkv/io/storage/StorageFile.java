package com.cfs.sqlkv.io.storage;

import java.io.FileNotFoundException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 12:57
 */
public interface StorageFile {
    /**
     * 如果是文件直接删除文件,如果是目录则删除目录下所有的文件
     * */
    public boolean deleteAll();

    /**获取所有文件的名字*/
    public String[] list();

    public boolean exists();

    public boolean isDirectory();

    public boolean delete();

    public String getPath();

    public StorageFile getParentDir();

    public boolean canWrite();

    public StorageRandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException;
}
