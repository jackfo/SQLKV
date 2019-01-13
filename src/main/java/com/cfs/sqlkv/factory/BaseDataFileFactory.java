package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.StorageFile;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.cache.CacheableFactory;
import com.cfs.sqlkv.store.TransactionController;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.data.*;
import com.cfs.sqlkv.transaction.Transaction;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:53
 */
public class BaseDataFileFactory implements CacheableFactory,PrivilegedExceptionAction {

    public long getMaxContainerId() throws StandardException {
        return(findMaxContainerId());
    }

    private int actionCode;
    private static final int REMOVE_TEMP_DIRECTORY_ACTION           = 2;
    private static final int GET_CONTAINER_PATH_ACTION              = 3;
    private static final int GET_ALTERNATE_CONTAINER_PATH_ACTION    = 4;
    private static final int FIND_MAX_CONTAINER_ID_ACTION           = 5;
    private static final int DELETE_IF_EXISTS_ACTION                = 6;
    private static final int GET_PATH_ACTION                        = 7;
    private static final int POST_RECOVERY_REMOVE_ACTION            = 8;
    private static final int REMOVE_STUBS_ACTION                    = 9;
    private static final int BOOT_ACTION                            = 10;
    private static final int GET_LOCK_ON_DB_ACTION                  = 11;
    private static final int RELEASE_LOCK_ON_DB_ACTION              = 12;
    private static final int RESTORE_DATA_DIRECTORY_ACTION          = 13;
    private static final int GET_CONTAINER_NAMES_ACTION             = 14;

    private synchronized long findMaxContainerId() {
        actionCode = FIND_MAX_CONTAINER_ID_ACTION;
        try {
            return ((Long) AccessController.doPrivileged( this)).longValue();
        } catch (PrivilegedActionException pae) {
            // findMaxContainerId does not throw an exception
            return 0;
        }
    }

    private boolean	readOnly;

    StorageFactory storageFactory;

    private boolean supportsRandomAccess;

    private ContainerKey containerId;

    private boolean stub;

    private StorageFile actionFile;

    private Hashtable<String,StorageFile> postRecoveryRemovedFiles;

    private boolean inCreateNoLog;

    public static int pageCacheSize = 1000;
    private CacheManager pageCache = new CacheManager(this, "PageCache", pageCacheSize / 2, pageCacheSize);


    public long addContainer(
            Transaction     transaction,
            long            segmentId,
            long            input_containerid,
            int             mode,
            Properties tableProperties,
            int             temporaryFlag) throws StandardException {

        long containerId = ((input_containerid != BaseContainerHandle.DEFAULT_ASSIGN_ID) ? input_containerid : getNextId());

        ContainerKey identity = new ContainerKey(segmentId, containerId);
        LockingPolicy   cl = null;

        boolean tmpContainer = (segmentId == BaseContainerHandle.TEMPORARY_SEGMENT);

        BaseContainerHandle ch = null;

        if(!tmpContainer){
            ch = transaction.openContainer(identity, cl, (BaseContainerHandle.MODE_FORUPDATE | BaseContainerHandle.MODE_OPEN_FOR_LOCK_ONLY));
        }
        FileContainer container = (FileContainer) containerCache.create(identity, tableProperties);
        BaseContainerHandle containerHdl = null;
        Page            firstPage    = null;
        try{
            containerHdl = transaction.openContainer(identity, null, (BaseContainerHandle.MODE_FORUPDATE | mode));

            //如果不是临时容器,则进行相关的操作
            if (!tmpContainer){
                ContainerOperation lop = new ContainerOperation(rch, ContainerOperation.CREATE);

                containerHdl.preDirty(true);

                try{
                    transaction.logAndDo(lop);
                    flush(t.getLastLogInstant());
                }finally{
                    rch.preDirty(false);
                }

            }

            firstPage = containerHdl.addPage();
        }finally {
            if (firstPage != null) {
                firstPage.unlatch();
                firstPage = null;
            }
            containerCache.release(container);
            if (containerHdl != null) {
                containerHdl.close();
                containerHdl = null;
            }
            if (!tmpContainer) {

                cl.unlockContainer(t, ch);
            }
        }
        return containerId;
    }

