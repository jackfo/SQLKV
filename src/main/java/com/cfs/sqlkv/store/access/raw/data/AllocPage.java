package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.row.RawStoreFactory;
import com.cfs.sqlkv.transaction.Transaction;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:03
 */
public class AllocPage extends StoredPage{

    private AllocExtent extent;

    public static final int FORMAT_NUMBER = StoredFormatIds.RAW_STORE_ALLOC_PAGE;

    public static final int MAX_BORROWED_SPACE = RawStoreFactory.PAGE_SIZE_MINIMUM / 5;

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.RAW_STORE_ALLOC_PAGE;
    }

    /**
     * rawDataOut and logicalDataOut are defined by StoredPage
     * */
    private void writeExtent(int offset) throws IOException {
        rawDataOut.setPosition(offset);
        extent.writeExternal(logicalDataOut);
    }

    /**
     * 在写入页面前,需要更新头部信息
     * */


    /**
     * 阅读分区
     * */
    private AllocExtent readExtent(int offset) throws IOException, ClassNotFoundException{
        ArrayInputStream lrdi = rawDataIn;
        rawDataIn.setPosition(offset);
        AllocExtent newExtent = new AllocExtent();
        newExtent.readExternal(lrdi);
        return newExtent;
    }
    /**
     * allocation page header
     * 8 bytes	long	next alloc page number
     * 8 bytes	long	next alloc page physical offset
     */
    protected static final int ALLOC_PAGE_HEADER_OFFSET = StoredPage.PAGE_HEADER_OFFSET + StoredPage.PAGE_HEADER_SIZE;

    /**
     * 分配页大小
     * */
    protected static final int ALLOC_PAGE_HEADER_SIZE = 8+8;

    /**
     *     page header offset 4
     *     page head size
     *
     * */
    protected static final int BORROWED_SPACE_OFFSET = ALLOC_PAGE_HEADER_OFFSET + ALLOC_PAGE_HEADER_SIZE;


    protected static final int BORROWED_SPACE_LEN = 1;


    /**
     * 该字节标识剩余内容的长度
     * */
    private int borrowedSpace;


    /**
     * 初始化pageData的内存结构
     * */
    @Override
    protected void initFromData(FileContainer myContainer, PageKey newIdentity)  {

        if (pageData.length < BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN){
            throw new RuntimeException(String.format("Unexpected exception on in-memory page %s",identity));
        }
        /**
         * 字节n表示头部之后的第一个字节的内容
         * 该字节记录的事剩下内容的长度
         * */
        byte n = pageData[BORROWED_SPACE_OFFSET];

        borrowedSpace = (int)n;

        if (pageData.length < BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN + n) {
            throw new RuntimeException(String.format("Unexpected exception on in-memory page %s",identity));
        }

        if (borrowedSpace > 0) {
            //将所有的borrowedSpace空间中内容填充为0
            clearSection(BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN, borrowedSpace);
        }

        //初始化容器
        super.initFromData(myContainer, newIdentity);

        //读取分配页头部数据
        try {
            readAllocPageHeader();
            int offset = BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN + borrowedSpace;
            extent = readExtent(offset);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readAllocPageHeader() throws IOException{
        ArrayInputStream lrdi = rawDataIn;
        lrdi.setPosition(ALLOC_PAGE_HEADER_OFFSET);
        nextAllocPageNumber = lrdi.readLong();
        nextAllocPageOffset = lrdi.readLong();
    }

    /**
     * 从epage中提取容器信息
     * @param containerInfo 容器信息
     * */
    public static void readContainerInfo(byte[] containerInfo, byte[] epage)  {
        //获取空间大小
        int N = (int)epage[BORROWED_SPACE_OFFSET];
        if (N != 0){
            try {
                //直接将容器信息拷贝到缓冲池可以用空间的起始
                System.arraycopy(epage, BORROWED_SPACE_OFFSET+BORROWED_SPACE_LEN, containerInfo, 0, N);
            }  catch (ArrayIndexOutOfBoundsException ioobe) {
                ioobe.printStackTrace();
            }
        }
    }

    /**
     * 写入容器信息的长度
     * */
    public static void writeContainerInfo(byte[] containerInfo, byte[] epage, boolean create)   {
        int N = (containerInfo == null) ? 0 : containerInfo.length;
        if (create) {
            epage[BORROWED_SPACE_OFFSET] = (byte)N;
        }else{
            int oldN = (int)epage[BORROWED_SPACE_OFFSET];
            if (oldN != N){
                throw new RuntimeException(String.format("Container information cannot change once written: was %s, now %s",oldN,N));
            }
        }
        /**
         * 将容器信息添加进去
         * */
        if (N != 0){
            System.arraycopy(containerInfo, 0, epage, BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN, N);
        }
    }

    private long nextAllocPageNumber;
    private long nextAllocPageOffset;
    public long getNextAllocPageNumber(){
        return nextAllocPageNumber;
    }


    /**
     * 在创建分配页的时候,会创建对应区
     * */
    protected void createPage(PageKey newIdentity, PageCreationArgs args) {
        borrowedSpace = args.containerInfoSize;
        super.createPage(newIdentity, args);
        //设置当前页面数据空间大小
        pageData[BORROWED_SPACE_OFFSET] = (byte)borrowedSpace;
        if (borrowedSpace > 0){
            clearSection(BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN, borrowedSpace);
        }
        nextAllocPageNumber = BaseContainerHandle.INVALID_PAGE_NUMBER;
        nextAllocPageOffset = 0;
        //创建分区
        extent = createExtent(newIdentity.getPageNumber()+1, getPageSize(), 0 , totalSpace);
    }

    /**
     * 创建区
     * */
    private AllocExtent createExtent(long pageNum, int pageSize, int pagesAlloced, int availspace){
        int maxPages = AllocExtent.MAX_RANGE(availspace);
        return new AllocExtent(pageNum*pageSize, pageNum, pagesAlloced, pageSize, maxPages);
    }


    protected void writePage(PageKey identity)  {
        try{
            //更新分配页头部
            updateAllocPageHeader();
            int n = (int)pageData[BORROWED_SPACE_OFFSET];
            if (n > 0) {
                clearSection(BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN, n);
            }
            int offset = BORROWED_SPACE_OFFSET + BORROWED_SPACE_LEN + n;
            writeExtent(offset);
        }catch (IOException ioe){

        }
        super.writePage(identity);
    }

    /**
     * 更新分配页头部信息
     * */
    private void updateAllocPageHeader() throws IOException {
        rawDataOut.setPosition(ALLOC_PAGE_HEADER_OFFSET);
        logicalDataOut.writeLong(nextAllocPageNumber);
        logicalDataOut.writeLong(nextAllocPageOffset);
    }


    /**
     * 判断当前页是否是最后一页
     * */
    public boolean isLast() {
        return nextAllocPageNumber == BaseContainerHandle.INVALID_PAGE_NUMBER;
    }



    /**
     * 根据当前页号找到下一个页号
     * */
    public long nextFreePageNumber(long pageNumber) {
        return extent.getFreePageNumber(pageNumber);
    }

    public long getLastPagenum(){
        return extent.getLastPagenum();
    }

    /**
     * 在这里主要是将当前页添加到区
     * */
    public void addPage(FileContainer mycontainer, long newPageNumber, Transaction transaction, BaseContainerHandle userHandle)   {
        extent.allocPage(newPageNumber);
    }

    protected AllocExtent getAllocExtent() {
        return extent;
    }



}
