package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:40
 */
public class HeapConglomerateFactory implements ConglomerateFactory {


    /**
     * 返回处理的id的方式
     * */
    @Override
    public int getConglomerateFactoryId() {
        return ConglomerateFactory.HEAP_FACTORY_ID;
    }

    @Override
    public Conglomerate createConglomerate(TransactionManager xact_mgr, int segment, long input_containerid, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, int[] collationIds, Properties properties, int temporaryFlag) throws StandardException {
        Heap heap = new Heap();


    }
}
