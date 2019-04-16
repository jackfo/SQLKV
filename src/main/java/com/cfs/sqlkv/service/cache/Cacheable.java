package com.cfs.sqlkv.service.cache;



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
    public Cacheable createIdentity(Object key, Object createParameter)  ;

    public Cacheable setIdentity(Object key)  ;

    /**检测缓存对象是否为脏数据*/
    public boolean isDirty();

    /**获取标识*/
    public Object getIdentity();

    /**清除标识*/
    public void clearIdentity();

    /**清除当前缓存对象*/
    public void clean(boolean forRemove)  ;
}
