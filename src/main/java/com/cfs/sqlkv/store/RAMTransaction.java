package com.cfs.sqlkv.store;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.row.RowLocationRetRowSource;
import com.cfs.sqlkv.store.access.RAMAccessManager;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.conglomerate.TransactionManager;
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
public class RAMTransaction implements TransactionManager {

    private long nextTempConglomId = -1;

    protected HashMap<Long,Conglomerate> tempCongloms;

    public RAMTransaction(AccessManager myaccessmanager, Transaction theRawTran, RAMTransaction   parent_transaction) throws StandardException {

    }

    @Override
    public void commit() throws StandardException {

    }

    @Override
    public long createAndLoadConglomerate(String implementation, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, int[] collationIds, Properties properties, int temporaryFlag, RowLocationRetRowSource rowSource, long[] rowCount) throws StandardException {
        return 0;
    }

    protected RAMAccessManager accessmanager;

    /**
     * 具体实现过程会创建相应的文件
     * */
    @Override
    public long createConglomerate(String implementation, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, int[] collationIds, Properties properties, int temporaryFlag) throws StandardException {
        /**找到对应的方法工厂*/
        MethodFactory methodFactory;

        methodFactory = accessmanager.findMethodFactoryByImpl(implementation);
        if(methodFactory==null || !(methodFactory instanceof ConglomerateFactory)){
            throw new RuntimeException("methodFactory不能为空 或者未实现ConglomerateFactory");
        }
        ConglomerateFactory conglomerateFactory = (ConglomerateFactory) methodFactory;

        int     segment;
        long    conglomid;

        if ((temporaryFlag & TransactionController.IS_TEMPORARY) == TransactionController.IS_TEMPORARY) {
            segment = BaseContainerHandle.TEMPORARY_SEGMENT;
            conglomid = BaseContainerHandle.DEFAULT_ASSIGN_ID;
        } else {
            segment = 0;
            conglomid = accessmanager.getNextConglomId(conglomerateFactory.getConglomerateFactoryId());
        }

        Conglomerate conglom = conglomerateFactory.createConglomerate(this, segment, conglomid, template, columnOrder, collationIds, properties, temporaryFlag);


        long conglomId;
        if ((temporaryFlag & TransactionController.IS_TEMPORARY) == TransactionController.IS_TEMPORARY) {
            conglomId = nextTempConglomId--;
            if (tempCongloms == null)
                tempCongloms = new HashMap<Long,Conglomerate>();
            tempCongloms.put(conglomId, conglom);
        }
        else {
            conglomId = conglom.getContainerid();
            accessmanager.conglomCacheAddEntry(conglomId, conglom);
        }
        return conglomId;

    }
}
