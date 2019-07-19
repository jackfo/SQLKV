package com.cfs.sqlkv.store.access.raw.data;


import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.io.FormatIdUtil;
import com.cfs.sqlkv.store.access.raw.PageKey;

import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 15:04
 */
public abstract class CachedPage extends BasePage {

    /**
     * 初始化行数
     */
    protected int initialRowCount;

    /**
     * 页面的写操作是否需要异步
     */
    public static final int WRITE_SYNC = 1;

    protected CacheManager containerCache;

    /**
     * 页上的实际数据
     */
    protected byte[] pageData;

    /**
     * 当业数据被直接或间接操作必须设置为真
     */
    protected boolean isDirty;

    /**
     * 如果页面是被尚未处理并且pageData array准备开始进行操作页面
     */
    protected boolean preDirty;

    /**
     * 从缓存将对象写入到磁盘
     * 1.写入页对应的格式
     * 2.写入整页内容
     * 3.根据容器Id找到对应的文件容器
     * 4.向对应的页号写入相应的数据
     *
     * @param identity 页的标识,主要由页号和容器标识组成
     * @param syncMe   检测写是同步写还是异步写
     */
    private void writePage(PageKey identity, boolean syncMe)   {
        writeFormatId(identity);
        writePage(identity);
        FileContainer myContainer = (FileContainer) containerCache.find(identity.getContainerId());
        if (myContainer == null) {
            throw new RuntimeException("Container {0} cannot be opened; it either has been dropped or does not exist.");
        }
        try {
            myContainer.writePage(identity.getPageNumber(), pageData, syncMe);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            containerCache.release(myContainer);
            myContainer = null;
        }

        synchronized (this) {
            isDirty = false;
            preDirty = false;
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

    protected abstract void initFromData(FileContainer container, PageKey id)  ;

    /**
     * 设置pageData的大小
     */
    protected void setPageArray(int pageSize) {
        if ((pageData == null) || (pageData.length != pageSize)) {
            pageData = null;
            pageData = new byte[pageSize];
        }
        usePageBuffer(pageData);
    }

    protected abstract void usePageBuffer(byte[] buffer);

    /**
     * 写入当前页面的格式ID
     */
    protected abstract void writeFormatId(PageKey identity)  ;

    protected BaseDataFileFactory dataFactory;
    protected CacheManager pageCache;

    public final void setFactory(BaseDataFileFactory factory) {
        dataFactory = factory;
        pageCache = factory.getPageCache();
        containerCache = factory.getContainerCache();
    }

    @Override
    public Cacheable setIdentity(Object key)   {
        //初始化
        initialize();
        PageKey newIdentity = (PageKey) key;
        FileContainer myContainer = (FileContainer) containerCache.find(newIdentity.getContainerId());
        if (!alreadyReadPage) {
            //读取磁盘的数据到pageData
            readPage(myContainer, newIdentity);
        } else {
            alreadyReadPage = false;
        }
        int fmtId = getTypeFormatId();
        //从磁盘数据中读取出格式Id
        int onPageFormatId = FormatIdUtil.readFormatIdInteger(pageData);
        if (fmtId != onPageFormatId) {
            return changeInstanceTo(onPageFormatId).setIdentity(key);
        }
        initFromData(myContainer, newIdentity);
        fillInIdentity(newIdentity);
        return this;
    }

    protected void fillInIdentity(PageKey key) {
        identity = key;
    }

    private void readPage(FileContainer myContainer, PageKey newIdentity)   {
        //获取容器页面大小
        int pagesize = myContainer.getPageSize();
        setPageArray(pagesize);
        while (true) {
            try {
                //从容器中读取对应页面的数据到pageData
                myContainer.readPage(newIdentity.getPageNumber(), pageData);
                break;
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    /**
     * 找到容器,在容器中创建对应的页
     */
    @Override
    public Cacheable createIdentity(Object key, Object createParameter)   {
        initialize();
        PageKey newIdentity = (PageKey) key;
        PageCreationArgs createArgs = (PageCreationArgs) createParameter;
        int formatId = createArgs.getFormatId();
        if (formatId == -1) {
            throw new RuntimeException("unknow page format_id");
        }
        //如果格式Id不是AllocPage需要做一个转化,因为只有分配页才能分配相应的页面
        if (formatId != getTypeFormatId()) {
            CachedPage cachedPage = changeInstanceTo(formatId);
            return cachedPage.createIdentity(key, createParameter);
        }

        initializeHeaders(5);
        createPage(newIdentity, createArgs);
        //将标识添加到当前页
        fillInIdentity(newIdentity);
        return this;
    }

    protected abstract void createPage(PageKey id, PageCreationArgs args)  ;

    protected boolean alreadyReadPage;

    public CachedPage changeInstanceTo(int formatId) {
        CachedPage realPage;
        if (formatId == StoredPage.FORMAT_NUMBER) {
            realPage = new StoredPage();
        } else if (formatId == AllocPage.FORMAT_NUMBER) {
            realPage = new AllocPage();
        } else {
            throw new RuntimeException(String.format("unknow disk page formatId,formatId is %s", formatId));
        }
        realPage.setFactory(dataFactory);
        if (this.pageData != null) {
            realPage.alreadyReadPage = true;
            realPage.usePageBuffer(this.pageData);
        }
        return realPage;
    }

    /**
     * 将页写入到磁盘
     */
    public void clean(boolean remove)   {
        synchronized (this) {
            //如果当前页不存在脏数据,则直接返回
            if (!isDirty()) {
                return;
            }
            //将当前页写入到磁盘
            writePage(getPageId(), false);
        }
    }

    public void clearIdentity() {
        alreadyReadPage = false;
        super.clearIdentity();
    }


}
