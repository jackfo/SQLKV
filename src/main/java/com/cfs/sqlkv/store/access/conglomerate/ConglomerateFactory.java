package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.column.ColumnOrdering;

import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:38
 */
public interface ConglomerateFactory extends MethodFactory {

    static final int HEAP_FACTORY_ID = 0x00;
    static final int BTREE_FACTORY_ID = 0x01;

    public int getConglomerateFactoryId();

    /**
     * 创建一个Conglomerate
     *
     * @param transactionManager transaction to perform the create in.
     * @param segment            segment to create the conglomerate in.
     * @param input_containerid  containerid to assign the container, or
     * @param template           Template of row in the conglomerate.
     * @return 返回一个Conglomerate
     * @  出现异常
     */
    public Conglomerate createConglomerate(TransactionManager transactionManager, int segment, long input_containerid, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, Properties properties)  ;


    public Conglomerate readConglomerate(TransactionManager transactionManager, ContainerKey container_key)  ;


}
