package com.cfs.sqlkv.store.access;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.row.RawStore;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.HeapConglomerateFactory;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 10:54
 */
public  class RAMAccessManager {

    private ConcurrentHashMap<String,MethodFactory> implmap = new ConcurrentHashMap<>();

    public MethodFactory findMethodFactoryByImpl(String impltype) throws StandardException{
        MethodFactory factory = implmap.get(impltype);
        if (factory != null){
            return factory;
        }
        MethodFactory methodFactory = new HeapConglomerateFactory();
        implmap.put(impltype,methodFactory);
        return methodFactory;
    }

    private CacheManager conglom_cache;
    private RawStore rawstore;

    private long conglom_nextid = 0;

    /**
     * 根据工厂类型获取对应的ConglomId
     * */
    public long getNextConglomId(int factory_type)throws StandardException{
        long conglomid;
        synchronized (conglom_cache) {
            if (conglom_nextid == 0) {
                conglom_nextid = (rawstore.getMaxContainerId() >> 4) + 1;
            }
            conglomid = conglom_nextid++;
        }

        // shift in the factory id and then return the conglomid.

        return((conglomid << 4) | factory_type);
    }

    public void conglomCacheAddEntry(long conglomid, Conglomerate conglom) throws StandardException {

        CacheableConglomerate conglom_entry = (CacheableConglomerate) conglom_cache.create(conglomid, conglom);
        conglom_cache.release(conglom_entry);
    }
}
