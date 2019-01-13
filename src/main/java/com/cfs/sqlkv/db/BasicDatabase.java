package com.cfs.sqlkv.db;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.DataDictionaryImpl;
import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.GenericLanguageConnectionFactory;
import com.cfs.sqlkv.factory.GenericLanguageFactory;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;
import com.cfs.sqlkv.store.TransactionController;
import com.cfs.sqlkv.transaction.AccessManager;

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

    public BasicDatabase(){
        accessManager = new AccessManager();
        lcf = new GenericLanguageConnectionFactory();
        lf = new GenericLanguageFactory();
        dd = new DataDictionaryImpl();
    }

    @Override
    public DataDictionary getDataDictionary() {
        return dd;
    }

    @Override
    public LanguageConnectionContext setupConnection(ContextManager cm, String user, String drdaID, String dbname) throws StandardException {
        TransactionController tc = getConnectionTransaction(cm);
        LanguageConnectionContext lctx = lcf.newLanguageConnectionContext(cm, tc, lf, this, user, drdaID, dbname);
        return lctx;
    }

    protected TransactionController getConnectionTransaction(ContextManager cm) throws StandardException {
        return accessManager.getTransaction(cm);
    }


}
