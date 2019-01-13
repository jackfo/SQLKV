package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.Storable;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * Conglomerate是抽象的存储结构
 * */
public interface Conglomerate extends DataValueDescriptor {

    public void addColumn(TransactionManager  xact_manager, int column_id, Storable template_column, int collation_id) throws StandardException;

    public void drop(TransactionManager  xact_manager) throws StandardException;

    public long getContainerid();

}
