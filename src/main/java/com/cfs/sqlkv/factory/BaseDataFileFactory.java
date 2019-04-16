package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.io.storage.DirStorageFactory;
import com.cfs.sqlkv.io.storage.StorageFile;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.cache.CacheableFactory;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.data.*;
import com.cfs.sqlkv.store.access.raw.log.LogInstant;
import com.cfs.sqlkv.transaction.Transaction;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 11:53
 */
public class BaseDataFileFactory implements CacheableFactory {



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

    private boolean	readOnly;

    StorageFactory storageFactory;

    private boolean supportsRandomAccess;

    private ContainerKey containerId;

    private boolean stub;

    private StorageFile actionFile;

    private Hashtable<String,StorageFile> postRecoveryRemovedFiles;

    /**数据存放的目录*/
    private final String dataDirectory;


    public BaseDataFileFactory(String dataDirectory){
        this.dataDirectory = dataDirectory;
        storageFactory = new DirStorageFactory(dataDirectory);
    }

    public static int pageCacheSize = 1000;
    private CacheManager pageCache = new CacheManager(this, "PageCache", pageCacheSize / 2, pageCacheSize);


    /**
     * 通过数据工厂添加容器
     * */
    public long addContainer(Transaction transaction, long segmentId, long input_containerid)   {
        //获取容器Id
        long containerId = ((input_containerid != BaseContainerHandle.DEFAULT_ASSIGN_ID) ? input_containerid : getNextId());
        //创建容器标识
        ContainerKey identity = new ContainerKey(segmentId, containerId);

        boolean tmpContainer = (segmentId == BaseContainerHandle.TEMPORARY_SEGMENT);


        //创建容器
        FileContainer container = (FileContainer) containerCache.create(identity,null);

        BaseContainerHandle baseContainerHandle = null;
        Page            firstPage    = null;

        try{
            baseContainerHandle = transaction.openContainer(identity);


            //如果不是临时容器,则进行相关的操作
            if (!tmpContainer){
                ContainerOperation lop = new ContainerOperation(baseContainerHandle, ContainerOperation.CREATE);
                baseContainerHandle.preDirty(true);
            }
            //在创建完容器之后需要添加一个分配页
            firstPage = baseContainerHandle.addPage();
        }finally {
            if (firstPage != null) {
                firstPage.unlatch();
                firstPage = null;
            }
            containerCache.release(container);
            if (baseContainerHandle != null) {
                baseContainerHandle.close();
                baseContainerHandle = null;
            }

        }
        return containerId;
    }

    private long nextContainerId = System.currentTimeMillis();
    private int fileCacheSize = 100;
    private CacheManager containerCache = new CacheManager(this, "ContainerCache", fileCacheSize / 2, fileCacheSize);

    public BaseContainerHandle openContainer(Transaction transaction, ContainerKey containerId)  {
        return openContainer(transaction, containerId, false );
    }

    /**
     * 创建容器句柄
     *    将容器,锁,页面行为,分配行为,事务封装进去
     * */
    private BaseContainerHandle openContainer(Transaction  transaction, ContainerKey  identity, boolean droppedOK)   {
        BaseContainerHandle c;
        FileContainer container = (FileContainer)containerCache.find(identity);
        if (container == null) {
            return null;
        }
        PageActions pageActions  = new DirectActions();
        AllocationActions allocActions = new DirectAllocActions();
        //创建容器句柄
        c = new BaseContainerHandle(getIdentifier(), transaction, pageActions, allocActions, container);
        return c;
    }

    public void flush(LogInstant instant){
        //获取日志工厂进行刷新
        //getLogFactory().flush(instant);
    }
    private UUID identifier;
    public UUID getIdentifier(){
        return identifier;
    }

    synchronized long getNextId() {
        return nextContainerId++;
    }


    public final Object run(int actionCode){
        switch(actionCode) {
            case BOOT_ACTION:
                readOnly = storageFactory.isReadOnlyDatabase();
                supportsRandomAccess = storageFactory.supportsRandomAccess();
                return null;

            case REMOVE_TEMP_DIRECTORY_ACTION:
                StorageFile tempDir = storageFactory.getTempDir();
                if( tempDir != null){
                    tempDir.deleteAll();
                }
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
            default:
                break;

        }
        return null;
    }

    /**
     * 创建的缓存必然是StoredPage
     * */
    @Override
    public Cacheable newCacheable(CacheManager cm) {
        if (cm == pageCache) {
            StoredPage sp = new StoredPage();
            sp.setFactory(this);
            return sp;
        }
        return new RAFContainer(this);
    }

    public CacheManager getPageCache() {
        return pageCache;
    }

    public CacheManager getContainerCache() {
        return containerCache;
    }

    public StorageFile getContainerPath(ContainerKey containerId, boolean stub) {
        return getContainerPath(containerId, stub, GET_CONTAINER_PATH_ACTION);
    }

    private synchronized StorageFile getContainerPath(ContainerKey containerId, boolean stub, int code){

        this.containerId = containerId;
        this.stub = stub;
        try {
            return  (StorageFile)run(code);
        }finally {
            this.containerId = null;
        }
    }

    /**
     *构建文件目录,获取目录下所有的文件,进行遍历,基于基数16做偏移
     * */
    public synchronized long getMaxContainerId(){
        long maxnum = 1;
        StorageFile seg = storageFactory.newStorageFile( "seg0");
        if (seg.exists() && seg.isDirectory()) {
            String[] files = seg.list();
            for (int f = files.length-1; f >= 0 ; f--) {
                try {
                    long fileNumber = Long.parseLong(files[f].substring(1, files[f].length()-4), 16);
                    if (fileNumber > maxnum){
                        maxnum = fileNumber;
                    }
                }
                catch (Throwable t) {
                }
            }
        }
        return maxnum;
    }

    /**
     * 获取交替路径
     *
     * */
    public StorageFile getAlternateContainerPath(ContainerKey containerId, boolean stub) {
        return getContainerPath(containerId,stub,GET_ALTERNATE_CONTAINER_PATH_ACTION);
    }

    /**
     * 是否只读
     * */
    public boolean isReadOnly() {
        return readOnly;
    }

    public void checkpoint()   {
        pageCache.cleanAll();
        containerCache.cleanAll();
    }

}
