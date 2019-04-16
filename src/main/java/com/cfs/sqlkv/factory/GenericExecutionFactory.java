package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.engine.execute.GenericQualifier;
import com.cfs.sqlkv.row.ExecIndexRow;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Qualifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:03
 */
public class GenericExecutionFactory implements ExecutionFactory{
    private GenericResultSetFactory rsFactory;

    public static final String module = GenericExecutionFactory.class.getName();

    public GenericResultSetFactory getResultSetFactory(){
        if (rsFactory == null) {
            rsFactory = new GenericResultSetFactory();
        }
        return rsFactory;
    }

    public ExecRow getValueRow(int numColumns) {
        return new ValueRow(numColumns);
    }

    public ExecIndexRow getIndexableRow(int numColumns) {
        return new IndexRow(numColumns);
    }

    @Override
    public Qualifier getQualifier(int columnId, int operator, GeneratedMethod orderableGetter, Activation activation, boolean orderedNulls) {
        return new GenericQualifier(columnId, operator, orderableGetter, activation, orderedNulls);
    }
}
