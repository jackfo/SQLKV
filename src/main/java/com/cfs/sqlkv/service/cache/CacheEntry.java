package com.cfs.sqlkv.service.cache;

/**
 * @author zhengxiaokang
 * @Description 缓存条目
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-27 23:00
 */
public class CacheEntry {

    //实际缓存对象
    private Cacheable cacheable;

    public Cacheable getCacheable() {
        return cacheable;
    }
    /**缓存对象是否有效*/
    public boolean isValid(){
        return cacheable!=null;
    }

    public void setCacheable(Cacheable cacheable) {
        this.cacheable = cacheable;
    }
}
