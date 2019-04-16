package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.ColumnReference;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.DefaultNode;
import com.cfs.sqlkv.compile.node.FromList;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.compile.predicate.PredicateList;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 23:43
 */
public class ResultColumn extends ValueNode implements ResultColumnDescriptor {

    /**
     * 虚拟列标识
     */
    private int virtualColumnId;

    private String _underlyingName;
    private String _derivedColumnName;
    /**
     * 列描述
     */
    private ColumnDescriptor _columnDescriptor;
    private String _unqualifiedTableName;
    private String _unqualifiedSourceTableName;
    private String _sourceSchemaName;
    private ValueNode _expression;
    private boolean _isGenerated;
    private boolean _isGeneratedForUnmatchedColumnInInsert;
    private boolean _isGroupingColumn;
    private boolean _isReferenced;
    private boolean _isRedundant;
    private boolean _isNameGenerated;
    private boolean _updated;
    private boolean _updatableByCursor;
    private boolean defaultColumn;
    private boolean wasDefault;

    public ResultColumn(ContextManager cm) {
        super(cm);
    }

    public ResultColumn(ColumnReference cr, ValueNode expression, ContextManager cm) {
        super(cm);
        setTypeExpressionAndDefault(expression);
        _underlyingName = cr.getColumnName();
        _derivedColumnName = cr.getColumnName();
        _reference = cr;
    }

    public ResultColumn(ColumnDescriptor cd, ValueNode expression, ContextManager cm) {
        super(cm);
        setTypeExpressionAndDefault(expression);
        _underlyingName = cd.getColumnName();
        _derivedColumnName = _underlyingName;
        setType(cd.getType());
        _columnDescriptor = cd;
        if (_expression instanceof ColumnReference) {
            _reference = (ColumnReference) expression;
        }
    }

    /**
     * @param underlyingName 列的名字
     * @param expression     The expression this result column represents
     * @param cm             上下文管理器
     */
    public ResultColumn(String underlyingName, ValueNode expression, ContextManager cm) {
        super(cm);
        setTypeExpressionAndDefault(expression);
        _underlyingName = underlyingName;
        _derivedColumnName = _underlyingName;
    }


    public void setVirtualColumnId(int id) {
        virtualColumnId = id;
    }

    private void setTypeExpressionAndDefault(ValueNode expression) {
        setExpression(expression);
        if (expression != null &&
                expression instanceof DefaultNode) {
            // This result column represents a <default> keyword in an insert or
            // update statement
            defaultColumn = true;
        }
    }

    public void setExpression(ValueNode expression) {
        _expression = expression;
    }

    @Override
    public DataTypeDescriptor getType() {
        return getTypeServices();
    }

    @Override
    public DataTypeDescriptor getTypeServices() {
        DataTypeDescriptor type = super.getTypeServices();
        if (type != null)
            return type;
        if (getExpression() != null)
            return getExpression().getTypeServices();
        return null;
    }

    public String getName() {
        return _derivedColumnName;
    }


    /**
     * 设置结果列的表达式
     */
    public void bindResultColumnToExpression() {
        setType(_expression.getTypeServices());
        if (_expression instanceof ColumnReference) {
            ColumnReference cr = (ColumnReference) _expression;
            _unqualifiedTableName = cr.getTableName();
            _unqualifiedSourceTableName = cr.getSourceTableName();
            _sourceSchemaName = cr.getSourceSchemaName();
        }
    }

    public boolean isNameGenerated() {
        return _isNameGenerated;
    }

    public void setName(String name) {
        if (_underlyingName == null) {
            _underlyingName = name;
        }
        _derivedColumnName = name;
    }

    private int resultSetNumber = -1;

    public void setResultSetNumber(int resultSetNumber) {
        this.resultSetNumber = resultSetNumber;
    }

    public ValueNode getExpression() {
        return _expression;
    }

    public ColumnDescriptor getColumnDescriptor() {
        return _columnDescriptor;
    }

    @Override
    public void generateExpression(ActivationClassBuilder ecb, MethodBuilder mb) {
        _expression.generateExpression(ecb, mb);
    }

    public TableName getTableNameObject() {
        return null;
    }

    public void setReferenced() {
        _isReferenced = true;
    }

