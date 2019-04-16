package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.FromList;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.row.ValueRow;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-28 11:48
 */
public class ColumnReference extends ValueNode {

    private String _columnName;

    private int tableNumber;

    private int columnNumber;

    private ResultColumn source;

    private TableName _qualifiedTableName;

    public ColumnReference(String columnName, TableName tableName, int tokBeginOffset, int tokEndOffset, ContextManager cm) {
        super(cm);
        _columnName = columnName;
        _qualifiedTableName = tableName;
        this.setBeginOffset(tokBeginOffset);
        this.setEndOffset(tokEndOffset);
        tableNumber = -1;
    }

    public ColumnReference(String columnName, TableName tableName, ContextManager cm) {
        super(cm);
        _columnName = columnName;
        _qualifiedTableName = tableName;
        tableNumber = -1;
    }


    public String getSQLColumnName() {
        if (_qualifiedTableName == null) {
            return _columnName;
        }
        return _qualifiedTableName.toString() + "." + _columnName;
    }

    @Override
    public String getColumnName() {
        return _columnName;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }


    @Override
    public ValueNode bindExpression(FromList fromList) {
        fromList.bindColumnReference(this);
        return this;
    }

    @Override
    public String getTableName() {
        return ((_qualifiedTableName != null) ? _qualifiedTableName.getTableName() : null);
    }

    @Override
    public DataTypeDescriptor getTypeServices() {
        if (source == null) {
            return super.getTypeServices();
        }
        return source.getTypeServices();
    }

    public TableName getQualifiedTableName() {
        return _qualifiedTableName;
    }

    public void setColumnNumber(int colNum) {
        this.columnNumber = colNum;
    }


    public void setSource(ResultColumn source) {
        this.source = source;
    }

    public ResultColumn getSource() {
        return source;
    }

    public void setQualifiedTableName(TableName tableName) {
        _qualifiedTableName = tableName;
    }

    private int nestingLevel = -1;
    private int sourceLevel = -1;

    public boolean getCorrelated() {
        return sourceLevel != nestingLevel;
    }

    private int getNestingLevel() {
        return nestingLevel;
    }

    public int getSourceLevel() {
        return sourceLevel;
    }


    public ValueNode getClone() {
        ColumnReference newCR = new ColumnReference(_columnName, _qualifiedTableName, getContextManager());

        newCR.copyFields(this);
        return newCR;
    }

    void copyFields(ColumnReference oldCR) {
        super.copyFields(oldCR);
        setQualifiedTableName(oldCR.getQualifiedTableName());
        tableNumber = oldCR.getTableNumber();
        columnNumber = oldCR.getColumnNumber();
        source = oldCR.getSource();
        nestingLevel = oldCR.getNestingLevel();
        sourceLevel = oldCR.getSourceLevel();
    }

    public String getSourceTableName() {
        return ((source != null) ? source.getTableName() : null);
    }

   public String getSourceSchemaName(){
        return ((source != null) ? source.getSchemaName() : null);
    }

    @Override
    public String toString() {
        return "ColumnReference{" +
                "_columnName='" + _columnName + '\'' +
                ", tableNumber=" + tableNumber +
                ", columnNumber=" + columnNumber +
                ", source=" + source +
                ", _qualifiedTableName=" + _qualifiedTableName +
                '}';
    }
}
