package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.compile.sql.ExecPreparedStatement;
import com.cfs.sqlkv.engine.execute.ExecRowBuilder;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.TransactionControl;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 18:48
 */
public abstract class ScanResultSet extends NoPutResultSetImpl {

    final ExecRow candidate;

    final ExecRowBuilder resultRowBuilder;

    protected FormatableBitSet accessedCols;

    private final boolean tableLocked;

    /**
     * 标志隔离级别是否需要更新
     */
    private boolean isolationLevelNeedsUpdate;

    /**
     * 锁的模式
     */
    private int lockMode;

    private final int suppliedLockMode;

    /**
     * 事务的隔离级别
     */
    private int isolationLevel;

    /**
     * @param activation                 激活器
     * @param resultSetNumber            结果集的数目
     * @param resultRowTemplate          identifier of saved object for row template
     * @param lockMode                   lock mode (record or table)
     * @param tableLocked                true if marked as table locked in SYS.SYSTABLES
     * @param isolationLevel             language isolation level for the result set
     * @param colRefItem                 Identifier of saved object for accessedCols,
     *                                   -1 if need to fetch all columns.
     * @param optimizerEstimatedRowCount estimated row count
     * @param optimizerEstimatedCost     estimated cost
     */
    public ScanResultSet(Activation activation, int resultSetNumber, int resultRowTemplate, int lockMode, boolean tableLocked, int isolationLevel, int colRefItem, double optimizerEstimatedRowCount, double optimizerEstimatedCost) {
        super(activation, resultSetNumber, optimizerEstimatedRowCount, optimizerEstimatedCost);
        this.tableLocked = tableLocked;
        suppliedLockMode = lockMode;
        ExecPreparedStatement ps = activation.getPreparedStatement();
        resultRowBuilder = (ExecRowBuilder) ps.getSavedObject(resultRowTemplate);
        candidate = resultRowBuilder.build();
    }

    /**
     * 初始化隔离级别和锁模式
     * 获取语言连接上下文里面的事务隔离级别设置到当前扫描结果
     */
    public void initIsolationLevel() {
        if (isolationLevelNeedsUpdate) {
            int languageLevel = getLanguageConnectionContext().getCurrentIsolationLevel();
            lockMode = getLockMode(languageLevel);
            isolationLevel = translateLanguageIsolationLevel(languageLevel);
            isolationLevelNeedsUpdate = false;
        }
    }

    /**
     * 根据事务的隔离级别获取锁模式
     */
    private int getLockMode(int languageLevel) {
        if (tableLocked || (languageLevel == TransactionControl.SERIALIZABLE_ISOLATION_LEVEL)) {
            return suppliedLockMode;
        } else {
            return TransactionManager.MODE_RECORD;
        }
    }

    public abstract boolean canGetInstantaneousLocks();

    private int translateLanguageIsolationLevel(int languageLevel) {
        switch (languageLevel) {
            case TransactionControl.READ_UNCOMMITTED_ISOLATION_LEVEL:
                return TransactionManager.ISOLATION_READ_UNCOMMITTED;
            case TransactionControl.READ_COMMITTED_ISOLATION_LEVEL:
                if (!canGetInstantaneousLocks()) {
                    return TransactionManager.ISOLATION_READ_COMMITTED;
                }
                return TransactionManager.ISOLATION_READ_COMMITTED_NOHOLDLOCK;
            case TransactionControl.REPEATABLE_READ_ISOLATION_LEVEL:
                return TransactionManager.ISOLATION_REPEATABLE_READ;
            case TransactionControl.SERIALIZABLE_ISOLATION_LEVEL:
                return TransactionManager.ISOLATION_SERIALIZABLE;
            default:
                return 0;
        }
    }

    protected boolean fetchRowLocations = false;
    public String indexName;

    public void setRowLocationsState() {
        fetchRowLocations = ((indexName == null) &&
                (candidate.nColumns() > 0) &&
                (candidate.getColumn(candidate.nColumns()) instanceof TableRowLocation));
    }
}
