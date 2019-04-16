package com.cfs.sqlkv.store.access.heap;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateFactory;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-26 12:47
 */
public class TableConglomerateFactory implements ConglomerateFactory {

    public static final String IMPLEMENTATIONID = "table";
    private static final String FORMATUUIDSTRING = "D2976090-D9F5-11d0-B54D-00A024BF8878";
    private UUID formatUUID;

    public int getConglomerateFactoryId() {
        return ConglomerateFactory.HEAP_FACTORY_ID;
    }

    @Override
    public Conglomerate createConglomerate(TransactionManager transactionManager, int segment, long input_containerid, DataValueDescriptor[] template,
                                           ColumnOrdering[] columnOrder, Properties properties)   {
        Heap heap = new Heap();
        heap.create(transactionManager.getRawStoreFactoryTransaction(), segment, input_containerid, template, heap.getTypeFormatId());
        return heap;
    }

    /**
     * 根据container_key找到对应的容器对象
     */
    @Override
    public Conglomerate readConglomerate(TransactionManager transactionManager, ContainerKey container_key)   {
        BaseContainerHandle baseContainerHandle = null;
        Page page = null;
        //构建控制行数据描述
        DataValueDescriptor[] control_row = new DataValueDescriptor[1];
        Transaction raw_transaction = transactionManager.getRawStoreFactoryTransaction();
        //根据容器key找到对应的容器句柄
        baseContainerHandle = raw_transaction.openContainer(container_key);
        control_row[0] = new Heap();
        //获取当前容器第一页,即分配页
        page = baseContainerHandle.getPage(BaseContainerHandle.FIRST_PAGE_NUMBER);
        //获取记录到control_row中去
        RecordId rh = page.fetchFromSlot(null, 0, control_row, null, true);
        return (Conglomerate) control_row[0];
    }


}
