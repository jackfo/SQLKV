package com.cfs.sqlkv.store;

import com.cfs.sqlkv.column.ColumnOrdering;

import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.row.RowLocationRetRowSource;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.conglomerate.ScanController;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.transaction.AccessManager;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.HashMap;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 16:07
 */
public abstract class TransactionManager {
    public static final int OPENMODE_BASEROW_INSERT_LOCKED = 0x00004000;
    public static final int OPENMODE_FORUPDATE = 0x00000004;
    private long nextTempConglomId = -1;

    protected HashMap<Long, Conglomerate> tempCongloms;

    public TransactionManager(AccessManager myaccessmanager, Transaction theRawTran, TransactionManager parent_transaction)   {
        this.accessmanager = myaccessmanager;
    }

    protected AccessManager accessmanager;

    /**
     * 具体实现过程会创建相应的文件
     */
//    public long createConglomerate(String implementation, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, int[] collationIds, Properties properties, int temporaryFlag)   {
//        /**找到对应的方法工厂*/
//        MethodFactory methodFactory;
//        methodFactory = accessmanager.findMethodFactoryByImpl(implementation);
//        if(methodFactory==null || !(methodFactory instanceof ConglomerateFactory)){
//            throw new RuntimeException("methodFactory不能为空 或者未实现ConglomerateFactory");
//        }
//        ConglomerateFactory conglomerateFactory = (ConglomerateFactory) methodFactory;
//        int     segment;
//        long    conglomid;
//
//        if ((temporaryFlag & TransactionManager.IS_TEMPORARY) == TransactionManager.IS_TEMPORARY) {
//            segment = BaseContainerHandle.TEMPORARY_SEGMENT;
//            conglomid = BaseContainerHandle.DEFAULT_ASSIGN_ID;
//        } else {
//            segment = 0;
//            conglomid = accessmanager.getNextConglomId(conglomerateFactory.getConglomerateFactoryId());
//        }
//
//        Conglomerate conglom = conglomerateFactory.createConglomerate(this, segment, conglomid, template);
//
//
//        long conglomId;
//        if ((temporaryFlag & TransactionManager.IS_TEMPORARY) == TransactionManager.IS_TEMPORARY) {
//            conglomId = nextTempConglomId--;
//            if (tempCongloms == null)
//                tempCongloms = new HashMap<Long,Conglomerate>();
//            tempCongloms.put(conglomId, conglom);
//        }
//        else {
//            conglomId = conglom.getContainerid();
//            accessmanager.conglomCacheAddEntry(conglomId, conglom);
//        }
//        return conglomId;
//
//    }
    public Transaction getRawStoreFactoryTransaction() {
        return null;
    }


    public static final int MODE_RECORD = 6;

    public static final int ISOLATION_NOLOCK = 0;

    public static final int ISOLATION_READ_UNCOMMITTED = 1;

    /**
     * No lost updates, no dirty reads, only committed data is returned.
     * Writes only visible when committed.  Exclusive transaction
     * length locks are set on data that is written, short term locks (
     * possibly instantaneous duration locks) are set
     * on data that is read.
     **/
    public static final int ISOLATION_READ_COMMITTED = 2;

    /**
     * No lost updates, no dirty reads, only committed data is returned.
     * Writes only visible when committed.  Exclusive transaction
     * length locks are set on data that is written, short term locks (
     * possibly instantaneous duration locks) are set
     * on data that is read.  Read locks are requested for "zero" duration,
     * thus upon return from access no read row lock is held.
     **/
    public static final int ISOLATION_READ_COMMITTED_NOHOLDLOCK = 3;

    /**
     * 可重复读
     */
    public static final int ISOLATION_REPEATABLE_READ = 4;

    /**
     * 串行化
     */
    public static final int ISOLATION_SERIALIZABLE = 5;

    public abstract void commit()  ;

    /**
     * 初始化flag
     */
    static final byte IS_DEFAULT = (byte) 0x00;
    /**
     * conglom是临时的
     */
    static final byte IS_TEMPORARY = (byte) 0x01;
    static final byte IS_KEPT = (byte) 0x02;

    /**
     *
     */
    public abstract long createAndLoadConglomerate(
            String implementation,
            DataValueDescriptor[] template,
            ColumnOrdering[] columnOrder,
            int[] collationIds,
            Properties properties,
            int temporaryFlag,
            RowLocationRetRowSource rowSource,
            long[] rowCount)
             ;


    public abstract long createConglomerate(String implementation, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, Properties properties)  ;

    public abstract ConglomerateController openConglomerate(long conglomId, boolean hold);

    public abstract ConglomerateController openConglomerate(Conglomerate conglomerate);

    public abstract ScanController openScan(long conglomId, boolean hold, int open_mode, int lock_level, int isolation_level,
                                            FormatableBitSet scanColumnList, DataValueDescriptor[] startKeyValue,
                                            int startSearchOperator, Qualifier qualifier[][], DataValueDescriptor[] stopKeyValue,
                                            int stopSearchOperator)  ;

    public abstract ScanController openScan(Conglomerate conglom,
                                   boolean hold, int open_mode, int lock_level,
                                   int isolation_level, FormatableBitSet scanColumnList,
                                   DataValueDescriptor[] startKeyValue,
                                   int startSearchOperator,
                                   Qualifier qualifier[][],
                                   DataValueDescriptor[] stopKeyValue,
                                   int stopSearchOperator);

    public abstract Conglomerate findConglomerate(long conglomId);
}
