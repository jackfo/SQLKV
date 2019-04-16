package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.row.ExecIndexRow;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Qualifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 13:45
 */
public interface ExecutionFactory {

    public static final String MODULE = ExecutionFactory.class.getName();
    public GenericResultSetFactory getResultSetFactory();
    public ExecRow getValueRow(int numColumns);
    public ExecIndexRow getIndexableRow(int numColumns);

    public Qualifier getQualifier(int columnId,
                           int operator,
                           GeneratedMethod orderableGetter,
                           Activation activation,
                           boolean orderedNulls);
}
