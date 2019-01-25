package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.transaction.Transaction;

public interface PageActions {

    public void actionInitPage(Transaction t, BasePage page, int initFlag, int pageFormatId, long pageOffset) throws StandardException;

}
