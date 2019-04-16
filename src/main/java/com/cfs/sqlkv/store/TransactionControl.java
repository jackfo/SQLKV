package com.cfs.sqlkv.store;

import com.cfs.sqlkv.transaction.TransactionListener;

import java.util.ArrayList;

/**
 * @author zhengxiaokang
 * @Description 提供
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 20:42
 */
public final class TransactionControl {
    public static final int UNSPECIFIED_ISOLATION_LEVEL = 0;
    public static final int READ_UNCOMMITTED_ISOLATION_LEVEL = 1;
    public static final int READ_COMMITTED_ISOLATION_LEVEL = 2;
    public static final int REPEATABLE_READ_ISOLATION_LEVEL = 3;
    public static final int SERIALIZABLE_ISOLATION_LEVEL = 4;

    /**
     * 转化事务隔离级别和JDBC对应
     * */
    private static final int[] CS_TO_JDBC_ISOLATION_LEVEL_MAP = {
            java.sql.Connection.TRANSACTION_NONE,
            java.sql.Connection.TRANSACTION_READ_UNCOMMITTED,
            java.sql.Connection.TRANSACTION_READ_COMMITTED,
            java.sql.Connection.TRANSACTION_REPEATABLE_READ,
            java.sql.Connection.TRANSACTION_SERIALIZABLE
    };

    private final ArrayList<TransactionListener> listeners;

    public TransactionControl()
    {
        listeners = new ArrayList<TransactionListener>();
    }

    public void addListener(TransactionListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(TransactionListener listener)
    {
        listeners.remove(listener);
    }
}
