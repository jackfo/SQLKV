package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.service.monitor.SQLKVObserver;
import com.cfs.sqlkv.store.access.raw.ContainerKey;

/**
 * @author zhengxiaokang
 * @Description 容器提交行为
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 12:21
 */
abstract class ContainerActionOnCommit implements SQLKVObserver {

    protected ContainerKey identity;

    protected ContainerActionOnCommit(ContainerKey identity) {
        this.identity = identity;
    }

    @Override
    public int hashCode(){
        return identity.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ContainerActionOnCommit) {
            if (!identity.equals(((ContainerActionOnCommit) other).identity)){
                return false;
            }
            return getClass().equals(other.getClass());
        }
        return false;
    }

}
