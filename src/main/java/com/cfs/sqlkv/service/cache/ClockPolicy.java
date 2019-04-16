package com.cfs.sqlkv.service.cache;



import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final AtomicInteger freeEntries = new AtomicInteger();

    private final AtomicBoolean isShrinking = new AtomicBoolean();

    /**缓存头当前的位置*/
    private int hand;

    /**设置收缩比例*/
    private static final float PART_OF_CLOCK_FOR_SHRINK = 0.1f;

    private static final float MAX_ROTATION = 0.2f;

    /**最小检查项数目*/
    private static final int MIN_ITEMS_TO_CHECK = 20;

    public ClockPolicy(CacheManager cacheManager, int initialSize, int maxSize) {
        this.cacheManager = cacheManager;
        this.maxSize = maxSize;
        clock = new ArrayList<Holder>(initialSize);
    }

    /**
     * 插入条目
     * */
    public void insertEntry(CacheEntry entry)   {
        final int size;
        synchronized (clock) {
            size = clock.size();
            if (size < maxSize) {
                if (freeEntries.get() == 0) {
                    clock.add(new Holder(entry));
                    return;
                }
            }


        }

        /**如果缓存已经达到最大,需要清除一些缓存条目*/
        if(size>maxSize){
            doShrink();
        }

        Holder h = rotateClock(entry, size >= maxSize);
        if (h == null) {
            synchronized (clock) {
                clock.add(new Holder(entry));
            }
        }

    }

    /**
     *
     * */
    private Holder rotateClock(CacheEntry entry, boolean allowEvictions)   {
        int itemsToCheck = 0;
        if (allowEvictions) {
            synchronized (clock) {
                itemsToCheck = Math.max(MIN_ITEMS_TO_CHECK, (int) (clock.size() * MAX_ROTATION));
            }
        }

        while (itemsToCheck-- > 0 || freeEntries.get() > 0) {
            System.out.println("rotateClock");
            final Holder h = moveHand();
            if (h == null) {
                return null;
            }
            final CacheEntry e = h.getEntry();
            /**如果当前Holder的entry为空,直接将新增entry添加进去*/
            if (e == null) {
                if (h.takeIfFree(entry)) {
                    return h;
                }
                continue;
            }
            //检测是否允许清除条目
            if (!allowEvictions) {
                continue;
            }

            final Cacheable dirty;
            if (!isEvictable(e, h, true)) {
                continue;
            }
            Cacheable c = e.getCacheable();
            //如果缓存对象不是脏数据,直接转化为当前条目,并将原有的key所对应的值移除
            if (!c.isDirty()) {
                h.switchEntry(entry);
                cacheManager.evictEntry(c.getIdentity());
                return h;
            }
            //在这里表示缓存对象是脏数据,即尚未刷新到磁盘中去,需要将对象条目添加到磁盘
            dirty = c;
            cacheManager.cleanAndUnkeepEntry(e, dirty);
        }
        return null;
    }

    private Holder moveHand() {
        synchronized (clock) {
            if (clock.isEmpty()) {
                return null;
            }
            if (hand >= clock.size()) {
                hand = 0;
            }
            return clock.get(hand++);
        }
    }

    public void doShrink() {
        if (isShrinking.compareAndSet(false, true)) {
            try {
                shrinkMe();
            } finally {
                isShrinking.set(false);
            }
        }
    }

    private void shrinkMe() {
        //获取清除的缓存数量
        int maxLooks = Math.max(1, (int) (maxSize * PART_OF_CLOCK_FOR_SHRINK));
        int pos;
        synchronized (clock) {
            pos = hand;
        }
        //具体开始清除缓存
        while (maxLooks-- > 0) {
            System.out.println("shrinkMe");
            final Holder h;
            final int size;

            synchronized (clock) {
                size = clock.size();
                if (pos >= size) {
                    pos = 0;
                }
                h = clock.get(pos);
            }

            final int index = pos;
            pos++;
            if (size <= maxSize) {
                break;
            }

            //获取当前句柄对应的缓存条目
            final CacheEntry e = h.getEntry();
            //如果当前句柄缓存条目为空,且evicted依旧标记为false,将当前Holder给移除
            if (e == null) {
                if (h.evictIfFree()) {
                    removeHolder(index, h);
                    pos = index;
                }
                continue;
            }


            if (!isEvictable(e, h, false)) {
                continue;
            }
            final Cacheable c = e.getCacheable();
            if (c.isDirty()) {
                continue;
            }

            h.setEvicted();
            cacheManager.evictEntry(c.getIdentity());
            removeHolder(index, h);
            pos = index;
        }
    }


    private boolean isEvictable(CacheEntry e, Holder h, boolean clearRecentlyUsedFlag) {
        if (h.getEntry() != e) {
            return false;
        }
        return true;
    }
    private void removeHolder(int pos, Holder h) {
        synchronized (clock) {
            Holder removed = clock.remove(pos);
        }
    }

    private class Holder implements Callback {

        boolean recentlyUsed;

        private CacheEntry entry;

        private Cacheable freedCacheable;

        private boolean evicted;

        public Holder(CacheEntry e) {
            entry = e;
        }

        @Override
        public void access() {
            recentlyUsed = true;
        }

        @Override
        public void free() {
        }

        synchronized CacheEntry getEntry() {
            return entry;
        }

        synchronized boolean evictIfFree() {
            if (entry == null && !evicted) {
                int free = freeEntries.decrementAndGet();
                evicted = true;
                return true;
            }
            return false;
        }

        synchronized void setEvicted() {
            evicted = true;
            entry = null;
        }

        synchronized boolean takeIfFree(CacheEntry e) {
            if (entry == null && !evicted) {
                freeEntries.decrementAndGet();
                e.setCacheable(freedCacheable);
                entry = e;
                freedCacheable = null;
                return true;
            }
            return false;
        }

        /**
         * 将缓存对象添加到当前Holder所在的条目
         * */
        synchronized void switchEntry(CacheEntry e) {
            e.setCacheable(entry.getCacheable());
            entry = e;
        }
    }
}
