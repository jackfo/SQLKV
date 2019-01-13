package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:12
 */
abstract class NoRowsResultSetImpln implements ResultSet{


    public final Activation activation;
    public final LanguageConnectionContext lcc;
    public NoRowsResultSetImpln(Activation activation){
        this.activation = activation;
        lcc = activation.getLanguageConnectionContext();
    }

}
