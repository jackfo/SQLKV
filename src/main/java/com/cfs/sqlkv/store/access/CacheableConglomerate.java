package com.cfs.sqlkv.store.access;

import com.cfs.sqlkv.context.RAMTransactionContext;

import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.transaction.AccessManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 19:48
 */
public class CacheableConglomerate implements Cacheable {

    private final AccessManager accessManager;
    private Long conglomid;
    private Conglomerate conglom;
    public CacheableConglomerate(AccessManager parent) {
        this.accessManager = parent;
    }

    public Conglomerate getConglom(){
        return this.conglom;
    }

    @Override
    public Cacheable createIdentity(Object key, Object createParameter)   {
        this.conglomid = (Long) key;
        this.conglom   = ((Conglomerate) createParameter);
        return this;
    }

    /**
     * 需要根据key来识别对应的工厂
     * 读取到对应的
     * */
    public Cacheable setIdentity(Object key)   {
        conglomid = (Long) key;
        long id = conglomid.longValue();
        RAMTransactionContext ramTransactionContext = accessManager.getCurrentTransactionContext();
        TransactionManager transactionManager = ramTransactionContext.getTransaction();
        ConglomerateFactory conglomerateFactory = accessManager.getFactoryFromConglomId(conglomid);
        conglom = conglomerateFactory.readConglomerate(transactionManager,new ContainerKey(0, id));
        return this;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public Object getIdentity() {
        return this.conglomid;
    }

    @Override
    public void clearIdentity() {
        this.conglomid = null;
        this.conglom   = null;
    }

    @Override
    public void clean(boolean forRemove)   {

    }
}
