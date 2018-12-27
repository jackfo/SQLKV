package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.catalog.SchemaDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:30
 */
public class GenericStatement {

    private final SchemaDescriptor compilationSchema;
    private final String statementText;
    private final boolean isForReadOnly;


    public GenericStatement(SchemaDescriptor schemaDescriptor,String statementText, boolean isForReadOnly){
        this.compilationSchema = schemaDescriptor;
        this.statementText = statementText;
        this.isForReadOnly = isForReadOnly;
    }


}
