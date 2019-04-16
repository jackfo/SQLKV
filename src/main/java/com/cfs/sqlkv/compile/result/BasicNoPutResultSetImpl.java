package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.context.StatementContext;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 17:16
 */
public abstract class BasicNoPutResultSetImpl implements NoPutResultSet {

    protected boolean isOpen;
    protected boolean finished;
    public ExecRow currentRow;
    protected boolean isTopResultSet;

    protected final Activation activation;

    private StatementContext statementContext;

    ExecRow compactRow;

    private int[] baseColumnMap;


    /**
     * 子结果集
     */
    public NoPutResultSet[] subqueryTrackingArray;

    public long beginTime;

    public BasicNoPutResultSetImpl(ResultDescription resultDescription, Activation activation, double optimizerEstimatedRowCount, double optimizerEstimatedCost) {
        this.activation = activation;
        this.resultDescription = resultDescription;
    }

    @Override
    public final void open() {
        //设置为未完成
        finished = false;
        //获取对应的上下文
        attachStatementContext();
        openCore();
    }

    public boolean returnsRows() {
        return true;
    }

    protected void attachStatementContext() {
        if (isTopResultSet) {
            if (statementContext == null || !statementContext.onStack()) {
                statementContext = getLanguageConnectionContext().getStatementContext();
            }
            statementContext.setTopResultSet(this, subqueryTrackingArray);
            if (subqueryTrackingArray == null) {
                subqueryTrackingArray = statementContext.getSubqueryTrackingArray();
            }
            statementContext.setActivation(activation);
        }
    }

    protected final LanguageConnectionContext getLanguageConnectionContext() {
        return getActivation().getLanguageConnectionContext();
    }

    public final Activation getActivation() {
        return activation;
    }

    @Override
    public final ExecRow getNextRow() {
        if (!isOpen) {
            throw new RuntimeException(String.format("ResultSet not open. Operation '{0}' not permitted. Verify that autocommit is off", "NEXT"));
        }
        attachStatementContext();
        return getNextRowCore();
    }

    public abstract ExecRow getNextRowCore();

    /**
     * 通过行指定的位图获取行的压缩版本
     * 如果没有位图,采用候选行错位压缩行
     *
     * @param candidate
     * @param accessedCols
     * @param isKeyed
     */
    protected ExecRow getCompactRow(ExecRow candidate, FormatableBitSet accessedCols, boolean isKeyed) {
        //获取候选行的列数`
        int numCandidateCols = candidate.nColumns();
        if (accessedCols == null) {
            compactRow = candidate;
            baseColumnMap = new int[numCandidateCols];
            for (int i = 0; i < baseColumnMap.length; i++) {
                baseColumnMap[i] = i;
            }
        } else {
            int numCols = accessedCols.getNumBitsSet();
            baseColumnMap = new int[numCols];
            if (compactRow == null) {
                if (isKeyed) {
                    compactRow = new IndexRow(numCols);
                } else {
                    compactRow = new ValueRow(numCols);
                }
            }
            int position = 0;
            for (int i = accessedCols.anySetBit(); i != -1; i = accessedCols.anySetBit(i)) {
                //TODO：防止超过候选行
                if (i >= numCandidateCols) {
                    break;
                }
                //获取候选行对应的数据描述,将其设置到当前行
                DataValueDescriptor sc = candidate.getColumn(i + 1);
                if (sc != null) {
                    compactRow.setColumn(position + 1, sc);
                }
                baseColumnMap[position] = i;
                position++;
            }
        }
        return compactRow;
    }

    protected ExecRow setCompactRow(ExecRow candidateRow, ExecRow compactRow) {
        ExecRow retval;
        if (baseColumnMap == null) {
            retval = candidateRow;
        } else {
            retval = compactRow;
            setCompatRow(compactRow, candidateRow.getRowArray());
        }
        return retval;
    }

    /**
     * 将数据描述设置到压缩列
     *
     * @param compactRow 压缩列
     * @param sourceRow  数据描述
     */
    protected final void setCompatRow(ExecRow compactRow, DataValueDescriptor[] sourceRow) {
        DataValueDescriptor[] destRow = compactRow.getRowArray();
        int[] lbcm = baseColumnMap;
        for (int i = 0; i < lbcm.length; i++) {
            destRow[i] = sourceRow[lbcm[i]];
        }
    }

    protected final long getElapsedMillis(long beginTime) {
        return (System.currentTimeMillis() - beginTime);
    }

    public ResultDescription resultDescription;
    public ResultDescription getResultDescription() {
        return resultDescription;
    }

}
