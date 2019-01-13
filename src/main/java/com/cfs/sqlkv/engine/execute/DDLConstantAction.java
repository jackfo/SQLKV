package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionController;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 09:39
 */
abstract class DDLConstantAction implements ConstantAction {

    /**
     * 获取字典描述
     * */
    static SchemaDescriptor getSchemaDescriptorForCreate(DataDictionary dd, Activation activation, String schemaName) throws StandardException {
        TransactionController tc = null;
        SchemaDescriptor sd = dd.getSchemaDescriptor(schemaName, tc, false);
        return sd;
    }



}
