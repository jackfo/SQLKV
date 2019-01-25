package com.cfs.sqlkv.service.cache;

import com.cfs.sqlkv.exception.StandardException;

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

    private final AtomicLong hits = new AtomicLong();

    private final AtomicLong misses = new AtomicLong();

    private final AtomicLong evictions = new AtomicLong();

    private volatile boolean stopped;

    public CacheManager(CacheableFactory holderFactory, String name, int initialSize, int maxSize){
        cache = new ConcurrentHashMap(initialSize);
        this.holderFactory = holderFactory;
        this.name = name;
        this.maxSize = maxSize;
        clockPolicy = new ClockPolicy(this, initialSize, maxSize);
    }

    /**
     *创建页
     * */
    public Cacheable create(Object key, Object createParameter) throws StandardException{
        if (stopped) {
            return null;
        }

        CacheEntry entry = new CacheEntry();
        entry.lock();

        if (cache.putIfAbsent(key, entry) != null) {
           throw new RuntimeException("Cannot create new object with key {1} in {0} cache. The object already exists in the cache.");
        }

        Cacheable item;
        try {
            item = insertIntoFreeSlot(key, entry);
        } finally {
            entry.unlock();
        }

        Cacheable itemWithIdentity = null;
        try {
            itemWithIdentity = item.createIdentity(key, createParameter);
        } finally {
            // Always invoke settingIdentityComplete(), also on error,
            // otherwise other threads may wait forever. If createIdentity()
            // fails, itemWithIdentity is going to be null.
            settingIdentityComplete(key, entry, itemWithIdentity);
        }
        return itemWithIdentity;
    }

    public void release(Cacheable entry){

    }

    public Cacheable find(Object key) throws StandardException{
        return null;
    }

    /**
     * Insert a {@code CacheEntry} into a free slot in the {@code
     * ReplacementPolicy}'s internal data structure, and return a {@code
     * Cacheable} that the caller can reuse. The entry must have been locked
     * before this method is called.
     *
     * 插入一个CacheEntry在空闲的slot
     * @param key the identity of the object being inserted
     * @param entry the entry that is being inserted
     * @return a {@code Cacheable} object that the caller can reuse
     * @throws StandardException if an error occurs while inserting the entry
     * or while allocating a new {@code Cacheable}
     */
    private Cacheable insertIntoFreeSlot(Object key, CacheEntry entry) throws StandardException {
        try {
            clockPolicy.insertEntry(entry);
        } catch (StandardException se) {
            // Failed to insert the entry into the replacement policy. Make
            // sure that it's also removed from the hash table.
            removeEntry(key);
            throw se;
        }
    }
}
