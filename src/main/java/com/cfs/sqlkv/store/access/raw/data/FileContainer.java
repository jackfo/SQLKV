package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.store.access.raw.PageKey;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 10:50
 */
public abstract class FileContainer  extends BaseContainer implements Cacheable{

    /**页面缓存*/
    protected final CacheManager          pageCache;
    /**容器缓存*/
    protected final CacheManager          containerCache;
    /**数据缓存*/
    protected final BaseDataFileFactory   dataFactory;

    public FileContainer(BaseDataFileFactory factory){
        dataFactory = factory;
        pageCache = factory.getPageCache();
        containerCache = factory.getContainerCache();
    }

    /**
     *根据pageNumber获取使用页
     * 在这个过程主要存在的问题是当前页是否被使用
     * 一般情况是被使用的话会做一些等待
     * */
    private BasePage getUserPage(BaseContainerHandle handle, long pageNumber, boolean overflowOK, boolean wait) throws StandardException {
        //如果页号小于首页则直接返回空
        if (pageNumber < BaseContainerHandle.FIRST_PAGE_NUMBER){
            return null;
        }

        //如果事务是提交或者删除状态则返回空
        if (getCommittedDropState()){
            return null;
        }

        //如果不是合法页 则返回空
        if (!pageValid(handle, pageNumber)) {
            return null;
        }

        //创建页页对应的键
        PageKey pageSearch = new PageKey(identity, pageNumber);

        BasePage page = (BasePage)pageCache.find(pageSearch);

        if(page == null){
            return page;
        }

        /**
         * 尝试抓住当前页
         * */
        if (latchPage(handle,page,wait) == null) {
            return null;
        }
        boolean isoverflowOk  = page.isOverflowPage() && !overflowOK ;

        if (isoverflowOk|| (page.getPageStatus() != BasePage.VALID_PAGE)) {
            page.unlatch();
            page = null;
        }
        return page;
    }

    /**
     *
     * */
    protected BasePage latchPage(BaseContainerHandle handle, BasePage foundPage, boolean wait) throws StandardException{
        if(foundPage!=null){
            if(wait){
                //如果需要等待 则设置为排它逻辑
                foundPage.setExclusive(handle);
            }else{
                if (!foundPage.setExclusiveNoWait(handle)) {
                    return null;
                }
            }
        }
        return foundPage;
    }

    /**
     * TODO:待实现
     * */
    private boolean pageValid(BaseContainerHandle handle, long pagenum) throws StandardException {
         return true;
    }

    @Override
    public BasePage getPage(BaseContainerHandle handle, long pageNumber, boolean wait) throws StandardException {
        return getUserPage(handle, pageNumber, true , wait);
    }
}
