package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.MethodFactory;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:38
 */
public interface ConglomerateFactory extends MethodFactory {

    static final int    HEAP_FACTORY_ID     = 0x00;
    static final int    BTREE_FACTORY_ID    = 0x01;

    int getConglomerateFactoryId();

    /**
     * 创建一个Conglomerate
     *
     * @param xact_mgr             transaction to perform the create in.
     * @param segment              segment to create the conglomerate in.
     * @param input_containerid    containerid to assign the container, or
     * @param template             Template of row in the conglomerate.
     * @param columnOrder          columns sort order for Index creation
     * @param collationIds         collation ids of columns in the conglomerate.
     * @param properties           Properties associated with the conglomerate.
     * @param temporaryFlag
     *
     * @exception StandardException 出现异常
     *
     * @return 返回一个Conglomerate
     * */
    Conglomerate createConglomerate(
            TransactionManager      xact_mgr,
            int                     segment,
            long                    input_containerid,
            DataValueDescriptor[]   template,
            ColumnOrdering[]		columnOrder,
            int[]                   collationIds,
            Properties properties,
            int						temporaryFlag)
            throws StandardException;


}
