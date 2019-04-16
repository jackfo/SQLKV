package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.ValueNode;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-06 22:19
 */
public class BaseColumnNode extends ValueNode {
    private String columnName;
    private TableName tableName;

    public BaseColumnNode(
            String columnName,
            TableName tableName,
            DataTypeDescriptor dtd,
            ContextManager cm){
        super(cm);
        this.columnName = columnName;
        this.tableName = tableName;
        setType(dtd);
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public String getTableName() {
        return ((tableName != null) ? tableName.getTableName() : null);
    }

    @Override
    public String getSchemaName() {
        return ((tableName != null) ? tableName.getSchemaName() : null);
    }


}
