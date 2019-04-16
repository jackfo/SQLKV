package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.heap.BackingStoreHashtable;
import com.cfs.sqlkv.store.access.heap.OpenTable;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 12:59
 */
public abstract class GenericScanController extends GenericController implements ScanManager {

    private FormatableBitSet init_scanColumnList;

    private DataValueDescriptor[] init_startKeyValue;
    private int init_startSearchOperator;
    private Qualifier[][] init_qualifier;
    private DataValueDescriptor[] init_stopKeyValue;
    private int init_stopSearchOperator;

    protected int stat_numrows_qualified = 0;
    protected int stat_numpages_visited = 0;

    /**
     * 扫描存在五种状态:
     * SCAN_INIT:扫描已开始，但尚未进行任何定位。
     * 在第一次next（）调用完成后，扫描将被定位,在此状态下，所有定位状态变量均无效
     * SCAN_INPROGRESS:在第一次next（）调用之后，扫描处于此状态。
     * 退出任何GenericScanController方法，而在此状态下，扫描“指向”符合扫描条件的行
     * SCAN_DONE:停止扫表后会处于此状态
     * SCAN_HOLD_INIT:
     * SCAN_HOLD_INPROGRESS:
     */
    public static final int SCAN_INIT = 1;
    public static final int SCAN_INPROGRESS = 2;
    public static final int SCAN_DONE = 3;
    public static final int SCAN_HOLD_INIT = 4;
    public static final int SCAN_HOLD_INPROGRESS = 5;

    private int scan_state;


    private FetchDescriptor init_fetchDesc;

    private BaseContainerHandle baseContainerHandle;


    /**
     * 当前扫描的位置:
     */
    protected RowPosition scan_position;


    /**
     * 从表中获取接下来N条数据
     */
    protected int fetchRows(DataValueDescriptor[][] row_array, TableRowLocation[] rowloc_array, BackingStoreHashtable hash_table, long max_rowcnt, int[] key_column_numbers) {
        int ret_row_count = 0;
        DataValueDescriptor[] fetch_row = null;
        //max_rowcnt标识最大读取行数,一般传入是row_array的长度
        if (max_rowcnt == -1) {
            max_rowcnt = Long.MAX_VALUE;
        }
        if (this.scan_state == SCAN_INPROGRESS) {
            positionAtResumeScan(scan_position);
        } else if (this.scan_state == SCAN_INIT) {
            positionAtStartForForwardScan(scan_position);
        } else {
            return 0;
        }

        while (scan_position.current_page != null) {
            //当前槽位小于页面记录数的时候
            while ((scan_position.current_slot + 1) < scan_position.current_page.recordCount()) {
                if (fetch_row == null) {
                    if (hash_table == null) {
                        if (row_array[ret_row_count] == null) {
                            //如果数据描述为空则设置模板
                            row_array[ret_row_count] = open_table.getRuntimeMem().get_row_for_export(open_table.getRawTransaction());
                        }
                        fetch_row = row_array[ret_row_count];
                    }
                }

                //移动槽位,这是因为scan_position里面记录的还是上一个槽位
                scan_position.positionAtNextSlot();
                scan_position.current_rh = scan_position.current_page.getRecordIdAtSlot(scan_position.current_slot);
                //从scan的槽位开始获取数据
                scan_position.current_rh_qualified = (scan_position.current_page.fetchFromSlot(
                        scan_position.current_rh, scan_position.current_slot,
                        fetch_row, init_fetchDesc, false) != null);
                if (scan_position.current_rh_qualified) {
                    ret_row_count++;
                    stat_numrows_qualified++;
                    if (hash_table == null) {
                        if (rowloc_array != null) {
                            setRowLocationArray(rowloc_array, ret_row_count - 1, scan_position);
                        }
                        fetch_row = null;
                    } else {
                        throw new RuntimeException("the feature not implement");
                    }
                    if (max_rowcnt <= ret_row_count) {
                        scan_position.unlatch();
                        return ret_row_count;
                    }
                }
            }
            positionAtNextPage(scan_position);
            this.stat_numpages_visited++;


        }


        positionAtDoneScan(scan_position);

        this.stat_numpages_visited--;
        return ret_row_count;
    }