    private long nextContainerId = System.currentTimeMillis();

    private CacheManager containerCache;

    public BaseContainerHandle openContainer(Transaction transaction, ContainerKey containerId, LockingPolicy locking, int mode) throws StandardException{
        return openContainer(transaction, containerId, locking, mode, false );
    }

    /**
     * 创建容器句柄
     *    将容器,锁,页面行为,分配行为,事务封装进去
     * */
    private BaseContainerHandle openContainer(Transaction  transaction, ContainerKey  identity, LockingPolicy locking, int mode, boolean droppedOK) throws StandardException {
        boolean waitForLock = ((mode & BaseContainerHandle.MODE_LOCK_NOWAIT) == 0);
        if ((mode & BaseContainerHandle.MODE_OPEN_FOR_LOCK_ONLY) != 0) {
            BaseContainerHandle lockOnlyHandle = new BaseContainerHandle(getIdentifier(), transaction, identity, locking, mode);
            if (lockOnlyHandle.useContainer(true, waitForLock)) {
                return lockOnlyHandle;
            } else {
                return null;
            }
        }

        BaseContainerHandle c;
        FileContainer container = (FileContainer)containerCache.find(identity);
        if (container == null) {
            return null;
        }
        if (identity.getSegmentId() == BaseContainerHandle.TEMPORARY_SEGMENT) {
            if ((mode & BaseContainerHandle.MODE_TEMP_IS_KEPT) == BaseContainerHandle.MODE_TEMP_IS_KEPT) {
                // 如果模式保存,不做截断
                mode |= BaseContainerHandle.MODE_UNLOGGED;
            } else {
                mode |= (BaseContainerHandle.MODE_UNLOGGED | BaseContainerHandle.MODE_TRUNCATE_ON_ROLLBACK);
            }

            //构建相应的锁策略
            locking = transaction.newLockingPolicy(LockingPolicy.MODE_NONE, TransactionController.ISOLATION_NOLOCK, true);
        }else{
            if (inCreateNoLog){

            }else {

            }
        }

        PageActions pageActions  = null;
        AllocationActions allocActions = null;
        if ((mode & BaseContainerHandle.MODE_FORUPDATE) == BaseContainerHandle.MODE_FORUPDATE){
            if ((mode & BaseContainerHandle.MODE_UNLOGGED) == 0){
                //pageActions  = getLoggablePageActions();
                //allocActions = getLoggableAllocationActions();
            }else{
                pageActions  = new DirectActions();
                allocActions = new DirectAllocActions();
            }
        }

        //创建容器句柄
        c = new BaseContainerHandle(getIdentifier(), transaction, pageActions, allocActions, locking, container, mode);

        //检测当前容器是否可以使用
        try{
            if (!c.useContainer(droppedOK, waitForLock)) {
                containerCache.release(container);
                return null;
            }
        }catch (StandardException se) {
            containerCache.release(container);
            throw se;
        }
        return c;
    }

    private UUID identifier;
    public UUID getIdentifier(){
        return identifier;
    }

    synchronized long getNextId() {
        return nextContainerId++;
    }

