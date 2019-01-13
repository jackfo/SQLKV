package com.cfs.sqlkv.compile.table;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.FromTable;
import com.cfs.sqlkv.compile.result.ResultColumnList;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-04 20:17
 */
public class FromBaseTable extends FromTable {
    public  FromBaseTable(TableName tableName, String correlationName, ResultColumnList derivedRCL,
                          Properties tableProperties, ContextManager cm) {
    }
}
