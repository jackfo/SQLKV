package com.cfs.sqlkv.btree;

import com.cfs.sqlkv.btree.controlrow.LeafControlRow;
import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.AccessFactoryGlobals;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.conglomerate.OpenConglomerateScratchSpace;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import static com.sun.javaws.jnl.MatcherReturnCode.MATCH_FOUND;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 15:55
 */
public class BTreeController extends OpenBTree implements ConglomerateController {

    transient DataValueDescriptor[] scratch_template = null;


    public void init(TransactionManager transactionManager, Transaction raw_transaction, BTree conglomerate) {
        transactionManager.openConglomerate(conglomerate.baseConglomerateId, false);
        super.init(transactionManager, null, raw_transaction, conglomerate);
    }

    @Override
    public void insertAndFetchLocation(DataValueDescriptor[] row, TableRowLocation destRowLocation) {
        throw new RuntimeException("btree not implemented feature");
    }

    @Override
    public int insert(DataValueDescriptor[] row) {
        return doIns(row);
    }

    private int doIns(DataValueDescriptor[] rowToInsert) {
        LeafControlRow targetleaf = null;
        LeafControlRow save_targetleaf = null;
        int insert_slot = 0;
        int result_slot = 0;
        int ret_val = 0;
        boolean reclaim_deleted_rows_attempted = false;
        if (scratch_template == null) {
            scratch_template = runtime_mem.get_template(getRawTran());
        }
        SearchParameters sp = new SearchParameters(rowToInsert,
                SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH,
                scratch_template, this, false);

        while (true) {
            LeafControlRow leafControlRow = (LeafControlRow) LeafControlRow.getPage(this, BTree.ROOTPAGEID);
            targetleaf = (LeafControlRow) leafControlRow.search(sp);
            if (sp.resultExact) {
                result_slot = insert_slot = sp.resultSlot;
                if (!(targetleaf.page.isDeletedAtSlot(insert_slot))) {
                    ret_val = ConglomerateController.ROWISDUPLICATE;
                    break;
                } else {
                    if (this.getConglomerate().nKeyFields == this.getConglomerate().nUniqueColumns) {
                        targetleaf.page.deleteAtSlot(insert_slot, false);
                        break;
                    } else if (this.getConglomerate().nUniqueColumns == (this.getConglomerate().nKeyFields - 1)) {
                        targetleaf.page.deleteAtSlot(insert_slot, false);
                        boolean update_succeeded = true;

                        int rowloc_index = this.getConglomerate().nKeyFields - 1;
                        DataValueDescriptor column = (DataValueDescriptor) RowUtil.getColumn(rowToInsert, null, rowloc_index);
                        targetleaf.page.updateFieldAtSlot(insert_slot, rowloc_index, column);

                        if (update_succeeded)
                            break;
                    } else {


                    }
                }
            } else if (targetleaf.page.recordCount() - 1 < BTree.maxRowsPerPage) {
                insert_slot = sp.resultSlot + 1;
                result_slot = insert_slot + 1;
                if (targetleaf.page.insertAtSlot(insert_slot, rowToInsert, null, null, Page.INSERT_DEFAULT, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD) != null) {
                    break;
                }
                if (targetleaf.page.recordCount() <= 2) {
                    throw new RuntimeException("no space for key");
                }
            }

            int flag = 0;
            if (insert_slot == 1) {
                flag |= LeafControlRow.SPLIT_FLAG_FIRST_ON_PAGE;
                if (targetleaf.isLeftmostLeaf()) {
                    flag |= LeafControlRow.SPLIT_FLAG_FIRST_IN_TABLE;
                }
            } else if (insert_slot == targetleaf.page.recordCount()) {
                flag |= LeafControlRow.SPLIT_FLAG_LAST_ON_PAGE;
                if (targetleaf.isRightmostLeaf())
                    flag |= LeafControlRow.SPLIT_FLAG_LAST_IN_TABLE;
            }

            long targetleaf_pageno = targetleaf.page.getPageNumber();

            if ((targetleaf.page.recordCount() - targetleaf.page.nonDeletedRecordCount()) <= 0) {
                reclaim_deleted_rows_attempted = true;
            }
            BranchRow branchrow = BranchRow.createBranchRowFromOldLeafRow(rowToInsert, targetleaf_pageno);
            targetleaf.release();

            start_dosplit(!reclaim_deleted_rows_attempted, targetleaf_pageno, scratch_template, branchrow.getRow(), flag);

            reclaim_deleted_rows_attempted = true;

        }

        targetleaf.last_search_result = result_slot;
        targetleaf.release();
        targetleaf = null;
        return ret_val;
    }

