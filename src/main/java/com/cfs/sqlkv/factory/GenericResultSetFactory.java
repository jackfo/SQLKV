package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.common.conn.Authorizer;
import com.cfs.sqlkv.compile.result.MiscResultSet;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:07
 */
public class GenericResultSetFactory {

    public GenericResultSetFactory() {
    }

    public ResultSet getDDLResultSet(Activation activation) throws StandardException {
        return getMiscResultSet( activation);
    }

    public ResultSet getMiscResultSet(Activation activation) throws StandardException {
        return new MiscResultSet(activation);
    }
}
