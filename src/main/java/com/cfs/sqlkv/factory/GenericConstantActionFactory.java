package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.engine.execute.ColumnInfo;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.engine.execute.CreateConstraintConstantAction;
import com.cfs.sqlkv.engine.execute.CreateTableConstantAction;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 19:44
 */
public class GenericConstantActionFactory {

    public static ConstantAction getCreateTableConstantAction(
                    String			schemaName,
                    String			tableName,
                    int				tableType,
                    ColumnInfo[]	columnInfo,
                    CreateConstraintConstantAction[] constraintActions,
                    Properties properties,
                    char			lockGranularity,
                    boolean			onCommitDeleteRows,
                    boolean			onRollbackDeleteRows)
    {
        return new CreateTableConstantAction(schemaName, tableName, tableType, columnInfo,
                constraintActions, properties,
                lockGranularity, onCommitDeleteRows, onRollbackDeleteRows);
    }
}
