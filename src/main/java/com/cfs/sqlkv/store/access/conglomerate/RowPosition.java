package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.store.access.raw.data.RecordId;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 13:32
 */
public class RowPosition {
    /**当前页*/
    public Page     current_page;
    /**当前记录*/
    public RecordId current_rh;
    /**当前槽位*/
    public int      current_slot;
    public boolean  current_rh_qualified;
    /**页号*/
    public long     current_pageno;

    public RowPosition(){}
    public void init() {
        current_page            = null;
        current_rh              = null;
        current_slot            = Page.INVALID_SLOT_NUMBER;
        current_rh_qualified    = false;
        current_pageno          = BaseContainerHandle.INVALID_PAGE_NUMBER;
    }

    public final void positionAtNextSlot() {
        current_slot++;
        current_rh   = null;
    }
    public final void positionAtPrevSlot() {
        current_slot--;
        current_rh   = null;
    }
    public void unlatch() {
        if (current_page != null) {
            current_page.unlatch();
            current_page = null;
        }
        current_slot = Page.INVALID_SLOT_NUMBER;
    }

    @Override
    public String toString() {
        return "RowPosition{" +
                "current_page=" + current_page +
                ", current_rh=" + current_rh +
                ", current_slot=" + current_slot +
                ", current_rh_qualified=" + current_rh_qualified +
                ", current_pageno=" + current_pageno +
                '}';
    }
}
