package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.RAMTransactionContext;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.store.RAMTransaction;
import com.cfs.sqlkv.store.TransactionController;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 16:18
 */
public class AccessManager {

    public static final String RAMXACT_CONTEXT_ID = "RAMTransactionContext";

    public static final String USER_TRANS_NAME = "UserTransaction";



    public TransactionController getTransaction(ContextManager cm) throws StandardException{
        return getAndNameTransaction(cm, USER_TRANS_NAME);
    }


    public TransactionController getAndNameTransaction(ContextManager cm, String transName)throws StandardException{
        if(cm==null){
            return null;
        }
        RAMTransactionContext rtc = (RAMTransactionContext) cm.getContext(RAMXACT_CONTEXT_ID);
        if(rtc==null){
            RAMTransaction rt = new RAMTransaction(this, null, null);
            rtc = new RAMTransactionContext(cm, RAMXACT_CONTEXT_ID, rt, false);
            TransactionController tc = rtc.getTransaction();
            tc.commit();
            return tc;
        }
        return rtc.getTransaction();
    }


}
