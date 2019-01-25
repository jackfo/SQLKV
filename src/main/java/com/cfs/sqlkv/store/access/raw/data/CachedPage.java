package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.store.access.raw.PageKey;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public abstract class CachedPage extends BasePage {

    /**初始化行数*/
    protected int initialRowCount;

    /**页面的写操作是否需要异步*/
    public static final int WRITE_SYNC = 1;

    protected CacheManager containerCache;

    /**
     * 页上的实际数据
     * */
    protected byte[] pageData;

    /**
     * 当业数据被直接或间接操作必须设置为真
     */
    protected boolean isDirty;

    /**
     *如果页面是被尚未处理并且pageData array准备开始进行操作页面
     * */
    protected boolean		preDirty;

    /**
     *
     * 从缓存将对象写入到磁盘
     * 1.写入页对应的格式
     * 2.写入整页内容
     * 3.根据容器Id找到对应的文件容器
     * 4.向对应的页号写入相应的数据
     *
     * @param identity 页的标识,主要由页号和容器标识组成
     * @param syncMe   检测写是同步写还是异步写
     * */
    private void writePage(PageKey identity, boolean syncMe) throws StandardException {
        writeFormatId(identity);
        writePage(identity);
        FileContainer myContainer = (FileContainer)containerCache.find(identity.getContainerId());

        if(myContainer==null){
            throw new RuntimeException("Container {0} cannot be opened; it either has been dropped or does not exist.");
        }

        try{
            myContainer.writePage(identity.getPageNumber(), pageData, syncMe);
            //如果不是溢出且是脏数据
            if (!isOverflowPage() && isDirty()) {

                myContainer.trackUnfilledPage(identity.getPageNumber(), unfilled());
                /**
                 * 更新当前记录行数
                 * */
                int currentRowCount = internalNonDeletedRecordCount();
                if (currentRowCount != initialRowCount) {
                    myContainer.updateEstimatedRowCount(currentRowCount - initialRowCount);
                    setContainerRowCount(myContainer.getEstimatedRowCount(0));
                    initialRowCount = currentRowCount;
                }
            }
        }catch (IOException e){
            throw new RuntimeException("Page {0} could not be written to disk, please check if the disk is full, or if a file system limit, such as a quota or a maximum file size, has been reached");
        }finally {
            containerCache.release(myContainer);
            myContainer = null;
        }

        synchronized (this) {
            isDirty     = false;
            preDirty    = false;
        }
    }


    /**
     * isDirty标签表明pageData或者pageHeader是否被修改
     * preDirty标签表明pageData或者pageHeader准备被修改
     **/
    public boolean isDirty() {
        synchronized (this) {
            return isDirty || preDirty;
        }
    }

    protected abstract void initFromData(FileContainer container, PageKey id) throws StandardException;

}
