package com.cfs.sqlkv.service.cache;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-19 13:16
 */
public interface Callback {

    void access();

    void free();
}
