package com.cfs.sqlkv.btree;


import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.conglomerate.OpenConglomerateScratchSpace;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 16:07
 */
public class OpenBTree {

    private BTree init_conglomerate;
    private TransactionManager init_xact_manager;

    private Transaction init_rawtran;
    public BaseContainerHandle container;
    protected TransactionManager init_open_user_scans = null;
    protected OpenConglomerateScratchSpace runtime_mem;


    public void init(TransactionManager xact_manager, BaseContainerHandle input_container, Transaction rawtran, BTree conglomerate)   {
        if (input_container == null) {
            this.container = rawtran.openContainer(conglomerate.id);
        } else {
            this.container = input_container;
        }
        if (this.container == null) {
            throw new RuntimeException("Btree container not found");
        }
        init_conglomerate = conglomerate;
        init_xact_manager = xact_manager;
        init_rawtran = rawtran;
        this.runtime_mem = conglomerate.getDynamicCompiledConglomInfo();
    }

    public final BTree getConglomerate() {
        return init_conglomerate;
    }

    /**
     * 重新打开容器
     */
    public BaseContainerHandle reopen()   {
        if (container == null) {
            this.container = init_xact_manager.getRawStoreFactoryTransaction().openContainer(init_conglomerate.id);
        }
        return this.container;
    }

    public final OpenConglomerateScratchSpace getRuntimeMem() {
        return runtime_mem;
    }


    public final Transaction getRawTran() {
        return init_rawtran;
    }

    public void close()   {
        if (container != null){
            container.close();
        }
        container = null;
    }
}
