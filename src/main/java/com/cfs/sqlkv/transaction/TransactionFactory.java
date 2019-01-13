package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.io.Formatable;
import com.cfs.sqlkv.io.TransactionTable;
import com.cfs.sqlkv.store.TransactionController;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 15:21
 */
public class TransactionFactory {

    public static final String module = TransactionFactory.class.getName();

    private LockingPolicy[][] lockingPolicies = new LockingPolicy[3][6];

    /**事务表*/
    public TransactionTable transactionTable;

    /**
     * 设置一个新的事务id
     * 首先将当前事务就的ID给移除,并获取其excludeMe属性,该属性默认为true
     * 通过事务句柄创建一个新的事务ID
     * 将新事务ID和全局事务ID设置到当前事务
     * @param oldTransactionId 旧事务ID
     * @param transaction 事务
     * */
    public void setNewTransactionId(TransactionId oldTransactionId, Transaction transaction){
        boolean excludeMe = true;
        /**
         * 如果事务id不为空,
         * */
        if(oldTransactionId!=null){
            excludeMe = remove(oldTransactionId);
        }

        /**增长一个事务id*/
        TransactionId transactionId = new TransactionId(transaction.getAndIncrement());
        //给当前事务设置全局事务ID和事务ID
        transaction.setTransactionId(transaction.getGlobalId(), transactionId);
        if (oldTransactionId != null){
            add(transaction, excludeMe);
        }
    }

    protected boolean remove(TransactionId transactionId) {
        return transactionTable.remove(transactionId);
    }

    protected void add(Transaction transaction, boolean excludeMe) {
        transactionTable.add(transaction, excludeMe);
    }


    /**
     * 获取事务的隔离级别
     * @param mode 锁的模式
     * @param isolation 事务的隔离级别
     * @param stricterOk 是否严格执行
     * */
    public final LockingPolicy getLockingPolicy(int mode,int isolation,boolean stricterOk){
        if (mode == LockingPolicy.MODE_NONE){
            isolation = TransactionController.ISOLATION_NOLOCK;
        }
        LockingPolicy policy = lockingPolicies[mode][isolation];
        if ((policy != null) || (!stricterOk)){
            return policy;
        }
        for (mode++; mode <= LockingPolicy.MODE_CONTAINER; mode++){
            for (int i = isolation; i <= TransactionController.ISOLATION_SERIALIZABLE; i++) {
                policy = lockingPolicies[mode][i];
                if (policy != null){
                    return policy;
                }
            }
        }
        return null;
    }

    public Formatable getTransactionTable() {
        return transactionTable;
    }
}
