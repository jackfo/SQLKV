package com.cfs.sqlkv.store.access.heap;


import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.AccessFactoryGlobals;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.GenericConglomerateController;
import com.cfs.sqlkv.store.access.conglomerate.GenericController;
import com.cfs.sqlkv.store.access.conglomerate.RowPosition;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-25 16:05
 */
public class TableController extends GenericConglomerateController implements ConglomerateController {

    /**
     * 插入数据描述
     *
     * @param row 行数据描述
     */
    private RecordId doInsert(DataValueDescriptor[] row)   {
        Page page = null;
        byte insert_mode;
        RecordId rh;


        //获取插入页
        page = open_table.getContainer().getPageForInsert(0);

        if (page != null) {
            /**
             * 设置插入模式
             * */
            insert_mode = (page.recordCount() == 0) ? Page.INSERT_OVERFLOW : Page.INSERT_DEFAULT;
            rh = page.insert(row, null, insert_mode, AccessFactoryGlobals.HEAP_OVERFLOW_THRESHOLD);
            page.unlatch();
            page = null;
            if (rh != null) {
                return rh;
            }
        }

        //获取首个未插满的页
        page = open_table.getContainer().getPageForInsert(BaseContainerHandle.GET_PAGE_UNFILLED);
        if (page != null) {
            if (page.recordCount() == 0) {
                insert_mode = Page.INSERT_OVERFLOW;
            } else {
                insert_mode = Page.INSERT_DEFAULT;
            }
            rh = page.insert(row, null, insert_mode, AccessFactoryGlobals.HEAP_OVERFLOW_THRESHOLD);
            if (rh != null) {
                return rh;
            }
        }
        //如果找不到插入页,则添加一个页,再将数据添加进去
        page = open_table.getContainer().addPage();
        rh = page.insert(row, null, Page.INSERT_OVERFLOW, AccessFactoryGlobals.HEAP_OVERFLOW_THRESHOLD);
        return rh;
    }

    /**
     * 将行插入到记录
     */
    @Override
    public void insertAndFetchLocation(DataValueDescriptor[] row, TableRowLocation templateRowLocation)   {
        RecordId rh = doInsert(row);
        TableRowLocation hrl = templateRowLocation;
        hrl.setFrom(rh);
    }

    @Override
    public int insert(DataValueDescriptor[] row)   {
        doInsert(row);
        return 0;
    }

    @Override
    public void close()   {

    }



    @Override
    public TableRowLocation newRowLocationTemplate()   {
        return new TableRowLocation();
    }

    /**
     * 根据行位置获取记录标识
     */
    protected final void getRowPositionFromRowLocation(TableRowLocation row_loc, RowPosition pos)   {
        pos.current_rh = row_loc.getRecordId(open_table.getContainer());
        pos.current_rh_qualified = true;
    }


}
