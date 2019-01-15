package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.store.TransactionController;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import sun.misc.Unsafe;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 16:25
 */
public class Transaction extends SQLKVObservable {

    private static final long serialVersionUID = 1927816293512124184L;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    private volatile long value;
    static {
        try {
            valueOffset = unsafe.objectFieldOffset(AtomicLong.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    public static final Integer COMMIT = 0;
    public static final Integer ABORT = 1;
    public static final Integer SAVEPOINT_ROLLBACK = 2;
    public static final Integer LOCK_ESCALATE = 3;

    protected final BaseDataFileFactory dataFactory;

    protected final TransactionFactory transactionFactory;

    protected	TransactionContext		xc;

    public Transaction(BaseDataFileFactory dataFactory,TransactionFactory transactionFactory) {
        this.dataFactory = dataFactory;
        this.transactionFactory = transactionFactory;
    }

    /**
     *
     */
    public long addContainer(long segmentId, long containerid, int mode, Properties tableProperties, int temporaryFlag) throws StandardException {
        return dataFactory.addContainer(this, segmentId, containerid, mode, tableProperties, temporaryFlag);
    }


    private UUID identifier;

    public UUID getIdentifier() {
        return identifier;
    }


    protected StandardException observerException;

    public void setObserverException(StandardException se) {
        if (observerException == null) {
            observerException = se;
        }
    }

    public final long getAndIncrement() {
        return unsafe.getAndAddLong(this, valueOffset, 1L);
    }

    /**
     * 通过指定的锁策略打开一个容器
     */
    public BaseContainerHandle openContainer(ContainerKey containerId, LockingPolicy locking, int mode) throws StandardException {
        setActiveState();

        /**
         * 获取锁的策略
         * 默认获取无锁的事务隔离级别
         * */
        if (locking == null){
            locking = transactionFactory.getLockingPolicy(LockingPolicy.MODE_NONE, TransactionController.ISOLATION_NOLOCK, false);
        }

        return dataFactory.openContainer(this, containerId, locking, mode);
    }

    /**
     * 当前事务的状态
     */
    protected volatile int state;

    protected static final int CLOSED = 0;
    protected static final int IDLE = 1;
    protected static final int ACTIVE = 2;
    protected static final int UPDATE = 3;
    protected static final int PREPARED = 4;
    /**
     * 在preComplete()和postComplete()之间设定
     */
    private Integer inComplete = null;

    //
    // When the xact is first created, it is in an IDLE state.  Since the
    // transaction table needs an XactId, one will be made for it.  When this
    // transaction commits, it goes back to the IDLE state.  If we then create
    // a new XactId for it, we will waste one XactId per transaction object
    // because it must first go thru the IDLE state before it gets closed.
    // Therefore, the first XactId is assigned in the constructor and
    // subsequent XactId is assigned in the setActiveState.  However, the first
    // time it goes into setActiveState, we don't want it to create a new
    // XactId when the one that was assigned to it in the constructore is good
    // enough, so we use this justCreate field to indicate to setActiveState
    // whether it needs to make a new XactId (for the next transaction) for
    // not.

    /**
     * 事务首先创造的状态是IDLE
     * 当事务被提交它会会到IDLE状态
     * 如果创建一个新的事务Id,我们会浪费一个事务id
     * */
    private boolean justCreated = true;

    private volatile TransactionId myId;

    /**
     * 设置激活状态
     */
    protected final void setActiveState() throws StandardException {
        Boolean isClosed = (state == CLOSED);
        Boolean isPrepared = !inAbort() && (state == PREPARED);
        /**
         * 如果是关闭状态或者inComplete是终止 当前状态是准备状态
         * */
        if (isClosed || isPrepared) {
            throw new RuntimeException("内部执行错误");
        }

        /**
         *如果状态是IDLE 将其转化为激活状态
         * */
        if (state == IDLE) {
            synchronized (this) {
                state = ACTIVE;
            }
        }

        /**
         * 如果事务不是创建 设置一个新id
         * */
        if (!justCreated) {
            transactionFactory.setNewTransactionId(myId, this);
        }
        justCreated = false;



    }

    public boolean inAbort() {
        return ABORT.equals(inComplete);
    }




    /**
     * 设置事务id和全局事务id
     * */
    void setTransactionId(GlobalTransactionId globalTransactionId, TransactionId transactionId) {
        myGlobalId = globalTransactionId;
        myId = transactionId;
    }

    /**
     * 返回事务标识
     */
    private GlobalTransactionId	myGlobalId;
    public GlobalTransactionId getGlobalId(){
        return myGlobalId;
    }

    /**
     * 返回事务ID
     * */
    public final TransactionId getId() {
        return myId;
    }

    /**
     * 创建一个锁策略
     * @param mode 锁的模式
     * @param isolation 事务的隔离级别
     * @param stricterOk 是否严格执行
     * */
    public final LockingPolicy newLockingPolicy(int mode, int isolation, boolean stricterOk) {
        return transactionFactory.getLockingPolicy(mode, isolation, stricterOk);
    }

    public void dropStreamContainer(long segmentId, long containerId) throws StandardException {
        setActiveState();
        //dataFactory.dropStreamContainer(this, segmentId, containerId);
    }

    public void dropContainer(ContainerKey containerId)throws StandardException {

        setActiveState();

        //dataFactory.dropContainer(this, containerId);
    }

    public Transaction startNestedTopTransaction() throws StandardException {

        return transactionFactory.startNestedTopTransaction(tr.getFactory(), xc.getContextManager());
    }
}
