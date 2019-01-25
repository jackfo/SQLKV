package com.cfs.sqlkv.service.cache;

import com.cfs.sqlkv.exception.StandardException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 19:51
 */
public interface Cacheable {
    /**
     * 创建标识
     * */
    public Cacheable createIdentity(Object key, Object createParameter) throws StandardException;

}
