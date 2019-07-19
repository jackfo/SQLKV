package com.cfs.sqlkv.btree;

import com.cfs.sqlkv.column.ColumnOrdering;

import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-03 00:02
 */
public class BTreeFactory implements ConglomerateFactory {
    public static final String IMPLEMENTATIONID = "BTREE";
    private static final String FORMATUUIDSTRING = "C6CEEEF0-DAD3-11d0-BB01-0060973F0942";

    @Override
    public int getConglomerateFactoryId() {
        return ConglomerateFactory.BTREE_FACTORY_ID;
    }

    @Override
    public Conglomerate createConglomerate(TransactionManager transactionManager, int segment, long input_containerid, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, Properties properties)   {
        BTree bTree = new BTree();
        bTree.create(transactionManager, segment, input_containerid, template, columnOrder, properties);
        return bTree;
    }

    @Override
    public Conglomerate readConglomerate(TransactionManager transactionManager, ContainerKey container_key)   {
        Conglomerate btree = null;
        BaseContainerHandle container = null;
        ControlRow root = null;
        try {
            Transaction transaction = transactionManager.getRawStoreFactoryTransaction();
            container = transaction.openContainer(container_key);
            if (container == null) {
                throw new RuntimeException("container can't be null");
            }
            root = ControlRow.getPage(container, BTree.ROOTPAGEID);
            btree = (Conglomerate) root.getConglom(BTree.FORMAT_NUMBER);

        } finally {
            if (root != null){
                root.release();
            }
            if (container != null){
                container.close();
            }
        }
        return btree;
    }
}
