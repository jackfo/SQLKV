package com.cfs.sqlkv.context;

import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.result.NoPutResultSet;

import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 18:53
 */
public interface StatementContext extends Context{

    public SQLSessionContext getSQLSessionContext();

    public void setActivation(Activation a);

    public boolean	onStack();

    public void setTopResultSet(ResultSet topResultSet, NoPutResultSet[] subqueryTrackingArray)  ;

    public NoPutResultSet[] getSubqueryTrackingArray()  ;

    public void pushMe();
}
