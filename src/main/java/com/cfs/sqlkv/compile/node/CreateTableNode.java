package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.table.TableElementList;
import com.cfs.sqlkv.compile.table.TableElementNode;
import com.cfs.sqlkv.engine.execute.ColumnInfo;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.engine.execute.CreateConstraintConstantAction;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.GenericConstantActionFactory;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 19:14
 */
public class CreateTableNode extends DDLStatementNode {


    private char				lockGranularity;
    private boolean				onCommitDeleteRows; //If true, on commit delete rows else on commit preserve rows of temporary table.
    private boolean				onRollbackDeleteRows; //If true, on rollback delete rows from temp table if it was logically modified in that UOW. true is the only supported value
    private Properties			properties;
    private ResultColumnList    resultColumns;
    private ResultSetNode		queryExpression;

    /**
     * 表的元素集合
     * */
    private TableElementList tableElementList;
    /**
     * 创建表的节点
     * */
    public CreateTableNode(TableName tableName, TableElementList tableElementList, Properties properties,
                           char lockGranularity, ContextManager cm) throws StandardException {
        super(tableName, cm);
        this.tableElementList = tableElementList;
        this.tableName = tableName;
    }



    protected int tableType;

    /**
     * 绑定创建表的节点
     * */
    @Override
    public void bindStatement() throws StandardException{
        //获取数据字典
        DataDictionary dataDictionary = getDataDictionary();
        //主键数目
        int numPrimaryKeys;
        //约束数目
        int numCheckConstraints;

        //获取模式描述
        SchemaDescriptor sd = getSchemaDescriptor(tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE, true);

        //TODO:查询表达式

        tableElementList.setCollationTypesOnCharacterStringColumns(getSchemaDescriptor(tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE, true));

        //TODO:列大于限制
    }


    @Override
    public ConstantAction makeConstantAction() throws StandardException{
        TableElementList coldefs = tableElementList;

        ColumnInfo[] colInfos = new ColumnInfo[coldefs.countNumberOfColumns()];

        int numConstraints = coldefs.genColumnInfos(colInfos);

        SchemaDescriptor sd = getSchemaDescriptor(tableType != TableDescriptor.GLOBAL_TEMPORARY_TABLE_TYPE, true);

        CreateConstraintConstantAction[] conActions = null;

        return(GenericConstantActionFactory.getCreateTableConstantAction(
                        sd.getSchemaName(),
                        getRelativeName(),
                        tableType,
                        colInfos,
                        conActions,
                        properties,
                        lockGranularity,
                        onCommitDeleteRows,
                        onRollbackDeleteRows));
    }

    private TableName   tableName;

    String getRelativeName() {
        return tableName.getTableName() ;
    }




}
