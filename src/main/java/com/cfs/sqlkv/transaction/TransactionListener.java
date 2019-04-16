package com.cfs.sqlkv.transaction;



/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 20:45
 */
public interface TransactionListener {

    public boolean preCommit()  ;

    public void preRollback()  ;
}
