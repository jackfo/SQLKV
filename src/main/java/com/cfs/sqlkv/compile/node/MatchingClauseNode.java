package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.result.ResultColumnList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-12 11:51
 */
public class MatchingClauseNode extends QueryTreeNode {

    private ValueNode _matchingRefinement;
    private ResultColumnList _updateColumns;
    private ResultColumnList _insertColumns;
    private ResultColumnList _insertValues;

    private DMLStatementNode _dml;
    private ResultColumnList _thenColumns;

    private int _clauseNumber;
    private String _actionMethodName;
    private String _resultSetFieldName;
    private String _rowMakingMethodName;

    private MatchingClauseNode(ValueNode matchingRefinement, ResultColumnList updateColumns,
                               ResultColumnList insertColumns, ResultColumnList insertValues, ContextManager cm) {
        super(cm);
        _matchingRefinement = matchingRefinement;
        _updateColumns = updateColumns;
        _insertColumns = insertColumns;
        _insertValues = insertValues;
    }

    public static MatchingClauseNode makeUpdateClause(ValueNode matchingRefinement, ResultColumnList updateColumns, ContextManager cm) {
        return new MatchingClauseNode(matchingRefinement, updateColumns, null, null, cm);
    }

    public static MatchingClauseNode makeDeleteClause(ValueNode matchingRefinement, ContextManager cm) {
        return new MatchingClauseNode(matchingRefinement, null, null, null, cm);
    }

    public static MatchingClauseNode makeInsertClause(ValueNode matchingRefinement, ResultColumnList insertColumns, ResultColumnList insertValues, ContextManager cm) {
        return new MatchingClauseNode(matchingRefinement, null, insertColumns, insertValues, cm);
    }

    public boolean isUpdateClause() {
        return (_updateColumns != null);
    }

    public boolean isInsertClause() {
        return (_insertValues != null);
    }

    public boolean isDeleteClause() {
        return !(isUpdateClause() || isInsertClause());
    }

    public ResultColumnList getThenColumns() {
        return _thenColumns;
    }
}
