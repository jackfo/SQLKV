package com.cfs.sqlkv.io.file;

import com.cfs.sqlkv.io.storage.StorageRandomAccessFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 09:46
 */
public class DirRandomAccessFile extends RandomAccessFile implements StorageRandomAccessFile {

    private final File name;
    private final String mode;

    public DirRandomAccessFile( File name, String mode) throws FileNotFoundException {
        super( name, mode);
        this.name = name;
        this.mode = mode;
    }
}
