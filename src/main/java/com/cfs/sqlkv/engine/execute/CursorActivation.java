package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.compile.result.CursorResultSet;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-07 13:08
 */
public interface CursorActivation extends Activation {

    public CursorResultSet getTargetResultSet();

    public CursorResultSet getCursorResultSet();
}
