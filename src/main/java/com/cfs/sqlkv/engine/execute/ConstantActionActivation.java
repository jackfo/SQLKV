package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.compile.result.ResultSet;

import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.sql.activation.BaseActivation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 19:57
 */
public final class ConstantActionActivation extends BaseActivation {

    public ResultSet createResultSet()   {
        return getResultSetFactory().getDDLResultSet(this);
    }


    public void postConstructor(){}
}
