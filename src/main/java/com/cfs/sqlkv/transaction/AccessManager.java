package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.btree.BTreeFactory;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.context.RAMTransactionContext;

import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.row.RawStoreFactory;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.cache.CacheableFactory;
import com.cfs.sqlkv.store.JuniorTransaction;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.AccessFactoryGlobals;
import com.cfs.sqlkv.store.access.CacheableConglomerate;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.heap.TableConglomerateFactory;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 16:18
 */
public class AccessManager implements CacheableFactory {

    public static final String RAMXACT_CONTEXT_ID = "RAMTransactionContext";
    public static final String USER_TRANS_NAME = "UserTransaction";
    private ConcurrentHashMap<String, MethodFactory> implmap = new ConcurrentHashMap<>();
    protected ConglomerateFactory conglom_map[] = new ConglomerateFactory[2];
    private CacheManager conglom_cache = new CacheManager(this, AccessFactoryGlobals.CFG_CONGLOMDIR_CACHE, 200, 300);
    private RawStoreFactory rawStoreFactory = new RawStoreFactory();
    private long conglom_nextid = 0;

    public AccessManager() {
        conglom_map[ConglomerateFactory.HEAP_FACTORY_ID] = new TableConglomerateFactory();
        conglom_map[ConglomerateFactory.BTREE_FACTORY_ID] = new BTreeFactory();
    }


    public MethodFactory findMethodFactoryByImpl(String impltype) {
        MethodFactory factory = implmap.get(impltype);
        if (factory != null) {
            return factory;
        }
        switch (impltype) {
            case BTreeFactory.IMPLEMENTATIONID:
                factory = new BTreeFactory();
                implmap.put(impltype, factory);
                break;
            case TableConglomerateFactory.IMPLEMENTATIONID:
                factory = new TableConglomerateFactory();
                implmap.put(impltype, factory);
                break;
            default:
                throw new RuntimeException("");
        }
        return factory;
    }

    public TransactionManager getTransaction(ContextManager cm) {
        return getAndNameTransaction(cm, USER_TRANS_NAME);
    }

    public void conglomCacheAddEntry(long conglomid, Conglomerate conglom) {
        conglom_cache.create(conglomid, conglom);
    }

    public ConglomerateFactory getFactoryFromConglomId(long conglom_id) {
        return (conglom_map[((int) (0x0f & conglom_id))]);
    }

    /**
     * 获取对应ConglomId
     * 由于文件存储采用的是16进制
     * ConglomId的高位是文件对应的编号,低四位是对应的工厂类型
     */
    public long getNextConglomId(int factory_type) {
        long conglomid;
        synchronized (conglom_cache) {
            if (conglom_nextid == 0) {
                conglom_nextid = (rawStoreFactory.getMaxContainerId() >> 4) + 1;
            }
            conglomid = conglom_nextid++;
        }
        return ((conglomid << 4) | factory_type);
    }

    /**
     * 获取事务上下文
     */
    public RAMTransactionContext getCurrentTransactionContext() {
        RAMTransactionContext ramTransactionContext = (RAMTransactionContext) ContextService.getContext(AccessFactoryGlobals.RAMXACT_CONTEXT_ID);
        return ramTransactionContext;
    }


    /**
     * 通过事务名称获取对应事务控制器
     * TODO:
     */
    public TransactionManager getAndNameTransaction(ContextManager cm, String transName) {
        RAMTransactionContext ramTransactionContext = (RAMTransactionContext) cm.getContext(AccessFactoryGlobals.RAMXACT_CONTEXT_ID);
        if (ramTransactionContext == null) {
            Transaction rawtran = rawStoreFactory.findUserTransaction(cm, transName);
            JuniorTransaction transaction = new JuniorTransaction(this, rawtran, null);
            ramTransactionContext = new RAMTransactionContext(cm, AccessFactoryGlobals.RAMXACT_CONTEXT_ID, transaction, false);
        }
        return ramTransactionContext.getTransaction();
    }

    /**
     * 通过缓存管理器找到对应的Conglomerate
     */
    public Conglomerate conglomCacheFind(long conglomid) {
        Conglomerate conglom = null;
        Long conglomid_obj = conglomid;

        CacheableConglomerate cache_entry = (CacheableConglomerate) conglom_cache.find(conglomid_obj);
        if (cache_entry != null) {
            conglom = cache_entry.getConglom();
            conglom_cache.release(cache_entry);
        }
        return conglom;
    }

    @Override
    public Cacheable newCacheable(CacheManager cm) {
        return new CacheableConglomerate(this);
    }

    public RawStoreFactory getRawStoreFactory() {
        return rawStoreFactory;
    }
}
