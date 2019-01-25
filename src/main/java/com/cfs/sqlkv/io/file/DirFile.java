package com.cfs.sqlkv.io.file;

import com.cfs.sqlkv.io.storage.StorageFile;
import com.cfs.sqlkv.io.storage.StorageRandomAccessFile;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author zhengxiaokang
 * @Description
 *   提供基于StorageFile接口的基础磁盘实现
 *   数据库引擎使用它来访问目录（默认）subsubprotocol下的持久数据和事务日志
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 09:35
 */
public class DirFile extends File implements StorageFile {


    /**
     * 根据指定路径构建一个DirFile
     * */
    public DirFile( String path) {
        super( path);
    }

    /**
     * 根据目录和文件名构建一个DirFile
     * */
    public DirFile(String directoryName, String fileName) {
        super( directoryName, fileName);
    }


    @Override
    public boolean deleteAll() {
        return false;
    }

    /**
     * 获取当前文件的父级目录
     */
    @Override
    public StorageFile getParentDir() {
        String parent = getParent();
        if( parent == null){
            return null;
        }
        return new DirFile( parent);
    }

    @Override
    public StorageRandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException {
        return new DirRandomAccessFile((File) this, mode);
    }
}
