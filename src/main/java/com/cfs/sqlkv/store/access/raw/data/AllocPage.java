package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.raw.PageKey;
import com.cfs.sqlkv.store.access.raw.RawStoreFactory;

import java.io.IOException;
import java.util.Arrays;

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
     * allocation page header
     * 8 bytes	long	next alloc page number
     * 8 bytes	long	next alloc page physical offset
     * 8 bytes  long	reserved1
     * 8 bytes  long	reserved2
     * 8 bytes  long	reserved3
     * 8 bytes  long	reserved4
     */
    protected static final int ALLOC_PAGE_HEADER_OFFSET = StoredPage.PAGE_HEADER_OFFSET + StoredPage.PAGE_HEADER_SIZE;

    protected static final int ALLOC_PAGE_HEADER_SIZE = 8+8+(4*8);

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
    protected void initFromData(FileContainer myContainer, PageKey newIdentity) throws StandardException{

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

    }

    /**
     * 将页面的一部分清零
     * @param offset 清0开始位置
     * @param length 清除的长度
     * */
    protected final void clearSection(int offset, int length) {
        Arrays.fill(pageData, offset, offset + length, (byte) 0);
    }

    /**
     * 阅读容器相关信息
     * @param containerInfo
     * */
    public static void ReadContainerInfo(byte[] containerInfo, byte[] epage) throws StandardException{
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
}
