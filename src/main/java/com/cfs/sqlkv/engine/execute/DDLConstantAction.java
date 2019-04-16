package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;

import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.temp.Temp;

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
    static SchemaDescriptor getSchemaDescriptorForCreate(DataDictionary dd, Activation activation, String schemaName)   {
        TransactionManager tc = null;
        SchemaDescriptor sd = Temp.schemaDescriptor;
        return sd;
    }



}
