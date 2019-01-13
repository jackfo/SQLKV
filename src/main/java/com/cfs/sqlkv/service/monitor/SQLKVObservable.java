package com.cfs.sqlkv.service.monitor;

import java.util.ArrayList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 11:37
 */
public class SQLKVObservable {

    private boolean hasChanged = false;

    /**观察者对应的集合*/
    private ArrayList<SQLKVObserver> observers  = new ArrayList<>();

    public synchronized void addObserver(SQLKVObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * 计算观察着的数量
     * */
    public synchronized int countObservers() {
        return observers.size();
    }

    /**
     * 移除一个观察者
     * */
    public synchronized void deleteObserver(SQLKVObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() { notifyObservers(null); }

    /**
     * 唤醒所有的观察者,并进行更新
     * */
    public void notifyObservers(Object extraInfo) {
        SQLKVObserver[] cachedObservers;
        synchronized (this) {
            if (!hasChanged) {
                return;
            }
            cachedObservers = new SQLKVObserver[observers.size()];
            observers.toArray(cachedObservers);
            hasChanged = false;
        }

        int lastIndex = cachedObservers.length - 1;
        for (int idx = lastIndex; idx >= 0; idx--) {
            cachedObservers[idx].update(this, extraInfo);
        }
    }


    protected synchronized void setChanged() {
            hasChanged = true;
    }
}
