package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.catalog.DataDictionaryImpl;
import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.common.PersistentService;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.row.RawStoreFactory;
import com.cfs.sqlkv.service.io.FileUtil;
import com.cfs.sqlkv.transaction.AccessManager;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description TransactionResourceImpl是数据库连接相关实例
 * 通常意义上,它是事务上下文,同时是加锁对象保证只有一个线程和上下文在访问底层事务
 * <p>
 * TransactionResourceImpl同时关系以下的情况:
 * 将ContextManager从ContextService中取出和添加
 * 事务划分,针对commit/abort/prepare/close相关类型做对应的路由
 * <p>
 * 唯一可以访问事务连接是root connection,所有其他嵌套连接访问事务最终都是通过根连接的渠道
 * root connection的种类:
 * EmbedConnection
 * DetachableConnection
 * XATransaction
 * 一个嵌套连接可能是一个ProxyConnection
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 21:37
 */
public final class TransactionResourceImpl {

    private InternalDriver driver;

    private String url;

    protected ContextManager cm;
    protected ContextService csf;

    protected Database database;

    protected LanguageConnectionContext lcc;

    private String dbname;

    public TransactionResourceImpl(InternalDriver driver, String url, Properties info, Database database) {
        this.driver = driver;
        this.url = url;
        dbname = InternalDriver.getDatabaseName(url, info);
        //通过驱动获取上下文服务
        csf = driver.getContextService();
        if (cm == null) {
            cm = csf.newContextManager();
            csf.setCurrentContextManager(cm);
        }
        this.database = database;
        String serviceDirectory = System.getProperty("user.dir") + "/" + dbname;
        System.getProperties().put(PersistentService.ROOT, serviceDirectory);

        Boolean isExists = FileUtil.isExists(serviceDirectory);

        boolean create = !isExists;
        //通过检测路径是否存在判断数据库是否需要创建
        database.init(cm, create);
        //将当前连接与数据库进行对应,这个时候需要修改一些系统属性
        lcc = database.setupConnection(cm, null, null, dbname);
    }

    public LanguageConnectionContext getLcc() {
        return lcc;
    }


    public void setDatabase(Database db) {
        database = db;
    }

    /**
     * 根连接的驱动是网络驱动
     */
    InternalDriver getDriver() {
        return driver;
    }

    public ContextManager getCm() {
        return cm;
    }

    public final void setupContextStack() {
        csf.setCurrentContextManager(cm);
    }

    public String getDbname() {
        return dbname;
    }

    public final void restoreContextStack() {
        if ((csf == null) || (cm == null)) {
            return;
        }
        csf.resetCurrentContextManager(cm);
    }

    public ContextManager getContextManager() {
        return cm;
    }

    public void rollback() {
        if (lcc != null) {
            //lcc.userRollback();
        }
    }

    public void commit() {
        DataDictionaryImpl dataDictionary = (DataDictionaryImpl) this.database.getDataDictionary();
        dataDictionary.finished();
        AccessManager accessManager = this.database.getAccessManager();
        cleanData(accessManager);
    }

    public static void cleanData(AccessManager accessManager) {
        RawStoreFactory rawStoreFactory = accessManager.getRawStoreFactory();
        BaseDataFileFactory dataFileFactory = rawStoreFactory.getDataFactory();

        dataFileFactory.checkpoint();

    }
}
