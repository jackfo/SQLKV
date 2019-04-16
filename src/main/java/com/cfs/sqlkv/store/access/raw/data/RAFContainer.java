package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.io.storage.StorageFile;
import com.cfs.sqlkv.io.storage.StorageRandomAccessFile;
import com.cfs.sqlkv.service.cache.Cacheable;
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

    public RAFContainer(BaseDataFileFactory factory) {
        super(factory);
    }


    synchronized StorageRandomAccessFile getRandomAccessFile(StorageFile file){

        return null;
    }

    /**
     * 创建随机存储文件
     * */
    public StorageRandomAccessFile createStorageRandomAccessFile(int actionCode) throws FileNotFoundException {
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
        return null;
    }




    public byte[] getEmbryonicPage(StorageRandomAccessFile file, long offset) throws IOException   {
        FileChannel ioChannel = getChannel(file);
        if (ioChannel != null) {
            byte[] buffer = new byte[AllocPage.MAX_BORROWED_SPACE];
            readPage(-1L, buffer, offset);
            return buffer;
        } else {
            throw new RuntimeException(String.format("%s can not null",ioChannel));
        }
    }

    /**
     * 获取文件名
     * */
    protected StorageFile privGetFileName(ContainerKey identity, boolean stub, boolean errorOK, boolean tryAlternatePath)   {
        //创建文件
        StorageFile container = dataFactory.getContainerPath(identity,stub);
        if(!container.exists() && tryAlternatePath){
            container = dataFactory.getAlternateContainerPath(identity, stub);
        }
        if (!container.exists()) {
            StorageFile directory = container.getParentDir();
            if(!directory.exists()){
                if (!directory.mkdirs()){
                    throw new RuntimeException(String.format("Cannot create segment %s",container));
                }
            }
        }
        return container;
    }

    /**
     *读取页号的数据到pageData
     * */
    private void readPage(long pageNumber, byte[] pageData, long offset) throws IOException   {
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
    private void readPage0(long pageNumber, byte[] pageData, long offset) throws IOException   {
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
    private void readFull(ByteBuffer dstBuffer, FileChannel srcChannel, long position) throws IOException   {
        while(dstBuffer.remaining() > 0) {
            int readNums = -1;
            try{
                readNums = srcChannel.read(dstBuffer, position + dstBuffer.position());
            }catch (IOException e){
                throw new RuntimeException(e.getMessage());
            }catch (IllegalArgumentException e){
                throw new RuntimeException(e.getMessage());
            }
            if (readNums == -1) {
                throw new EOFException("Reached end of file while attempting to read a whole page.");
            }
        }
    }


    @Override
    public void createContainer(ContainerKey newIdentity)  {

        StorageFile file = privGetFileName(newIdentity, false, false, false);
        try {
            if (file.exists()) {
                throw new RuntimeException(String.format("Could not create file %s as it already exists.",file));
            }
        } catch (SecurityException se) {
            throw new RuntimeException(String.format("Exception during creation of file %s for container",file));
        }

        try{
            fileData = file.getRandomAccessFile( "rw");
            writeRAFHeader(newIdentity, fileData,true);
        }catch (IOException ioe){
            boolean fileDeleted;
            try {
                fileDeleted = privRemoveFile(file);
            } catch (SecurityException se) {
                throw new RuntimeException(String.format("Exception during creation of file %s for container, file could not be removed.  The exception was: %s.",ioe,file));
            }
            if(!fileDeleted){
                throw new RuntimeException(String.format("Exception during creation of file %s for container, file could not be removed.  The exception was: %s.",ioe,file));
            }
        }


    }

    private boolean privRemoveFile(StorageFile file)  {
        if (file.exists()){
            return file.delete();
        }
        return true;
    }

    /**
     * 写入文件头部信息
     * */
    private void writeRAFHeader(Object  identity, StorageRandomAccessFile file, boolean create) throws IOException   {
        byte[] epage;
        if(create){
            epage = new byte[pageSize];
        }else{
            epage = getEmbryonicPage(file,FIRST_ALLOC_PAGE_OFFSET);
        }
        writeHeader(identity,file,create,epage);
    }

    /**
     * 根据标识打开容器
     * */
    @Override
    public boolean openContainer(ContainerKey newIdentity)   {
        actionIdentity = newIdentity;
        StorageFile file =privGetFileName(actionIdentity, false, true, true);
        if (file == null){
            return false;
        }
        canUpdate = false;
        try {
            if (!dataFactory.isReadOnly() && file.canWrite()){
                canUpdate = true;
            }
        } catch (SecurityException se) {

        }
        fileName = file.toString();
        try {
            fileData = file.getRandomAccessFile(canUpdate ? "rw" : "r");
            //将数据读取到字节数据,之后获取头文件信息
            readHeader(getEmbryonicPage(fileData, FIRST_ALLOC_PAGE_OFFSET));
        }catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
        return true;
    }


    @Override
    protected void flushAll()   {

    }

    @Override
    protected boolean canUpdate() {
        return false;
    }

    @Override
    public void setEstimatedRowCount(long count, int flag)   {

    }



    /**
     * 在文件中的给定偏移处写入一个字节序列
     * */
    public void writeAtOffset(StorageRandomAccessFile file, byte[] bytes, long offset) throws IOException   {
        //获取文件通道
        FileChannel ioChannel = getChannel(file);
        //如果通道为空则证明是非NIO模式
        if (ioChannel == null) {
            super.writeAtOffset(file, bytes, offset);
            return;
        }
        ourChannel = ioChannel;
        boolean success = false;
        while (!success) {
            synchronized (this) {
                ioChannel = getChannel();
            }
            try {
                writeFull(ByteBuffer.wrap(bytes), ioChannel, offset);
                success = true;
            } catch (ClosedChannelException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * 根据指定位置,将数据写入到通道
     * */
    private void writeFull(ByteBuffer srcBuffer, FileChannel dstChannel, long position)throws IOException{
        while(srcBuffer.remaining() > 0) {
            dstChannel.write(srcBuffer, position + srcBuffer.position());
        }
    }

    /**
     * 将数据写入到pageData 以及刷新到文件
     * */
    protected void writePage(long pageNumber, byte[] pageData, boolean syncPage) throws IOException   {
        boolean success = false;
        while (!success) {
            if (pageNumber == FIRST_ALLOC_PAGE_NUMBER) {
                synchronized (this) {
                    writePage0(pageNumber, pageData, syncPage);
                }
            }else{
                writePage0(pageNumber, pageData, syncPage);
            }
            success = true;
        }

    }

    private void writePage0(long pageNumber, byte[] pageData, boolean syncPage) throws IOException   {
        FileChannel ioChannel;
        synchronized (this) {
            ioChannel = getChannel();
        }
        if(ioChannel != null) {
            long pageOffset = pageNumber * pageSize;
            byte[] dataToWrite = updatePageArray(pageNumber, pageData);
            ByteBuffer writeBuffer = ByteBuffer.wrap(dataToWrite);
            writeFull(writeBuffer, ioChannel, pageOffset);
        }
    }

    /**
     * 更新页面数据
     * */
    protected byte[] updatePageArray(long pageNumber, byte[] pageData) throws IOException   {
        if (pageNumber == FIRST_ALLOC_PAGE_NUMBER){
            writeHeader(getIdentity(), pageData);
            return pageData;
        }else{
            return pageData;
        }
    }

    /**
     * 从页面偏移量位置开始,读取对应大小的页面数据
     * */
    protected void readPage(long pageNumber, byte[] pageData) throws IOException   {
        readPage0(pageNumber,pageData,-1);
    }

    /**
     * 将容器页信息写入到磁盘
     * */
    public void clean(boolean forRemove)  {
        if(getCommittedDropState()){
            return ;
        }
        try {
            writeRAFHeader(getIdentity(), fileData, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getFirstPageAllocPageNumber(){
        return firstAllocPageNumber;
    }

    @Override
    protected void deallocatePage(BaseContainerHandle userhandle, BasePage page)   {

    }
}
