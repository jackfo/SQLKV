package com.cfs.sqlkv.btree;

import com.cfs.sqlkv.btree.controlrow.LeafControlRow;
import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.ScanController;
import com.cfs.sqlkv.store.access.conglomerate.ScanManager;
import com.cfs.sqlkv.store.access.heap.BackingStoreHashtable;
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
 * @create 2019-04-02 16:25
 */
public class BTreeScan extends OpenBTree implements ScanManager {
    protected Transaction init_rawtran = null;
    protected boolean init_forUpdate;
    protected FormatableBitSet init_scanColumnList;
    protected DataValueDescriptor[] init_template;
    protected DataValueDescriptor[] init_startKeyValue;
    protected int init_startSearchOperator = 0;
    protected Qualifier init_qualifier[][] = null;
    protected DataValueDescriptor[] init_stopKeyValue;
    protected int init_stopSearchOperator = 0;
    protected boolean init_hold;
    protected FetchDescriptor init_fetchDesc;
    protected FetchDescriptor init_lock_fetch_desc;

    protected static final int SCAN_INIT = 1;
    protected static final int SCAN_INPROGRESS = 2;
    protected static final int SCAN_DONE = 3;
    protected static final int SCAN_HOLD_INIT = 4;
    protected static final int SCAN_HOLD_INPROGRESS = 5;

    BTreeRowPosition scan_position;

    protected int scan_state = SCAN_INIT;

    protected DataValueDescriptor[][] fetchNext_one_slot_array = new DataValueDescriptor[1][];

    public BTreeScan() {
    }

    protected int fetchRows(BTreeRowPosition pos, DataValueDescriptor[][] row_array, TableRowLocation[] tableRowLocations, BackingStoreHashtable hash_table, long max_rowcnt, int[] key_column_numbers) {
        int return_row_count = 0;
        DataValueDescriptor[] fetch_row = null;
        RecordId rh;
        if (max_rowcnt == -1) {
            max_rowcnt = Long.MAX_VALUE;
        }
        if (this.scan_state == BTreeScan.SCAN_INPROGRESS) {
            reposition(pos, true);
        } else if (this.scan_state == SCAN_INIT) {
            positionAtStartPosition(pos);
        } else if (this.scan_state == SCAN_HOLD_INPROGRESS) {
            reopen();
            this.scan_state = SCAN_INPROGRESS;
            reposition(pos, true);
        } else if (this.scan_state == SCAN_HOLD_INIT) {
            reopen();
            positionAtStartForForwardScan(scan_position);
        } else {
            return 0;
        }
        leaf_loop:
        while (pos.current_leaf != null) {
            slot_loop:
            //当前槽位小于页面记录数,则获取数据,如果页面没有数据则无法进行获取
            while ((pos.current_slot + 1) < pos.current_leaf.page.recordCount()) {
                if (fetch_row == null) {
                    if (hash_table == null) {
                        if (row_array[return_row_count] == null) {
                            row_array[return_row_count] = runtime_mem.get_row_for_export(getRawTran());
                        }
                        fetch_row = row_array[return_row_count];
                    } else {
                        fetch_row = runtime_mem.get_row_for_export(getRawTran());
                    }
                }
                pos.current_slot++;
                this.stat_numrows_visited++;
                rh = pos.current_leaf.page.fetchFromSlot(null, pos.current_slot, fetch_row, init_fetchDesc, true);
                pos.current_rh_qualified = true;

                if (init_stopKeyValue != null) {
                    int ret = ControlRow.compareIndexRowToKey(fetch_row, init_stopKeyValue, fetch_row.length, 0, this.getConglomerate().ascDescInfo);
                    if ((ret == 0) && (init_stopSearchOperator == ScanController.GE)) {
                        ret = 1;
                    }
                    if (ret > 0) {
                        pos.current_leaf.release();
                        pos.current_leaf = null;
                        positionAtDoneScan(pos);
                        return return_row_count;
                    }
                }
                pos.current_rh = rh;
                if (pos.current_leaf.page.isDeletedAtSlot(pos.current_slot)) {
                    this.stat_numdeleted_rows_visited++;
                    pos.current_rh_qualified = false;
                } else if (init_qualifier != null) {
                    pos.current_rh_qualified = this.process_qualifier(fetch_row);
                }

                if (pos.current_rh_qualified) {
                    return_row_count++;
                    stat_numrows_qualified++;
                    final boolean doneWithGroup = max_rowcnt <= return_row_count;

                    if (doneWithGroup) {
                        int[] vcols = init_fetchDesc.getValidColumnsArray();
                        savePositionAndReleasePage(fetch_row, vcols);
                    }

                    if (doneWithGroup) {
                        return return_row_count;
                    }
                }
            }
            positionAtNextPage(pos);
            this.stat_numpages_visited++;
        }
        positionAtDoneScan(pos);
        this.stat_numpages_visited--;
        return return_row_count;
    }

