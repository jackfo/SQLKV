package com.cfs.sqlkv.store;

import com.cfs.sqlkv.column.ColumnOrdering;

import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.row.RowLocationRetRowSource;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.conglomerate.ScanController;
import com.cfs.sqlkv.store.access.conglomerate.ScanManager;
import com.cfs.sqlkv.transaction.AccessManager;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 14:53
 */
public class JuniorTransaction extends TransactionManager {

    protected Transaction rawtran;

    private ArrayList<ScanManager> scanControllers = new ArrayList<>();

    private ArrayList<ConglomerateController> conglomerateControllers = new ArrayList<>();

    public JuniorTransaction(AccessManager myaccessmanager, Transaction theRawTran, TransactionManager parent_transaction)   {
        super(myaccessmanager, theRawTran, parent_transaction);
        this.rawtran = theRawTran;
    }

    @Override
    public void commit()   {

    }

    @Override
    public long createAndLoadConglomerate(String implementation, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, int[] collationIds, Properties properties, int temporaryFlag, RowLocationRetRowSource rowSource, long[] rowCount)   {
        return 0;
    }


    @Override
    public long createConglomerate(String implementation, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, Properties properties)   {
        MethodFactory methodFactory = accessmanager.findMethodFactoryByImpl(implementation);
        if (methodFactory == null) {
            throw new RuntimeException("MethodFactory can't be null");
        }
        if (!(methodFactory instanceof ConglomerateFactory)) {
            throw new RuntimeException("MethodFactory must be the implementation of ConglomerateFactory");
        }
        ConglomerateFactory conglomerateFactory = (ConglomerateFactory) methodFactory;
        int segment = 0;
        int factory_type_id = conglomerateFactory.getConglomerateFactoryId();
        //获取相应的conglomid
        long conglomid = accessmanager.getNextConglomId(factory_type_id);
        Conglomerate conglomerate = conglomerateFactory.createConglomerate(this, segment, conglomid, template, columnOrder, properties);
        conglomid = conglomerate.getContainerid();
        accessmanager.conglomCacheAddEntry(conglomid, conglomerate);
        return conglomid;
    }

    @Override
    public ConglomerateController openConglomerate(long conglomId, boolean hold)   {
        return openConglomerate(findExistingConglomerate(conglomId));
    }

    @Override
    public ScanController openScan(long conglomId, boolean hold, int open_mode, int lock_level, int isolation_level, FormatableBitSet scanColumnList, DataValueDescriptor[] startKeyValue, int startSearchOperator, Qualifier[][] qualifier, DataValueDescriptor[] stopKeyValue, int stopSearchOperator)   {
        return openScan(findExistingConglomerate(conglomId),
                hold, open_mode, lock_level,
                isolation_level, scanColumnList,
                startKeyValue, startSearchOperator,
                qualifier, stopKeyValue, stopSearchOperator);
    }

    public ScanController openScan(Conglomerate conglom,
                                    boolean hold, int open_mode, int lock_level,
                                    int isolation_level, FormatableBitSet scanColumnList,
                                    DataValueDescriptor[] startKeyValue,
                                    int startSearchOperator,
                                    Qualifier qualifier[][],
                                    DataValueDescriptor[] stopKeyValue,
                                    int stopSearchOperator)   {

        ScanManager scanManager = conglom.openScan(this, rawtran, startKeyValue, startSearchOperator,qualifier, stopKeyValue, stopSearchOperator);
        scanControllers.add(scanManager);

        return scanManager;
    }

    public ConglomerateController openConglomerate(Conglomerate conglom)   {
        ConglomerateController cc = conglom.open(this, rawtran);
        conglomerateControllers.add(cc);
        return cc;
    }

    /**
     * 根据conglomId找到对应Conglomerate
     *
     * @return 如果找到则返回对应的conglom 如果没有找到则扔出异常
     */
    private Conglomerate findExistingConglomerate(long conglomId)   {
        Conglomerate conglom = findConglomerate(conglomId);
        if (conglom == null) {
            throw new RuntimeException(String.format("The conglomerate sd requested does not exist  %s", conglomId));
        } else {
            return conglom;
        }
    }


    /**
     * 根据conglomId在访问管理器中找到对应的Conglomerate
     */
    public Conglomerate findConglomerate(long conglomId)   {
        Conglomerate conglom = null;
        if (conglomId >= 0) {
            conglom = accessmanager.conglomCacheFind(conglomId);
        } else {
            if (tempCongloms != null) {
                conglom = tempCongloms.get(conglomId);
            }
        }
        return conglom;
    }


    public Transaction getRawStoreFactoryTransaction() {
        return rawtran;
    }


}
