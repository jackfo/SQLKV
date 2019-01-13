package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:03
 */
public class GenericExecutionFactory {
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
}
