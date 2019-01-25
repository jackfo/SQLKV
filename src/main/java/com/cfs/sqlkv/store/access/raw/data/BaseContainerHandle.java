package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.service.monitor.SQLKVObservable;
import com.cfs.sqlkv.service.monitor.SQLKVObserver;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.transaction.Transaction;

import java.util.Observer;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:45
 */
public class BaseContainerHandle extends SQLKVObservable implements SQLKVObserver {

    public static final int TEMPORARY_SEGMENT = -1;

    public static final int DEFAULT_ASSIGN_ID = 0;

    /**不合法的页号*/
    public static final long INVALID_PAGE_NUMBER = -1;

    public static final int MODE_DEFAULT               = 0x00000000;
    public static final int MODE_UNLOGGED              = 0x00000001;
    public static final int MODE_CREATE_UNLOGGED       = 0x00000002;
    public static final int MODE_FORUPDATE             = 0x00000004;
    public static final int MODE_READONLY	           = 0x00000008;
    public static final int MODE_TRUNCATE_ON_COMMIT    = 0x00000010;
    public static final int MODE_DROP_ON_COMMIT        = 0x00000020;
    public static final int MODE_OPEN_FOR_LOCK_ONLY    = 0x00000040;
    /**非等待锁*/
    public static final int MODE_LOCK_NOWAIT           = 0x00000080;
    public static final int MODE_TRUNCATE_ON_ROLLBACK  = 0x00000100;
    public static final int MODE_FLUSH_ON_COMMIT       = 0x00000200;
    public static final int MODE_NO_ACTIONS_ON_COMMIT  = 0x00000400;
    public static final int MODE_TEMP_IS_KEPT		   = 0x00000800;

    public static final int MODE_USE_UPDATE_LOCKS	   = 0x00001000;
    public static final int MODE_SECONDARY_LOCKED      = 0x00002000;
    public static final int MODE_BASEROW_INSERT_LOCKED = 0x00004000;

    /**第一个可利用页*/
    public static final long FIRST_PAGE_NUMBER = 1;

    /**
     Container identifier
     <BR> MT - Immutable
     */
    private ContainerKey		identity;

    /**
     Is this ContainerHandle active.

     <BR> MT - Mutable : scoped
     */
    private boolean				        active;

    /**
     The actual container we are accessing. Only valid when active is true.

     <BR> MT - Mutable : scoped
     */
    protected BaseContainer		        container;
    private LockingPolicy               locking;
    private Transaction                 transaction;
    private	boolean		                forUpdate;
    private int                         mode;

    public BaseContainerHandle(UUID rawStoreId, Transaction transaction, ContainerKey identity, LockingPolicy locking, int mode) {
        this.identity = identity;
        this.transaction = transaction;
        this.locking = locking;
        this.mode = mode;
        this.forUpdate = (mode & MODE_FORUPDATE) == MODE_FORUPDATE;
    }

    private PageActions	actionsSet;
    private AllocationActions allocActionsSet;
    public BaseContainerHandle(UUID rawStoreId, Transaction transaction,PageActions actionsSet, AllocationActions allocActionsSet, LockingPolicy locking, BaseContainer container, int mode) {
        this(rawStoreId, transaction, (ContainerKey) container.getIdentity(), locking, mode);
        this.actionsSet      = actionsSet;
        this.allocActionsSet = allocActionsSet;
        this.container       = container;
    }