    /**
     * 重新定位扫描
     */
    protected boolean reposition(BTreeRowPosition pos, boolean missing_row_for_key_ok) {
        if (this.scan_state != SCAN_INPROGRESS) {
            throw new RuntimeException("Btree scan not positioned");
        }
        if (pos.current_positionKey == null) {
            throw new RuntimeException("current_positionKey can't be null");
        }
        if (pos.current_rh != null) {
            Page page = container.getPage(pos.current_rh.getPageNumber());

            if (page != null) {
                ControlRow row = ControlRow.getControlRowForPage(page);
                if (row instanceof LeafControlRow) {

                    pos.current_leaf = (LeafControlRow) row;
                    pos.current_slot = row.page.getSlotNumber(pos.current_rh);
                    pos.current_positionKey = null;
                    return true;
                }

            }
        }

        SearchParameters sp = new SearchParameters(pos.current_positionKey, SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH, init_template, this, false);
        LeafControlRow leafControlRow = (LeafControlRow) LeafControlRow.getPage(this, BTree.ROOTPAGEID);
        pos.current_leaf = (LeafControlRow) leafControlRow.search(sp);

        if (!sp.resultExact && !missing_row_for_key_ok) {
            pos.current_leaf.release();
            pos.current_leaf = null;
            return (false);
        }
        pos.current_slot = sp.resultSlot;
        if (pos.current_rh != null) {
            pos.current_rh = pos.current_leaf.page.getRecordIdAtSlot(pos.current_slot);
        }
        pos.current_positionKey = null;
        return true;
    }

    /**
     * 设置扫描开始位置
     */
    protected void positionAtStartPosition(BTreeRowPosition pos) {
        positionAtStartForForwardScan(pos);
    }

    protected int stat_numpages_visited = 0;
    protected int stat_numrows_visited = 0;
    protected int stat_numrows_qualified = 0;
    protected int stat_numdeleted_rows_visited = 0;

    /**
     * 设置开始扫描的位置
     * 如果init_startKeyValue为空,则从根节点开始
     * 如果init_startKeyValue不为空,构造查找参数,找到对应的位置
     */
    protected void positionAtStartForForwardScan(BTreeRowPosition pos) {
        boolean exact;
        ControlRow root = LeafControlRow.getPage(this, BTree.ROOTPAGEID);
        stat_numpages_visited += root.getLevel() + 1;
        if (init_startKeyValue == null) {
            pos.current_leaf = (LeafControlRow) root;
            pos.current_slot = LeafControlRow.CR_SLOT;
            exact = false;
        } else {
            SearchParameters sp = new SearchParameters(
                    init_startKeyValue,
                    ((init_startSearchOperator == ScanController.GE) ?
                            SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH :
                            SearchParameters.POSITION_RIGHT_OF_PARTIAL_KEY_MATCH),
                    init_template, this, false);

            pos.current_leaf = (LeafControlRow) root.search(sp);
            pos.current_slot = sp.resultSlot;
            exact = sp.resultExact;
            if (exact && init_startSearchOperator == ScanController.GE) {
                pos.current_slot--;
            }

            this.scan_state = SCAN_INPROGRESS;
        }
    }


    protected void positionAtDoneScan(BTreeRowPosition pos) {
        pos.current_slot = Page.INVALID_SLOT_NUMBER;
        pos.current_rh = null;
        pos.current_positionKey = null;
        this.scan_state = SCAN_DONE;
        return;
    }

    protected boolean process_qualifier(DataValueDescriptor[] row) {
        throw new RuntimeException("..");
    }


