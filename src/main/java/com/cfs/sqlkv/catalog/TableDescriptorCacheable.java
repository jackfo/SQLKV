package com.cfs.sqlkv.catalog;


import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;

/**
 * @author zhengxiaokang
 * @Description 表描述的缓存实现
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-31 23:22
 */
public class TableDescriptorCacheable implements Cacheable {
    /**
     * 表描述
     */
    protected TableDescriptor td;
    protected final DataDictionaryImpl dd;
    private TableKey identity;

    public TableDescriptorCacheable(DataDictionaryImpl dd) {
        this.dd = dd;
    }

    public void clean(boolean forRemove) {
        return;
    }

    public boolean isDirty() {
        return false;
    }

    public TableDescriptor getTableDescriptor() {
        return td;
    }

    @Override
    public void clearIdentity() {
        identity = null;
        td = null;
    }

    @Override
    public Object getIdentity() {
        return identity;
    }

    public Cacheable createIdentity(Object key, Object createParameter) {
        identity = (TableKey) key;
        td = (TableDescriptor) createParameter;
        if (td != null) {
            return this;
        } else {
            return null;
        }
    }

    public Cacheable setIdentity(Object key)   {
        td = dd.getUncachedTableDescriptor(identity = (TableKey) key);
        if (td != null) {
            return this;
        } else {
            return null;
        }
    }


}
