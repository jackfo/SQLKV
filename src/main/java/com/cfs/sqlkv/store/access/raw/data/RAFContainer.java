package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.storage.StorageFile;
import com.cfs.sqlkv.io.storage.StorageRandomAccessFile;
import com.cfs.sqlkv.store.access.raw.ContainerKey;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-24 19:52
 */
public class RAFContainer extends FileContainer {

    private static final int GET_FILE_NAME_ACTION = 1;
    private static final int CREATE_CONTAINER_ACTION = 2;
    private static final int REMOVE_FILE_ACTION = 3;
    private static final int OPEN_CONTAINER_ACTION = 4;
    private static final int STUBBIFY_ACTION = 5;
    private static final int GET_RANDOM_ACCESS_FILE_ACTION = 7;
    private static final int REOPEN_CONTAINER_ACTION = 8;

    private ContainerKey actionIdentity;

    private FileChannel ourChannel = null;


    protected boolean canUpdate;

    private String fileName;

    protected StorageRandomAccessFile fileData;


    synchronized StorageRandomAccessFile getRandomAccessFile(StorageFile file){

    }

    /**
     * 创建随机存储文件
     * */
    public StorageRandomAccessFile createStorageRandomAccessFile(int actionCode) throws StandardException, FileNotFoundException {
        switch (actionCode){
            case OPEN_CONTAINER_ACTION:{
                boolean isStub = false;
                StorageFile file = privGetFileName(actionIdentity, false, true, true);
                if (file==null){
                    return null;
                }
                try {
                    if (!file.exists()) {
                        file = privGetFileName( actionIdentity, true, true, true);
                        if (!file.exists()){
                            return null;
                        }
                        isStub = true;
                    }
                } catch (SecurityException se) {
                   se.printStackTrace();
                }

                canUpdate = false;
                if (!dataFactory.isReadOnly() && file.canWrite()){
                    canUpdate = true;
                }
                fileName = file.toString();
                try {
                    //获取文件可以进行读写
                    fileData = file.getRandomAccessFile(canUpdate ? "rw" : "r");
                    readHeader(getEmbryonicPage(fileData, FIRST_ALLOC_PAGE_OFFSET));
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            default:
                break;
        }
    }




    public byte[] getEmbryonicPage(StorageRandomAccessFile file, long offset) throws IOException, StandardException {
        FileChannel ioChannel = getChannel(file);
        if (ioChannel != null) {
            byte[] buffer = new byte[AllocPage.MAX_BORROWED_SPACE];
            readPage(-1L, buffer, offset);
            return buffer;
        } else {
            throw new RuntimeException(String.format("%s can not null",ioChannel));
        }
    }

    protected StorageFile privGetFileName(ContainerKey identity, boolean stub, boolean errorOK, boolean tryAlternatePath) throws StandardException {
        //创建文件
        StorageFile container = dataFactory.getContainerPath( identity, stub);

        if(!container.exists() && tryAlternatePath){
            container = dataFactory.getAlternateContainerPath(identity, stub);
        }
        if (!container.exists()) {
            throw new RuntimeException(String.format("annot create segment %s",container));
        }
        return container;
    }

    /**
     *
     * */
    private void readPage(long pageNumber, byte[] pageData, long offset) throws IOException, StandardException{
        boolean success = false;
        while (!success) {
            try {
                /**
                 * 如果这是第一个alloc页面，则可能有另一个线程访问同一页面上的借用空间中的容器信息
                 * 所以需要进行同步
                 */
                if (pageNumber == FIRST_ALLOC_PAGE_NUMBER) {
                    synchronized (this) {
                        readPage0(pageNumber, pageData, offset);
                    }
                } else {
                    readPage0(pageNumber, pageData, offset);
                }
                success = true;
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }

    }
    private void readPage0(long pageNumber, byte[] pageData, long offset) throws IOException, StandardException {
        FileChannel ioChannel;
        synchronized (this) {
            ioChannel = getChannel();
        }
        /**
         *获取页的偏移量
         *将数据读取到文件通道
         * */
        if(ioChannel != null) {
            long pageOffset = pageNumber * pageSize;
            ByteBuffer pageBuf = ByteBuffer.wrap(pageData);
            if (offset == -1L) {
                /**正常页面读取不指定偏移量,从页码的第一个开始读取*/
                readFull(pageBuf, ioChannel, pageOffset);
            }else{
                readFull(pageBuf, ioChannel, offset);
            }
        }else {
            throw new RuntimeException("通道不能为空");
        }
    }

    private FileChannel getChannel() {
        if (ourChannel == null) {
            ourChannel = getChannel(fileData);
        }
        return ourChannel;
    }

    /**
     * 尝试完全填充buf
     * @param dstBuffer buffer to read into
     * @param srcChannel channel to read from
     * @param position file position from where to read
     *
     */
    private void readFull(ByteBuffer dstBuffer, FileChannel srcChannel, long position) throws IOException, StandardException {
        while(dstBuffer.remaining() > 0) {
            if (srcChannel.read(dstBuffer, position + dstBuffer.position()) == -1) {
                throw new EOFException("Reached end of file while attempting to read a whole page.");
            }
        }
    }
}
