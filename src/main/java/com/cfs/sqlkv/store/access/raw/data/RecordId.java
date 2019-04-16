package com.cfs.sqlkv.store.access.raw.data;

import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.PageKey;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-13 15:13
 */
public class RecordId {

    /**页面标识*/
    private final PageKey pageId;

    /**当前记录行ID*/
    private final int recordId;

    transient private int slotNumberHint;

    /**首条记录标识*/
    public static final int FIRST_RECORD_ID = 6;


    public RecordId(ContainerKey container, long pageNumber, int recordId) {
        this.pageId = new PageKey(container, pageNumber);
        this.recordId = recordId;
    }

    public RecordId(PageKey pageId, int recordId) {
        this.pageId = pageId;
        this.recordId = recordId;
    }

    public RecordId(PageKey pageId, int recordId, int current_slot) {
        this.pageId = pageId;
        this.recordId = recordId;
        this.slotNumberHint = current_slot;
    }

    public int	getId() {
        return recordId;
    }

    public long getPageNumber(){
        return pageId.getPageNumber();
    }

    public Object getPageId() {
        return pageId;
    }
    public ContainerKey getContainerId() {
        return pageId.getContainerId();
    }

    public int getSlotNumberHint() {
        return slotNumberHint;
    }


}
