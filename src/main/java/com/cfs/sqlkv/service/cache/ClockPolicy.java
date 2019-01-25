package com.cfs.sqlkv.service.cache;

import java.util.ArrayList;

/**
 * @author zhengxiaokang
 * @Description 时钟策略
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-19 13:10
 */
public class ClockPolicy {

    private final CacheManager cacheManager;
    /**缓存最大数量*/
    private final int maxSize;

    private final ArrayList<Holder> clock;

    public ClockPolicy(CacheManager cacheManager, int initialSize, int maxSize) {
        this.cacheManager = cacheManager;
        this.maxSize = maxSize;
        clock = new ArrayList<Holder>(initialSize);
    }

    private class Holder implements Callback {

        boolean recentlyUsed;

        private CacheEntry entry;

        private Cacheable freedCacheable;

        private boolean evicted;

        public Holder(CacheEntry e) {
            entry = e;
            e.setCallback(this);
        }

        @Override
        public void access() {
            recentlyUsed = true;
        }

        @Override
        public void free() {
            freedCacheable = entry.getCacheable();
            entry = null;
            recentlyUsed = false;
            // let others know that a free entry is available
            int free = freeEntries.incrementAndGet();
        }
    }
}
