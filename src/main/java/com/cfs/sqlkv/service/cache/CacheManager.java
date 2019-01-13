package com.cfs.sqlkv.service.cache;

import com.cfs.sqlkv.exception.StandardException;
import com.sun.deploy.cache.CacheEntry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:50
 */
public class CacheManager {

    private final ConcurrentHashMap<Object, CacheEntry> cache;

    private final CacheableFactory holderFactory;

    private final String name;

    private final int maxSize;

    private final AtomicLong hits = new AtomicLong();

    private final AtomicLong misses = new AtomicLong();

    private final AtomicLong evictions = new AtomicLong();

    private volatile boolean stopped;

    public CacheManager(CacheableFactory holderFactory, String name, int initialSize, int maxSize){
        cache = new ConcurrentHashMap(initialSize);
        this.holderFactory = holderFactory;
        this.name = name;
        this.maxSize = maxSize;
    }

    public Cacheable create(Object key, Object createParameter) throws StandardException{
        return null;
    }

    public void release(Cacheable entry){

    }

    public Cacheable find(Object key) throws StandardException{
        return null;
    }

}