    /**
     * 设定已经完成扫描
     */
    protected void positionAtDoneScan(RowPosition pos) {
        pos.unlatch();
        scan_position.current_rh = null;
        this.scan_state = SCAN_DONE;

    }

    protected void positionAtResumeScan(RowPosition pos) {
        open_table.latchPageAndRepositionScan(scan_position);
    }


    protected void positionAtStartForForwardScan(RowPosition pos) {
        //如果扫描位置为空,标识还没有扫描过,获取第一页的第一个槽位
        if (pos.current_rh == null) {
            pos.current_page = open_table.getContainer().getFirstPage();
            pos.current_slot = Page.FIRST_SLOT_NUMBER;
        } else {
            open_table.latchPageAndRepositionScan(pos);
            pos.current_slot -= 1;
        }
        pos.current_rh = null;
        this.stat_numpages_visited = 1;
        this.scan_state = SCAN_INPROGRESS;
    }

    /**
     * 初始化扫描位置和设定
     * 扫描状态
     * 设定扫描列的长度
     */
    protected void positionAtInitScan(DataValueDescriptor[] startKeyValue, int startSearchOperator,
                                      Qualifier qualifier[][], DataValueDescriptor[] stopKeyValue,
                                      int stopSearchOperator, RowPosition pos) {
        this.init_startKeyValue = startKeyValue;
        if (RowUtil.isRowEmpty(this.init_startKeyValue)) {
            this.init_startKeyValue = null;
        }
        this.init_startSearchOperator = startSearchOperator;
        if ((qualifier != null) && (qualifier.length == 0)) {
            qualifier = null;
        }
        this.init_qualifier = qualifier;

        OpenConglomerateScratchSpace openConglomerateScratchSpace = open_table.getRuntimeMem();

        Transaction raw_transaction = open_table.getRawTransaction();
        int input_row_length = openConglomerateScratchSpace.get_scratch_row(raw_transaction).length;
        init_fetchDesc = new FetchDescriptor(input_row_length, init_scanColumnList, init_qualifier);
        this.init_stopKeyValue = stopKeyValue;
        if (RowUtil.isRowEmpty(this.init_stopKeyValue)) {
            this.init_stopKeyValue = null;
        }
        this.init_stopSearchOperator = stopSearchOperator;
        pos.init();
        scan_state = SCAN_INIT;
    }


    public void init(OpenTable openTable, FormatableBitSet scanColumnList,
                     DataValueDescriptor[] startKeyValue, DataValueDescriptor[] stopKeyValue,
                     int startSearchOperator, Qualifier[][] qualifiers, int stopSearchOperator) {
        super.init(openTable);
        //获取扫描位置
        scan_position = open_table.getRuntimeMem().get_scratch_row_position();

        init_scanColumnList = scanColumnList;

        positionAtInitScan(startKeyValue, startSearchOperator, qualifiers, stopKeyValue, stopSearchOperator, scan_position);

    }

    public void fetch(DataValueDescriptor[] row) {
        fetch(row, true);
    }

    private void fetch(DataValueDescriptor[] row, boolean qualify) {
        RecordId recordId =
                scan_position.current_page.fetchFromSlot(
                        scan_position.current_rh,
                        scan_position.current_slot,
                        row,
                        qualify ? init_fetchDesc : null,
                        false);
    }

    protected abstract void setRowLocationArray(TableRowLocation[] rowloc_array, int index, RowPosition pos);


    public void init(OpenTable open_table, Qualifier qualifier[][]) {
        super.init(open_table);
        scan_position = open_table.getRuntimeMem().get_scratch_row_position();
        positionAtInitScan(
                null,
                0,
                qualifier,
                null,
                0,
                scan_position);
    }


    /**
     * 将位置移动到下一页
     */
    public void positionAtNextPage(RowPosition pos) {
        if (pos.current_page != null) {
            long pageid = pos.current_page.getPageNumber();
            pos.unlatch();
            pos.current_page = open_table.getContainer().getNextPage(pageid);
            pos.current_slot = Page.FIRST_SLOT_NUMBER - 1;
        }
    }

    public TableRowLocation newRowLocationTemplate() {
        return open_table.newRowLocationTemplate();
    }


}
