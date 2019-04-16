package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.ColumnReference;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.predicate.Optimizer;
import com.cfs.sqlkv.compile.predicate.PredicateList;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.compile.result.ResultColumnDescriptor;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.GenericExecutionFactory;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 19:55
 */
public class ResultSetNode extends QueryTreeNode {

    private ResultColumnList resultColumns;

    public ResultSetNode(ContextManager contextManager) {
        super(contextManager);
    }

    public ResultSetNode bindNonVTITables(DataDictionary dataDictionary, FromList fromListParam) {
        return this;
    }

    public void setResultColumns(ResultColumnList newRCL) {
        resultColumns = newRCL;
    }

    public ResultColumnList getResultColumns() {
        return resultColumns;
    }

    private int resultSetNumber;

    public int getResultSetNumber() {
        return resultSetNumber;
    }

    public void setResultSetNumber(int rsn) {
        resultSetNumber = rsn;
    }

    public void assignResultSetNumber() {
        resultSetNumber = getCompilerContext().getNextResultSetNumber();
        resultColumns.setResultSetNumber(resultSetNumber);
    }

    public ResultSetNode bindVTITables(FromList fromListParam) {
        return this;
    }

    private boolean statementResultSet;

    public void markStatementResultSet() {
        statementResultSet = true;
    }

    public void bindResultColumns(FromList fromListParam) {
        //resultColumns.bindResultColumnsToExpressions();
    }

    public void bindResultColumns(FromList fromListParam,TableDescriptor targetTableDescriptor, ResultColumnList targetColumnList) {
        if (targetColumnList != null) {
            resultColumns.copyResultColumnNames(targetColumnList);
        }
        if (targetColumnList != null) {
           resultColumns.bindResultColumnsByName(targetTableDescriptor);
        } else{
            resultColumns.bindResultColumnsByPosition(targetTableDescriptor);
        }
    }

    public void renameGeneratedResultNames() {
        for (int i = 0; i < resultColumns.size(); i++) {
            ResultColumn rc = resultColumns.elementAt(i);
            if (rc.isNameGenerated())
                rc.setName(Integer.toString(i + 1));
        }
    }

    public ResultDescription makeResultDescription() {
        ResultColumnDescriptor[] colDescs = makeResultDescriptors();
        return new ResultDescription(colDescs, null);
    }


    public ResultColumn getMatchingColumn(ColumnReference columnReference) {
        return null;
    }

    public ResultColumnDescriptor[] makeResultDescriptors() {
        return resultColumns.makeResultDescriptors();
    }


    public ResultSetNode preprocess(int numTables, FromList fromList) {
        return null;
    }

    public ResultSetNode modifyAccessPaths() {
        return this;
    }

    public ResultSetNode optimize(DataDictionary dataDictionary, PredicateList predicates) {
        return null;
    }

    public ResultSetNode modifyAccessPaths(PredicateList predList) {
        return modifyAccessPaths();
    }

    public ResultSetNode changeAccessPath() {
        return this;
    }

    public void generateResultSet(ActivationClassBuilder acb, MethodBuilder mb) {

    }

    public void bindExpressions(FromList fromListParam) {

    }

    public ResultColumnList getAllResultColumns(TableName allTableName) {
        return null;
    }

    public void pushQueryExpressionSuffix() {
    }

    private Optimizer optimizer;

    public Optimizer getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(Optimizer opt) {
        optimizer = opt;
    }
}
