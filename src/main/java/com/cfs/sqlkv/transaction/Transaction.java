package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.DataValueFactoryImpl;
import com.cfs.sqlkv.service.locks.LockFactory;
import com.cfs.sqlkv.service.locks.LockSpace;
import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.TransactionContext;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.log.LogFactory;
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
    private volatile AtomicLong value = new AtomicLong();
    protected  DataValueFactory  dataValueFactory;

    public static final Integer COMMIT = 0;
    public static final Integer ABORT = 1;
    public static final Integer SAVEPOINT_ROLLBACK = 2;
    public static final Integer LOCK_ESCALATE = 3;

    private final BaseDataFileFactory dataFactory ;

    private final LogFactory logFactory;

    protected final TransactionFactory transactionFactory;

    public TransactionContext transactionContext;

    private final LockSpace lockSpace;

    public Transaction(TransactionFactory transactionFactory, Transaction transaction,
                       LogFactory logFactory, BaseDataFileFactory dataFactory,
                       DataValueFactory dataValueFactory, boolean readOnly,
                       LockSpace lockSpace, boolean flush_log_on_xact_end){
        super();
        this.transactionFactory = transactionFactory;
        this.logFactory = logFactory;
        this.dataFactory = dataFactory;
        this.lockSpace = lockSpace;
        this.dataValueFactory       = dataValueFactory;
    }

    /**
     *
     */
    public long addContainer(long segmentId, long containerid)   {
        return dataFactory.addContainer(this, segmentId, containerid);
    }


    private UUID identifier;

    public UUID getIdentifier() {
        return identifier;
    }


    public final long getAndIncrement() {
        return value.getAndIncrement();
    }

    /**
     * 通过指定的锁策略打开一个容器
     */
    public BaseContainerHandle openContainer(ContainerKey containerId)   {
        return dataFactory.openContainer(this, containerId);
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
    protected final void setActiveState()   {
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

    public void dropStreamContainer(long segmentId, long containerId)   {
        setActiveState();
        //dataFactory.dropStreamContainer(this, segmentId, containerId);
    }

    public void dropContainer(ContainerKey containerId)  {

        setActiveState();

        //dataFactory.dropContainer(this, containerId);
    }

    /**
     * 将事务工厂和上下文管理器传入
     * 开始一个内嵌事务
     * */
    public Transaction startNestedTopTransaction()   {
        return transactionFactory.startNestedTopTransaction(transactionContext.getFactory(), transactionContext.getContextManager());
    }


    private boolean postCompleteMode;
    public void setPostComplete() {
        postCompleteMode = true;
    }

    /**
     * 获取锁空间
     * */
    public final LockSpace getLockSpace(){
        return this.lockSpace;
    }

    public final LockFactory getLockFactory() {
        return transactionFactory.getLockFactory();
    }

    public void close()  {

    }

    public DataValueFactory getDataValueFactory(){
        if(dataValueFactory ==null){
            this.dataValueFactory = new DataValueFactoryImpl();
        }
        return dataValueFactory;
    }

    private String transName;
    public void setTransName(String name) {
        transName = name;
    }
}
