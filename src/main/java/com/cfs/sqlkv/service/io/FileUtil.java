package com.cfs.sqlkv.service.io;

import java.io.File;

public abstract class FileUtil {

    private static final int BUFFER_SIZE = 4096*4;

    public static boolean isExists(String fileName){
        File file = new File(fileName);
        return file.exists();
    }
}
