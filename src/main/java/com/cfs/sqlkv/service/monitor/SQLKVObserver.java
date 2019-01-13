package com.cfs.sqlkv.service.monitor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 11:37
 */
public interface SQLKVObserver {
    public void update(SQLKVObservable observable, Object extraInfo);
}
