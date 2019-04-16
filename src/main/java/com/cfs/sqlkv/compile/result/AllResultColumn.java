package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 11:15
 */
public class AllResultColumn extends ResultColumn{

    private TableName tableName;

    public AllResultColumn(TableName tableName, ContextManager cm){
        super(cm);
    }


    @Override
    public TableName getTableNameObject() {
        return tableName;
    }
}
