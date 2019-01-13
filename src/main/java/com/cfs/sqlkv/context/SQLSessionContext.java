package com.cfs.sqlkv.context;

import com.cfs.sqlkv.catalog.SchemaDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 21:04
 */
public class SQLSessionContext {

    private String currentUser;
    private String currentRole;
    private SchemaDescriptor currentDefaultSchema;

    public SQLSessionContext(SchemaDescriptor sd,String currentUser){
        currentRole = null;
        currentDefaultSchema = sd;
        this.currentUser = currentUser;
    }

    public SchemaDescriptor getDefaultSchema(){
        return currentDefaultSchema;
    }
}