    private long start_dosplit(boolean attempt_to_reclaim_deleted_rows,
                               long leaf_pageno, DataValueDescriptor[] scratch_template,
                               DataValueDescriptor[] rowToInsert, int flag) {

        OpenBTree split_open_btree = null;
        ControlRow root = null;
        boolean do_split = true;
        if (attempt_to_reclaim_deleted_rows) {
            split_open_btree = new OpenBTree();
            split_open_btree.init(
                    this.init_open_user_scans, null,
                    init_open_user_scans.getRawStoreFactoryTransaction(), this.getConglomerate());
            do_split = !reclaim_deleted_rows(split_open_btree, leaf_pageno);
            split_open_btree.close();
        }

        long new_leaf_pageno = leaf_pageno;
        if (do_split) {
            split_open_btree = new OpenBTree();
            split_open_btree.init(
                    this.init_open_user_scans, null,
                    init_open_user_scans.getRawStoreFactoryTransaction(), this.getConglomerate());
            root = ControlRow.getPage(split_open_btree, BTree.ROOTPAGEID);

            new_leaf_pageno = root.splitFor(split_open_btree, scratch_template,
                    null, rowToInsert, flag);
            split_open_btree.close();
        }
        return new_leaf_pageno;
    }

    private boolean reclaim_deleted_rows(OpenBTree open_btree, long pageno) {
        boolean purged_at_least_one_row = false;
        ControlRow controlRow = null;

        try {
            if ((controlRow = ControlRow.getPage(open_btree, pageno)) == null)
                return (false);
            LeafControlRow leaf = (LeafControlRow) controlRow;
            int num_possible_commit_delete = leaf.page.recordCount() - 1 - leaf.page.nonDeletedRecordCount();
            if (num_possible_commit_delete > 0) {
                Page page = leaf.page;
                FetchDescriptor lock_fetch_desc = RowUtil.getFetchDescriptorConstant(scratch_template.length - 1);
                for (int slot_no = page.recordCount() - 1;
                     slot_no > 0;
                     slot_no--) {
                    if (page.isDeletedAtSlot(slot_no)) {
                        page.purgeAtSlot(slot_no, 1, true);
                        purged_at_least_one_row = true;
                    }
                }
            }
        } catch (java.lang.ClassCastException cce) {
        } finally {
            if (controlRow != null) {
                if (purged_at_least_one_row) {
                    controlRow.page.setRepositionNeeded();
                } else {
                    controlRow.release();
                }
            }
        }
        return purged_at_least_one_row;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean fetch(TableRowLocation loc, DataValueDescriptor[] destRow, FormatableBitSet validColumns) {
        throw new RuntimeException("btree not implemented feature");
    }

    public boolean replace(TableRowLocation loc, DataValueDescriptor[] row, FormatableBitSet validColumns) {
        throw new RuntimeException("the feature is not support");
    }

    @Override
    public TableRowLocation newRowLocationTemplate() {
        throw new RuntimeException("btree not implemented feature");
    }

    @Override
    public boolean delete(TableRowLocation loc) {
        throw new RuntimeException("the feature is not support");
    }
}
