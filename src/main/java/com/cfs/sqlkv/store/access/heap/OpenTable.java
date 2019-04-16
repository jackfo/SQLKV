package com.cfs.sqlkv.store.access.heap;


import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.conglomerate.OpenConglomerateScratchSpace;
import com.cfs.sqlkv.store.access.conglomerate.RowPosition;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.transaction.Transaction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 16:07
 */
public class OpenTable {

    private BaseContainerHandle container;

    private Transaction init_raw_transaction;

    private Conglomerate init_conglomerate;

    public final BaseContainerHandle getContainer() {
        return container;
    }

    /**
     * 根据记录id获取当前页
     * 根据记录id获取页对应槽位号
     *
     * @return 返回扫描是否已经改变
     */
    public boolean latchPageAndRepositionScan(RowPosition pos) {
        boolean scan_repositioned = false;
        pos.current_page = null;
        try {
            if (pos.current_rh != null) {
                pos.current_page = container.getPage(pos.current_rh.getPageNumber());
            }
        } catch (Throwable t) {

        }
        if (pos.current_page != null) {

            pos.current_slot = pos.current_page.getSlotNumber(pos.current_rh);

        }

        //如果根据记录Id没有获取到当前页,则根据记录==》位置的页号获取下一页 加载到位置
        if (pos.current_page == null) {
            long current_pageno;
            if (pos.current_rh != null) {
                current_pageno = pos.current_rh.getPageNumber();
            } else if (pos.current_pageno != BaseContainerHandle.INVALID_PAGE_NUMBER) {
                current_pageno = pos.current_pageno;
            } else {
                //没有合法的位置,返回空页
                return false;
            }
            pos.current_page = container.getNextPage(current_pageno);
            pos.current_slot = Page.FIRST_SLOT_NUMBER - 1;
            pos.current_pageno = BaseContainerHandle.INVALID_PAGE_NUMBER;
            scan_repositioned = true;
        }
        if (scan_repositioned) {
            pos.current_rh = null;
        }
        return scan_repositioned;
    }

    public BaseContainerHandle init(Conglomerate conglomerate, BaseContainerHandle open_container, Transaction raw_transaction) {
        this.init_conglomerate = conglomerate;
        this.init_raw_transaction = raw_transaction;
        if (open_container != null) {
            this.container = open_container;
        } else {
            this.container = init_raw_transaction.openContainer(init_conglomerate.getId());
        }
        this.runtime_mem = conglomerate.getDynamicCompiledConglomInfo();
        return container;

    }

    private OpenConglomerateScratchSpace runtime_mem;

    public final OpenConglomerateScratchSpace getRuntimeMem() {
        return runtime_mem;
    }

    public final Transaction getRawTransaction() {
        return init_raw_transaction;
    }

    public boolean latchPage(RowPosition pos) {
        pos.current_page = null;
        try {
            pos.current_page = container.getPage(pos.current_rh.getPageNumber());
        } catch (Throwable t) {
        }
        if (pos.current_page != null) {
            try {
                pos.current_slot = pos.current_page.getSlotNumber(pos.current_rh);
                return true;
            } catch (Throwable t) {
                throw new RuntimeException("can't get currentPage");
            }
        }
        return false;
    }

    public TableRowLocation newRowLocationTemplate() {
        return new TableRowLocation();
    }


    public final boolean isClosed() {
        return (container == null);
    }
}
