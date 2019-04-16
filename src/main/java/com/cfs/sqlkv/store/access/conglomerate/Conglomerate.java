package com.cfs.sqlkv.store.access.conglomerate;


import com.cfs.sqlkv.io.storage.Storable;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * Conglomerate是抽象的存储结构
 */
public interface Conglomerate extends Storable, DataValueDescriptor {

    public void addColumn(TransactionManager xact_manager, int column_id, Storable template_column, int collation_id);

    public void drop(TransactionManager xact_manager);

    public long getContainerid();

    ContainerKey getId();

    public ConglomerateController open(TransactionManager xact_manager, Transaction rawtran);


    public ScanManager openScan(TransactionManager transactionManager, Transaction raw_transaction, DataValueDescriptor[] startKeyValue, int startSearchOperator, Qualifier qualifier[][], DataValueDescriptor[] stopKeyValue, int stopSearchOperator);

    public OpenConglomerateScratchSpace getDynamicCompiledConglomInfo();
}