    /**
     * 1.获取相应的锁策略,对相应的容器进行加锁
     * */
    public boolean useContainer(boolean droppedOK, boolean waitForLock) throws StandardException {
        boolean gotLock = getLockingPolicy().lockContainer(getTransaction(), this, waitForLock, forUpdate);

        if (gotLock == false) {
            container = null;
            throw new RuntimeException("获取锁超时");
        }

        if ((mode & BaseContainerHandle.MODE_OPEN_FOR_LOCK_ONLY) == 0){
            if (!container.use(this, forUpdate, droppedOK)){
                //解锁容器
                getLockingPolicy().unlockContainer(transaction, this);
                container = null;
                return false;
            }
            active = true;
        }else {
            // 仅仅在执行行级锁的时候观察事务
            if (getLockingPolicy().getMode() != LockingPolicy.MODE_RECORD){
                return true;
            }

        }
        transaction.addObserver(this);
        //添加指定对象,确定事务在提交/回滚
        if ((mode & (BaseContainerHandle.MODE_READONLY | BaseContainerHandle.MODE_NO_ACTIONS_ON_COMMIT)) == 0){
            if ((mode & MODE_TRUNCATE_ON_COMMIT) == MODE_TRUNCATE_ON_COMMIT) {
                transaction.addObserver(new TruncateOnCommit(identity, true));
            }else if ((mode & MODE_TRUNCATE_ON_ROLLBACK) == MODE_TRUNCATE_ON_ROLLBACK){
                transaction.addObserver(new TruncateOnCommit(identity, false));
            }
            if ((mode & MODE_DROP_ON_COMMIT) == MODE_DROP_ON_COMMIT) {
                transaction.addObserver(new DropOnCommit(identity));
            }
            if ((mode & MODE_FLUSH_ON_COMMIT) == MODE_FLUSH_ON_COMMIT) {
                transaction.addObserver(new SyncOnCommit(identity));
            }
        }
        return true;
    }


    public LockingPolicy getLockingPolicy(){
        return locking;
    }

    public final Transaction getTransaction() {
        return transaction;
    }

    /**
     * 基于容器本身在在事务发生变化的时候做更新
     * */
    @Override
    public void update(SQLKVObservable observable, Object extraInfo) {
        if(transaction==null){
            return ;
        }

        //根据参数的类型决定做何种触发
        if (extraInfo.equals(Transaction.COMMIT) || extraInfo.equals(Transaction.ABORT)  || extraInfo.equals(identity)) {
            //关闭当前容器
            close();
            return;
        }

        if (extraInfo.equals(Transaction.SAVEPOINT_ROLLBACK)){
            informObservers();
            return;
        }
        if (extraInfo.equals(Transaction.LOCK_ESCALATE)){
            if (getLockingPolicy().getMode() != LockingPolicy.MODE_RECORD){
                return;
            }
            try {
                getLockingPolicy().lockContainer(getTransaction(), this, false, forUpdate);
            } catch (StandardException se) {
                transaction.setObserverException(se);
            }
        }

    }


    /**
     * 关闭当前容器并解锁事务
     * */
    public synchronized void close(){

        if(transaction==null){
            return;
        }

        informObservers();

        active = false;

        /**对事务进行解锁*/
        getLockingPolicy().unlockContainer(transaction, this);

        if (container != null) {
            container.letGo(this);
            container = null;
        }

        /**
         *
         * */
        transaction.deleteObserver(this);
        transaction = null;
    }

    /**
     * 唤醒所有的观察者进行更新
     * */
    protected void informObservers() {
        if (countObservers() != 0) {
            setChanged();
            notifyObservers();
        }
    }

    public Page getPage(long pageNumber) throws StandardException {
        return container.getPage(this, pageNumber, true);
    }

    public void setEstimatedRowCount(long count, int flag) throws StandardException {
        container.setEstimatedRowCount(count, flag);
    }

    protected boolean			preDirty;
    public void preDirty(boolean preDirtyOn){
        synchronized (this){
            if(preDirtyOn){
                preDirty = true;
            }else{
                preDirty = false;
                notifyAll();
            }
        }
    }

    public Page addPage() throws StandardException{
        Page page = container.addPage(this, false);
        return page;
    }

    public int getMode() {
        return mode;
    }

    /**
     * 通过容器句柄获取分页行为
     * */
    public PageActions getActionSet() {
        return actionsSet;
    }
}
