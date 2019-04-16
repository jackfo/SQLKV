package com.cfs.sqlkv.context;

import com.cfs.sqlkv.catalog.SchemaDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 21:04
 */
public class SQLSessionContext {

    /**
     * 当前默认模式
     * */
    private SchemaDescriptor currentDefaultSchema;

    public SQLSessionContext(SchemaDescriptor sd){
        currentDefaultSchema = sd;
    }

    public SchemaDescriptor getDefaultSchema(){
        return currentDefaultSchema;
    }

    public void setDefaultSchema(SchemaDescriptor sd){
        this.currentDefaultSchema = sd;
    }
}
