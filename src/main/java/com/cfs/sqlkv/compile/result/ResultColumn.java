package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.DefaultNode;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.exception.StandardException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 23:43
 */
public class ResultColumn extends ValueNode {

    /**虚拟列标识*/
    private int virtualColumnId;

    private String			_underlyingName;
    private String			_derivedColumnName;
    private String			_unqualifiedTableName;
    private String			_unqualifiedSourceTableName;
    private String			_sourceSchemaName;
    private ValueNode		_expression;
    private boolean			_isGenerated;
    private boolean			_isGeneratedForUnmatchedColumnInInsert;
    private boolean			_isGroupingColumn;
    private boolean			_isReferenced;
    private boolean			_isRedundant;
    private boolean			_isNameGenerated;
    private boolean			_updated;
    private boolean			_updatableByCursor;
    private boolean defaultColumn;
    private boolean wasDefault;

    public ResultColumn(ContextManager cm) {
        super(cm);
    }

    /**
     * @param underlyingName 列的名字
     * @param expression The expression this result column represents
     * @param cm 上下文管理器
     */
    public ResultColumn(String underlyingName, ValueNode expression, ContextManager cm) throws StandardException {
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
    private void setExpression(ValueNode expression) {
        _expression = expression;
    }
}
