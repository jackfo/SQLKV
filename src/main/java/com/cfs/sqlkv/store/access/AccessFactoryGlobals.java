package com.cfs.sqlkv.store.access;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-13 15:11
 */
public class AccessFactoryGlobals {

    public static final int BTREE_OVERFLOW_THRESHOLD = 50;

    public static final String RAMXACT_CONTEXT_ID = "RAMTransactionContext";

    /**对溢出极限值*/
    public static final int HEAP_OVERFLOW_THRESHOLD  = 100;

    public static final String CFG_CONGLOMDIR_CACHE = "ConglomerateDirectoryCache";
}
