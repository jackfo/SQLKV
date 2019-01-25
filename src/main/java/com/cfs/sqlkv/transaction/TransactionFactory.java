package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.io.Formatable;
import com.cfs.sqlkv.io.TransactionTable;
import com.cfs.sqlkv.service.locks.LockFactory;
import com.cfs.sqlkv.store.TransactionController;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.RawStoreFactory;
import com.cfs.sqlkv.store.access.raw.TransactionContext;
import com.cfs.sqlkv.store.access.raw.log.LogFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 15:21
 */
public class TransactionFactory {

    public static final String module = TransactionFactory.class.getName();

    private UUIDFactory           uuidFactory;
    private ContextService        contextFactory;
    private LockFactory           lockFactory;
    private LogFactory            logFactory;
    private BaseDataFileFactory   dataFactory;
    private DataValueFactory      dataValueFactory;
    private RawStoreFactory       rawStoreFactory;

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


    protected static final String NTT_CONTEXT_ID = "NestedTransaction";
    public Transaction startNestedTopTransaction(RawStoreFactory rsf, ContextManager cm) throws StandardException {
        Transaction transaction = new Transaction(this, null, logFactory, dataFactory, dataValueFactory, false, null, false);
        transaction.setPostComplete();
        pushTransactionContext(cm, NTT_CONTEXT_ID, transaction, true , rsf, true);
        return transaction;
    }



    /**
     * 给事务新建事务上下文,在实例化过程会将其指向到事务
     * 之后将事务添加到事务表
     *
     * */
    protected void pushTransactionContext(ContextManager cm, String contextName, Transaction transaction,
                                          boolean abortAll, RawStoreFactory rsf, boolean excludeMe){
        //构造事务上下文
        new TransactionContext(cm, contextName, transaction, abortAll, rsf);
        add(transaction, excludeMe);
    }

    public LockFactory getLockFactory() {
        return lockFactory;
    }

}
