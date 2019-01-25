package com.cfs.sqlkv.store.access.raw.data;

/**
 * @author zhengxiaokang
 * @Description 创建页面的参数
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-17 17:31
 */
public class PageCreationArgs {

    /**
     * 标识页面创造的格式类型
     * StoredPage.FORMAT_NUMBER和AllocPage.FORMAT_NUMBER
     * */
    private final int formatId;

    /**
     * 写入操作是否做异步
     * */
    private final int syncFlag;

    /**
     * 页面的字节大小
     * */
    private final int pageSize;

    /**
     * 页面剩余空间
     * */
    private final int spareSpace;


    private final int minimumRecordSize;

    private final int containerInfoSize;

    public PageCreationArgs(int formatId, int syncFlag, int pageSize, int spareSpace, int minimumRecordSize, int containerInfoSize) {
        this.formatId = formatId;
        this.syncFlag = syncFlag;
        this.pageSize = pageSize;
        this.spareSpace = spareSpace;
        this.minimumRecordSize = minimumRecordSize;
        this.containerInfoSize = containerInfoSize;
    }

    public int getFormatId() {
        return formatId;
    }

    public int getSyncFlag() {
        return syncFlag;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getSpareSpace() {
        return spareSpace;
    }

    public int getMinimumRecordSize() {
        return minimumRecordSize;
    }

    public int getContainerInfoSize() {
        return containerInfoSize;
    }
}
