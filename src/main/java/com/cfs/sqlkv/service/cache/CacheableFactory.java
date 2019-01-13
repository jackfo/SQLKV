package com.cfs.sqlkv.service.cache;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-13 10:44
 */
public interface CacheableFactory {

    public Cacheable newCacheable(CacheManager cm);
}
