package com.cfs.sqlkv.store;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.row.RowLocationRetRowSource;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 14:52
 */
public interface TransactionController {

    public static final int ISOLATION_NOLOCK = 0;

    public static final int ISOLATION_READ_UNCOMMITTED = 1;

    /**
     * No lost updates, no dirty reads, only committed data is returned.
     * Writes only visible when committed.  Exclusive transaction
     * length locks are set on data that is written, short term locks (
     * possibly instantaneous duration locks) are set
     * on data that is read.
     **/
    public static final int ISOLATION_READ_COMMITTED = 2;

    /**
     * No lost updates, no dirty reads, only committed data is returned.
     * Writes only visible when committed.  Exclusive transaction
     * length locks are set on data that is written, short term locks (
     * possibly instantaneous duration locks) are set
     * on data that is read.  Read locks are requested for "zero" duration,
     * thus upon return from access no read row lock is held.
     **/
    public static final int ISOLATION_READ_COMMITTED_NOHOLDLOCK = 3;

    /**可重复读*/
    public static final int ISOLATION_REPEATABLE_READ = 4;

    /**串行化*/
    public static final int ISOLATION_SERIALIZABLE = 5;

    public void commit() throws StandardException;

    /**初始化flag*/
    static final byte IS_DEFAULT	=	(byte) 0x00;
    /**conglom是临时的*/
    static final byte IS_TEMPORARY	=	(byte) 0x01;
    static final byte IS_KEPT		=	(byte) 0x02;

    /**
     *
     * */
    long createAndLoadConglomerate(
            String                  implementation,
            DataValueDescriptor[]   template,
            ColumnOrdering[]		columnOrder,
            int[]                   collationIds,
            Properties properties,
            int                     temporaryFlag,
            RowLocationRetRowSource rowSource,
            long[]                  rowCount)
            throws StandardException;


    long createConglomerate(
            String                  implementation,
            DataValueDescriptor[]   template,
            ColumnOrdering[]        columnOrder,
            int[]                   collationIds,
            Properties              properties,
            int                     temporaryFlag)
            throws StandardException;
}
