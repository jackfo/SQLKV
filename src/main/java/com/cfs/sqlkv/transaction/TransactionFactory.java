package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.common.PersistentService;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.common.context.ContextService;

import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.io.TransactionTable;
import com.cfs.sqlkv.row.RawStoreFactory;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.locks.LockFactory;
import com.cfs.sqlkv.service.locks.LockSpace;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.TransactionContext;
import com.cfs.sqlkv.store.access.raw.log.LogFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 15:21
 */
public class TransactionFactory {

    protected static final String USER_CONTEXT_ID = "UserTransaction";

    public static final String module = TransactionFactory.class.getName();

    private UUIDFactory           uuidFactory;
    private ContextService        contextFactory;
    private LockFactory           lockFactory;
    private LogFactory            logFactory;
    private DataValueFactory      dataValueFactory;
    private RawStoreFactory       rawStoreFactory;
    //行存储实现,需要通过行存储来进行获取
    private BaseDataFileFactory   dataFactory;
    private LockingPolicy[][] lockingPolicies = new LockingPolicy[3][6];



    /**事务表*/
    public TransactionTable transactionTable = new TransactionTable();

    public TransactionFactory(RawStoreFactory rawStoreFactory){
        this.rawStoreFactory = rawStoreFactory;
        dataFactory = rawStoreFactory.getDataFactory();
    }

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
        //transactionTable.add(transaction, excludeMe);
    }


    /**
     * 获取事务的隔离级别
     * @param mode 锁的模式
     * @param isolation 事务的隔离级别
     * @param stricterOk 是否严格执行
     * */
    public final LockingPolicy getLockingPolicy(int mode,int isolation,boolean stricterOk){
        if (mode == LockingPolicy.MODE_NONE){
            isolation = TransactionManager.ISOLATION_NOLOCK;
        }
        LockingPolicy policy = lockingPolicies[mode][isolation];
        if ((policy != null) || (!stricterOk)){
            return policy;
        }
        for (mode++; mode <= LockingPolicy.MODE_CONTAINER; mode++){
            for (int i = isolation; i <= TransactionManager.ISOLATION_SERIALIZABLE; i++) {
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
    public Transaction startNestedTopTransaction(RawStoreFactory rawStore, ContextManager cm)   {
        Transaction transaction = new Transaction(this, null, logFactory, dataFactory, dataValueFactory, false, null, false);
        transaction.setPostComplete();
        pushTransactionContext(cm, NTT_CONTEXT_ID, transaction, true , rawStore, true);
        return transaction;
    }



    /**
     * 给事务新建事务上下文,在实例化过程会将其指向到事务
     * 之后将事务添加到事务表
     *
     * */
    protected void pushTransactionContext(ContextManager cm, String contextName, Transaction transaction,
                                          boolean abortAll, RawStoreFactory rawStore, boolean excludeMe){
        //构造事务上下文
        new TransactionContext(cm, contextName, transaction, abortAll, rawStore);
        add(transaction, excludeMe);
    }

    public LockFactory getLockFactory() {
        return lockFactory;
    }

    public Transaction findUserTransaction(RawStoreFactory rawStore, ContextManager contextMgr, String transName)   {
        TransactionContext transactionContext = (TransactionContext)contextMgr.getContext(USER_CONTEXT_ID);
        if (transactionContext == null){
            return startTransaction(rawStore, contextMgr, transName);
        } else{
            return transactionContext.getTransaction();
        }
    }

    public Transaction startTransaction(RawStoreFactory rawStore, ContextManager cm, String transName)   {
        return(
                startCommonTransaction(
                        rawStore,
                        null,
                        cm,
                        false,              // user xact always read/write
                        null,
                        USER_CONTEXT_ID,
                        transName,
                        true,               // user xact always excluded during quiesce
                        true));             // user xact default flush on xact end
    }
    private Transaction startCommonTransaction(RawStoreFactory rawStore, Transaction parentTransaction, ContextManager cm, boolean readOnly, LockSpace lockSpace,
                                               String transaction_context_id, String transName, boolean excludeMe, boolean flush_log_on_xact_end){
        Transaction transaction = new Transaction(this, parentTransaction, logFactory, dataFactory, dataValueFactory, readOnly, null, flush_log_on_xact_end);
        transaction.setTransName(transName);
        pushTransactionContext(cm, transaction_context_id, transaction, false, rawStore, false );
        return transaction;
    }
}