    @Override
    public final Object run() throws IOException, StandardException {
        switch(actionCode) {
            case BOOT_ACTION:
                readOnly = storageFactory.isReadOnlyDatabase();
                supportsRandomAccess = storageFactory.supportsRandomAccess();
                return null;

            case REMOVE_TEMP_DIRECTORY_ACTION:
                StorageFile tempDir = storageFactory.getTempDir();
                if( tempDir != null)
                    tempDir.deleteAll();
                return null;

            case GET_CONTAINER_PATH_ACTION:
            case GET_ALTERNATE_CONTAINER_PATH_ACTION:
            {
                StringBuffer sb = new StringBuffer("seg");
                sb.append(containerId.getSegmentId());
                sb.append(storageFactory.getSeparator());
                if( actionCode == GET_CONTAINER_PATH_ACTION)
                {
                    sb.append(stub ? 'd' : 'c');
                    sb.append(Long.toHexString(containerId.getContainerId()));
                    sb.append(".skv");
                }
                else
                {
                    sb.append(stub ? 'D' : 'C');
                    sb.append(Long.toHexString(containerId.getContainerId()));
                    sb.append(".SKV");
                }
                return storageFactory.newStorageFile(sb.toString());
            } // end of cases GET_CONTAINER_PATH_ACTION & GET_ALTERNATE_CONTAINER_PATH_ACTION

            case REMOVE_STUBS_ACTION:
            {
                char separator = storageFactory.getSeparator();
                StorageFile root = storageFactory.newStorageFile( null);

                // get all the non-temporary data segment, they start with "seg"
                String[] segs = root.list();
                for (int s = segs.length-1; s >= 0; s--)
                {
                    if (segs[s].startsWith("seg"))
                    {
                        StorageFile seg = storageFactory.newStorageFile(root, segs[s]);

                        if (seg.exists() && seg.isDirectory())
                        {
                            String[] files = seg.list();
                            for (int f = files.length-1; f >= 0 ; f--)
                            {
                                // stub
                                if (files[f].startsWith("D") ||
                                        files[f].startsWith("d"))
                                {
                                    StorageFile stub =
                                            storageFactory.newStorageFile(
                                                    root, segs[s] + separator + files[f]);

                                    boolean delete_status = stub.delete();
                                }
                            }
                        }
                    }
                }
                break;
            }

            case FIND_MAX_CONTAINER_ID_ACTION:
            {
                long maxnum = 1;
                StorageFile seg = storageFactory.newStorageFile( "seg0");

                if (seg.exists() && seg.isDirectory())
                {
                    // create an array with names of all files in seg0
                    String[] files = seg.list();

                    // loop through array looking for maximum containerid.
                    for (int f = files.length-1; f >= 0 ; f--)
                    {
                        try
                        {
                            long fileNumber =
                                    Long.parseLong(
                                            files[f].substring(
                                                    1, (files[f].length() -4)), 16);

                            if (fileNumber > maxnum)
                                maxnum = fileNumber;
                        }
                        catch (Throwable t)
                        {
                            // ignore errors from parse, it just means that someone
                            // put a file in seg0 that we didn't expect.  Continue
                            // with the next one.
                        }
                    }
                }
                return maxnum;
            } // end of case FIND_MAX_CONTAINER_ID_ACTION

            case DELETE_IF_EXISTS_ACTION:
            {
                boolean ret = actionFile.exists() && actionFile.delete();
                actionFile = null;
                return ret ? this : null;
            } // end of case DELETE_IF_EXISTS_ACTION

            case GET_PATH_ACTION:
            {
                String path = actionFile.getPath();
                actionFile = null;
                return path;
            } // end of case GET_PATH_ACTION

            case POST_RECOVERY_REMOVE_ACTION:
            {
                for (Enumeration<StorageFile> e = postRecoveryRemovedFiles.elements();
                     e.hasMoreElements(); )
                {
                    StorageFile f = e.nextElement();
                    if (f.exists()) {
                        boolean delete_status = f.delete();
                    }
                }
                return null;
            }

            case GET_LOCK_ON_DB_ACTION:
                //privGetJBMSLockOnDB();
                return null;

            case RELEASE_LOCK_ON_DB_ACTION:
                //privReleaseJBMSLockOnDB();
                return null;

            case RESTORE_DATA_DIRECTORY_ACTION:
                //privRestoreDataDirectory();
                return null;
            case GET_CONTAINER_NAMES_ACTION:
            {
                StorageFile seg = storageFactory.newStorageFile( "seg0");
                if (seg.exists() && seg.isDirectory())
                {
                    // return the  names of all files in seg0
                    return seg.list();
                }
                return null;
            }

        }
        return null;
    }

    @Override
    public Cacheable newCacheable(CacheManager cm) {
        return null;
    }

    public CacheManager getPageCache() {
        return pageCache;
    }

    public CacheManager getContainerCache() {
        return containerCache;
    }
}
