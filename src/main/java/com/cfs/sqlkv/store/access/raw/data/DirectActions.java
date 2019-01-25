package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 18:40
 */
public class DirectActions implements PageActions{

    @Override
    public void actionInitPage(Transaction t, BasePage page, int initFlag, int pageFormatId, long pageOffset) throws StandardException{
        boolean overflowPage = ((initFlag & BasePage.INIT_PAGE_OVERFLOW) != 0);
        boolean reuse = ((initFlag & BasePage.INIT_PAGE_REUSE) != 0);

        int nextRecordId = ((initFlag & BasePage.INIT_PAGE_REUSE_RECORDID) == 0) ? page.newRecordId() : RecordHandle.FIRST_RECORD_ID;
        page.initPage(null, BasePage.VALID_PAGE, nextRecordId, overflowPage, reuse);

    }
}