    public void savePositionAndReleasePage(DataValueDescriptor[] partialKey, int[] vcols) {
        //获取控制行的列
        final Page page = scan_position.current_leaf.getPage();
        try {
            DataValueDescriptor[] fullKey = scan_position.getKeyTemplate();

            FetchDescriptor fetchDescriptor = null;
            boolean haveAllColumns = false;
            if (partialKey != null) {
                int copiedCols = 0;
                final int partialKeyLength =
                        (vcols == null) ? partialKey.length : vcols.length;
                for (int i = 0; i < partialKeyLength; i++) {
                    if (vcols == null || vcols[i] != 0) {
                        fullKey[i].setValue(partialKey[i]);
                        copiedCols++;
                    }
                }
                if (copiedCols < fullKey.length) {
                    fetchDescriptor = scan_position.getFetchDescriptorForSaveKey(vcols, fullKey.length);
                } else {
                    haveAllColumns = true;
                }
            }

            if (!haveAllColumns) {
                RecordId rh = page.fetchFromSlot(null, scan_position.current_slot, fullKey, fetchDescriptor, true);
            }
            scan_position.current_positionKey = fullKey;
            scan_position.current_slot = Page.INVALID_SLOT_NUMBER;
        } finally {
            scan_position.current_leaf.release();
            scan_position.current_leaf = null;
        }
    }

    public void savePositionAndReleasePage() {
        savePositionAndReleasePage(null, null);
    }

    protected void positionAtNextPage(BTreeRowPosition pos) {
        pos.next_leaf = (LeafControlRow) pos.current_leaf.getRightSibling(this);
        pos.current_leaf.release();
        pos.current_leaf = pos.next_leaf;
        pos.current_slot = Page.FIRST_SLOT_NUMBER;
        pos.current_rh = null;
    }


    @Override
    public int fetchNextGroup(DataValueDescriptor[][] row_array, TableRowLocation[] rowloc_array) {
        return fetchRows(scan_position, row_array,
                rowloc_array, null, row_array.length, null);
    }

    @Override
    public boolean fetchNext(DataValueDescriptor[] row) {
        fetchNext_one_slot_array[0] = row;
        int fetchRows = fetchRows(scan_position, fetchNext_one_slot_array, null, null, 1, null);
        return fetchRows == 1;
    }

    @Override
    public void fetch(DataValueDescriptor[] destRow) {
        fetch(destRow, true);
    }

    @Override
    public TableRowLocation newRowLocationTemplate() {
        throw new RuntimeException("不支持");
    }

    @Override
    public void fetchLocation(TableRowLocation destRowLocation) {
        throw new RuntimeException("不支持");
    }

    private void fetch(DataValueDescriptor[] row, boolean qualify) {
        if (scan_state != SCAN_INPROGRESS)
            throw new RuntimeException("scan_state isn't SCAN_INPROGRESS");
        try {
            if (!reposition(scan_position, false)) {
                throw new RuntimeException(String.format("record not found PageNumber is %s id is %s", scan_position.current_rh.getPageNumber(), scan_position.current_rh.getId()));
            }
            scan_position.current_rh = scan_position.current_leaf.page.fetchFromSlot(
                    null, scan_position.current_slot, row,
                    qualify ? init_fetchDesc : null, true);
        } finally {
            if (scan_position.current_leaf != null) {
                savePositionAndReleasePage();
            }
        }
        return;
    }

    public void init(TransactionManager transactionManager, Transaction rawtran, FormatableBitSet scanColumnList, DataValueDescriptor[] startKeyValue, int startSearchOperator,Qualifier qualifier[][],
                     DataValueDescriptor[] stopKeyValue, int stopSearchOperator, BTree conglomerate) {
        super.init(transactionManager, null, rawtran, conglomerate);
        this.init_rawtran = rawtran;
        this.init_template = runtime_mem.get_template(getRawTran());
        this.init_scanColumnList = scanColumnList;
        this.init_fetchDesc = new FetchDescriptor(init_template.length, init_scanColumnList, null);
        initScanParams(startKeyValue, startSearchOperator, null, stopKeyValue, stopSearchOperator);
    }


    private void initScanParams(DataValueDescriptor[] startKeyValue, int startSearchOperator, Qualifier qualifier[][], DataValueDescriptor[] stopKeyValue,
                                int stopSearchOperator) {
        this.init_startKeyValue = startKeyValue;
        if (RowUtil.isRowEmpty(this.init_startKeyValue)) {
            this.init_startKeyValue = null;
        }
        this.init_startSearchOperator = startSearchOperator;
        this.init_qualifier = null;

        // stopKeyValue init.
        this.init_stopKeyValue = stopKeyValue;
        if (RowUtil.isRowEmpty(this.init_stopKeyValue))
            this.init_stopKeyValue = null;
        this.init_stopSearchOperator = stopSearchOperator;
        scan_position = new BTreeRowPosition(this);

        scan_position.init();

        scan_position.current_lock_template =
                new DataValueDescriptor[this.init_template.length];

        scan_position.current_lock_template[this.init_template.length - 1] =
                scan_position.current_lock_row_loc = (TableRowLocation) init_template[init_template.length - 1].cloneValue(false);

    }
}