    public int getColumnPosition() {
        if (_columnDescriptor != null) {
            return _columnDescriptor.getPosition();
        } else {
            return virtualColumnId;
        }
    }

    public void markUpdated() {
        _updated = true;
    }

    public int getResultSetNumber() {
        return resultSetNumber;
    }

    public boolean isRedundant() {
        return _isRedundant;
    }

    public int getVirtualColumnId() {
        return virtualColumnId;
    }

    public boolean isReferenced() {
        return _isReferenced;
    }

    public void adjustVirtualColumnId(int adjust) {
        virtualColumnId += adjust;
    }

    public void markGenerated() {
        _isGenerated = true;
        _isReferenced = true;
    }

    public void bindResultColumn(TableDescriptor tableDescriptor, int columnId) {
        ColumnDescriptor colDesc;
        colDesc = tableDescriptor.getColumnDescriptor(_derivedColumnName);
        if (colDesc == null) {
            throw new RuntimeException("colDesc can't be null");
        }
        setColumnDescriptor(tableDescriptor, colDesc);
        setVirtualColumnId(columnId);
    }


    public void setSourceTableName(String t) {
        _unqualifiedSourceTableName = t;
    }

    public void setSourceSchemaName(String s) {
        _sourceSchemaName = s;
    }

    public String getSourceTableName() {
        return _unqualifiedSourceTableName;
    }

    public String getSourceSchemaName() {
        return _sourceSchemaName;
    }

    public void setColumnDescriptor(TableDescriptor tableDescriptor, ColumnDescriptor columnDescriptor) {
        if (columnDescriptor != null) {
            setType(columnDescriptor.getType());
        }
        _columnDescriptor = columnDescriptor;
    }

    public ResultColumn cloneMe() {
        ResultColumn newResultColumn;
        ValueNode cloneExpr;
        if (_expression instanceof ColumnReference) {
            cloneExpr = ((ColumnReference) _expression).getClone();
        } else {
            cloneExpr = _expression;
        }
        if (_columnDescriptor != null) {
            newResultColumn = new ResultColumn(_columnDescriptor, _expression, getContextManager());
            newResultColumn.setExpression(cloneExpr);
        } else {
            newResultColumn = new ResultColumn(getName(), cloneExpr, getContextManager());
        }

        newResultColumn.setVirtualColumnId(getVirtualColumnId());
        newResultColumn.setName(getName());
        newResultColumn.setType(getTypeServices());
        newResultColumn.setNameGenerated(isNameGenerated());
        newResultColumn.setSourceTableName(getSourceTableName());
        newResultColumn.setSourceSchemaName(getSourceSchemaName());



        /* Set the "is referenced" status in the new node */
        if (isReferenced())
            newResultColumn.setReferenced();

        if (updated())
            newResultColumn.markUpdated();


        if (isGenerated()) {
            newResultColumn.markGenerated();
        }


        return newResultColumn;
    }

    public boolean updated() {
        return _updated;
    }

    public boolean isGenerated() {
        return (_isGenerated == true);
    }


    public void setNameGenerated(boolean value) {
        _isNameGenerated = value;
    }


    void typeUntypedNullExpression(ResultColumn bindingRC) {
        TypeId typeId = bindingRC.getTypeId();
        if (typeId == null) {
            throw new RuntimeException("LANG_NULL_IN_VALUES_CLAUSE");
        }
        if ((_expression instanceof ColumnReference) && _expression.getTypeServices() == null) {
            _expression.setType(bindingRC.getType());
        }
    }

    public void clearTableName() {
        if (_expression instanceof ColumnReference) {
            ((ColumnReference) _expression).setQualifiedTableName((TableName) null);
        }
    }

    @Override
    public ResultColumn bindExpression(FromList fromList) {
        setExpression(_expression.bindExpression(fromList));
        return this;
    }

    private ColumnReference _reference;

    public ColumnReference getReference() {
        return _reference;
    }

    @Override
    public ResultColumn preprocess(int numTables, FromList outerFromList, PredicateList outerPredicateList) {
        if (_expression == null){
            return this;
        }
        setExpression(_expression.preprocess(numTables, outerFromList, outerPredicateList));
        return this;
    }
}
