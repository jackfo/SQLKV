package com.cfs.sqlkv.service.cache;


import com.cfs.sqlkv.store.access.raw.PageKey;

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

    private final ClockPolicy clockPolicy;

    private final String name;

    private final int maxSize;


    public CacheManager(CacheableFactory holderFactory, String name, int initialSize, int maxSize) {
        cache = new ConcurrentHashMap(initialSize);
        this.holderFactory = holderFactory;
        this.name = name;
        this.maxSize = maxSize;
        clockPolicy = new ClockPolicy(this, initialSize, maxSize);
    }

    /**
     * 创建页,创建之后将其添加到缓存中去
     */
    public Cacheable create(Object key, Object createParameter) {
        CacheEntry entry = new CacheEntry();
        if (cache.putIfAbsent(key, entry) != null) {
            throw new RuntimeException(String.format("cache object is exists,it's key is %s", key));
        }
        Cacheable item = insertIntoFreeSlot(key, entry);
        item = item.createIdentity(key, createParameter);
        //在缓存创建完毕之后,需要将其重新注入到缓存条目
        settingIdentityComplete(key, entry, item);
        return item;
    }


    private void settingIdentityComplete(Object key, CacheEntry entry, Cacheable item) {
        entry.setCacheable(item);
    }

    /**
     * 将缓存插入到对应的条目
     */
    private Cacheable insertIntoFreeSlot(Object key, CacheEntry entry) {
        clockPolicy.insertEntry(entry);
        Cacheable free = entry.getCacheable();
        if (free == null) {
            free = holderFactory.newCacheable(this);
        }
        return free;
    }

    public void release(Cacheable entry) {

    }

    /**
     * 获取缓存的Cacheable
     */
    public Cacheable find(Object key) {
        CacheEntry entry = getEntry(key);
        Cacheable item = entry.getCacheable();
        if (item != null) {
            return item;
        } else {

            item = insertIntoFreeSlot(key, entry);

        }

        item = item.setIdentity(key);

        settingIdentityComplete(key, entry, item);
        return item;
    }

    /**
     * 获取缓存条目
     */
    private CacheEntry getEntry(Object key) {
        if (key == null) {
            throw new RuntimeException("key can't be null");
        }
        CacheEntry entry = cache.get(key);
        while (true) {
            if (entry != null) {
                return entry;
            } else {
                CacheEntry freshEntry = new CacheEntry();
                CacheEntry oldEntry = cache.putIfAbsent(key, freshEntry);
                if (oldEntry != null) {
                    entry = oldEntry;
                } else {
                    return freshEntry;
                }
            }


        }
    }

    private BackgroundCleaner cleaner;

    public BackgroundCleaner getBackgroundCleaner() {
        return cleaner;
    }

    public void evictEntry(Object key) {
        CacheEntry entry = cache.remove(key);
        entry.getCacheable().clearIdentity();
        entry.setCacheable(null);
    }

    /**
     * 清空并且保存缓存条目到磁盘
     */
    public void cleanAndUnkeepEntry(CacheEntry entry, Cacheable item) {
        item.clean(false);
    }

    /**
     * 清除缓存中所有的脏对象
     */
    public void cleanAll() {
        cleanCache();
    }

    private void cleanCache() {
        for (CacheEntry entry : cache.values()) {
            final Cacheable dirtyObject;
            if (!entry.isValid()) {
                continue;
            }
            Cacheable c = entry.getCacheable();
            if (!c.isDirty()) {
                continue;
            }
            dirtyObject = c;
            cleanAndUnkeepEntry(entry, dirtyObject);
        }
    }
}
