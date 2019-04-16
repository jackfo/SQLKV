package com.cfs.sqlkv.db;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.DataDictionaryImpl;
import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.common.PersistentService;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.factory.BaseDataFileFactory;
import com.cfs.sqlkv.factory.GenericLanguageConnectionFactory;
import com.cfs.sqlkv.factory.GenericLanguageFactory;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;
import com.cfs.sqlkv.jdbc.TransactionResourceImpl;
import com.cfs.sqlkv.row.RawStoreFactory;
import com.cfs.sqlkv.store.TransactionControl;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.transaction.AccessManager;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 19:12
 */
public class BasicDatabase implements Database {

    private DataDictionary dd;

    protected LanguageConnectionFactory lcf;

    protected GenericLanguageFactory lf;

    protected AccessManager accessManager;


    public BasicDatabase() {
    }

    public void init(ContextManager cm, boolean create) {
        accessManager = new AccessManager();
        lcf = new GenericLanguageConnectionFactory();
        lf = new GenericLanguageFactory();
        TransactionManager transactionController = null;

        transactionController = accessManager.getAndNameTransaction(cm, "bootTc");

        dd = new DataDictionaryImpl(transactionController);
        ((DataDictionaryImpl) dd).initializeCoreInfo(create);
        if (create) {
            TransactionManager transactionControl = ((DataDictionaryImpl) dd).transactionController;

            ((DataDictionaryImpl) dd).createDictionaryTables(transactionControl);

        } else {
            TransactionManager transactionControl = ((DataDictionaryImpl) dd).transactionController;

            ((DataDictionaryImpl) dd).loadDictionaryTables(transactionControl);

        }

        TransactionResourceImpl.cleanData(accessManager);
    }

    @Override
    public AccessManager getAccessManager() {
        return accessManager;
    }


    @Override
    public DataDictionary getDataDictionary() {
        return dd;
    }

    @Override
    public LanguageConnectionContext setupConnection(ContextManager cm, String user, String drdaID, String dbname) {
        TransactionManager tc = null;

        tc = getConnectionTransaction(cm);

        LanguageConnectionContext lctx = null;

        lctx = lcf.newLanguageConnectionContext(cm, tc, lf, this, user, drdaID, dbname);
        lctx.initialize();

        return lctx;
    }

    protected TransactionManager getConnectionTransaction(ContextManager cm) {
        return accessManager.getTransaction(cm);
    }


}
