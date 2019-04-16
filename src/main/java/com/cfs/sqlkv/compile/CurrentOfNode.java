package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.FromList;
import com.cfs.sqlkv.compile.node.FromTable;
import com.cfs.sqlkv.compile.node.ResultSetNode;
import com.cfs.sqlkv.compile.sql.ExecPreparedStatement;
import com.cfs.sqlkv.compile.sql.GenericPreparedStatement;
import com.cfs.sqlkv.compile.table.FromBaseTable;
import com.cfs.sqlkv.sql.activation.Activation;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 14:01
 */
public class CurrentOfNode extends FromTable {

    /**
     * 游标名字
     */
    private String cursorName;

    private ExecPreparedStatement preStmt;

    private TableName exposedTableName;

    private TableName baseTableName;

    private FromBaseTable dummyTargetTable;

    public CurrentOfNode(String correlationName, String cursor, ContextManager cm) {
        super(correlationName, cm);
        cursorName = cursor;
    }





}
