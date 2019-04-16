package com.cfs.sqlkv.sql.activation;

import com.cfs.sqlkv.compile.result.NoPutResultSetImpl;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.factory.GenericResultSetFactory;
import com.cfs.sqlkv.factory.ResultSetFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-07 16:02
 */
public abstract class CursorActivation extends BaseActivation {

    public static final String module = CursorActivation.class.getName();
    public void setCursorName(String cursorName) {
        if (!isClosed())
            super.setCursorName(cursorName);
    }

    public boolean isCursorActivation()
    {
        return true;
    }

    @Override
    public  ResultSet createResultSet(){
        ResultSetFactory genericResultSetFactory = this.getResultSetFactory();
        return new NoPutResultSetImpl(this,2,0,0);
    }


    @Override
    public void postConstructor() {

    }
}
