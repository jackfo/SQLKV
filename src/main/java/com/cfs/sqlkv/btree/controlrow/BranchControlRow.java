package com.cfs.sqlkv.btree.controlrow;

import com.cfs.sqlkv.btree.*;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.sql.types.SQLLongint;
import com.cfs.sqlkv.store.access.AccessFactoryGlobals;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Arrays;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-03 16:00
 */
public class BranchControlRow extends ControlRow {

    //左孩子页
    protected SQLLongint left_child_page = null;

    transient SQLLongint child_pageno_buf = null;

    private static final int CR_LEFTCHILD = ControlRow.CR_COLID_LAST + 1;
    private static final int CR_COLID_LAST = CR_LEFTCHILD;
    private static final int CR_NCOLUMNS = CR_COLID_LAST + 1;

    protected static final FormatableBitSet CR_LEFTCHILD_BITMAP = new FormatableBitSet(CR_LEFTCHILD + 1);


    static {
        CR_LEFTCHILD_BITMAP.set(CR_LEFTCHILD);
    }

    public BranchControlRow() {
    }

    @Override
    public int checkConsistency(OpenBTree btree, ControlRow parent, boolean check_other_pages) {
        return 0;
    }

    public BranchControlRow(OpenBTree open_btree, Page page, int level, ControlRow parent, boolean isRoot, long left_child) {
        super(open_btree, page, level, parent, isRoot);
        this.left_child_page = new SQLLongint(left_child);
        this.row[CR_LEFTCHILD] = left_child_page;
        child_pageno_buf = new SQLLongint();
    }


    //初始化控制行
    @Override
    public final void controlRowInit() {
        child_pageno_buf = new SQLLongint();
    }

    public boolean isLeftmostLeaf() {
        return false;
    }

    public boolean isRightmostLeaf() {
        return false;
    }

    public final int getNumberOfControlRowColumns() {
        return this.CR_NCOLUMNS;
    }

    public static long restartSplitFor(OpenBTree open_btree, DataValueDescriptor[] template,
                                       BranchControlRow parent, ControlRow child, DataValueDescriptor[] newbranchrow, DataValueDescriptor[] splitrow, int flag) {
        parent.release();
        child.release();
        ControlRow root = null;

        root = ControlRow.getPage(open_btree, BTree.ROOTPAGEID);


        return root.splitFor(open_btree, template, null, newbranchrow, flag);
    }


    public ControlRow search(SearchParameters sp) {
        ControlRow returnResult = null;

        searchForEntry(sp);

        ControlRow childpage = this.getChildPageAtSlot(sp.btree, sp.resultSlot);
        this.release();
        returnResult = childpage.search(sp);
        return returnResult;
    }

    public ControlRow searchLeft(OpenBTree btree) {
        ControlRow childpage = this.getLeftChild(btree);
        this.release();
        return childpage.searchLeft(btree);
    }


    public ControlRow searchRight(OpenBTree btree) {
        ControlRow childpage = this.getRightChild(btree);
        this.release();
        return childpage.searchRight(btree);
    }


    public boolean shrinkFor(OpenBTree open_btree, DataValueDescriptor[] shrink_key) {
        ControlRow childpage = null;
        boolean shrinkme = false;
        try {
            BranchRow branch_template = BranchRow.createEmptyTemplate(open_btree.getRawTran(), open_btree.getConglomerate());
            SearchParameters sp = new SearchParameters(
                    shrink_key,
                    SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH,
                    branch_template.getRow(), open_btree, false);

            this.searchForEntry(sp);
            childpage = this.getChildPageAtSlot(sp.btree, sp.resultSlot);
            if (childpage.shrinkFor(open_btree, shrink_key)) {
                if (sp.resultSlot != 0) {
                    this.page.purgeAtSlot(sp.resultSlot, 1, true);
                } else {
                    if (this.page.recordCount() > 1) {
                        long leftchildpageid = getChildPageIdAtSlot(open_btree, 1);
                        this.setLeftChildPageno(leftchildpageid);
                        this.page.purgeAtSlot(1, 1, true);
                    } else {
                        if (this.getIsRoot()) {
                            LeafControlRow newleafroot = new LeafControlRow(open_btree, this.page, null, true);
                            newleafroot.page.updateAtSlot(0, newleafroot.getRow(), null);
                            newleafroot.release();
                            shrinkme = true;
                        } else {
                            if (this.unlink(open_btree)) {
                                shrinkme = true;
                            }
                        }
                    }
                }
            }
        } finally {
            if (!shrinkme) {
                this.release();
            }
        }
        return shrinkme;
    }

    public long splitFor(OpenBTree open_btree, DataValueDescriptor[] template, BranchControlRow parent, DataValueDescriptor[] splitrow, int flag) {
        ControlRow childpage;
        if ((this.page.recordCount() - 1 >= BTree.maxRowsPerPage) || (!this.page.spaceForInsert(splitrow, (FormatableBitSet) null, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD))) {
            if (this.page.recordCount() == 1) {
                throw new RuntimeException("BTREE_NO_SPACE_FOR_KEY1");
            }

            if (this.getIsRoot()) {
                growRoot(open_btree, template, this);
                parent = (BranchControlRow) ControlRow.getPage(open_btree, BTree.ROOTPAGEID);
                return parent.splitFor(open_btree, template, null, splitrow, flag);
            }

            int splitpoint = (this.page.recordCount() - 1) / 2 + 1;
            if ((flag & ControlRow.SPLIT_FLAG_FIRST_ON_PAGE) != 0) {
                splitpoint = 1;
            } else if ((flag & ControlRow.SPLIT_FLAG_LAST_ON_PAGE) != 0) {
                splitpoint = this.page.recordCount() - 1;
            }
            BranchRow split_branch_row = BranchRow.createEmptyTemplate(open_btree.getRawTran(), open_btree.getConglomerate());

            this.page.fetchFromSlot(null, splitpoint, split_branch_row.getRow(), null, true);
            BranchRow newbranchrow = split_branch_row.createBranchRowFromOldBranchRow(BranchRow.DUMMY_PAGE_NUMBER);

            if (!parent.page.spaceForInsert(newbranchrow.getRow(), (FormatableBitSet) null, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD)) {
                return BranchControlRow.restartSplitFor(open_btree, template, parent, this, newbranchrow.getRow(), splitrow, flag);
            }
            childpage = this.getChildPageAtSlot(open_btree, splitpoint);
            BranchControlRow newbranch = BranchControlRow.allocate(open_btree, childpage, this.getLevel(), parent);
            newbranch.linkRight(open_btree, this);
            childpage.release();
            newbranchrow.setPageNumber(newbranch.page.getPageNumber());

            BranchRow branch_template =
                    BranchRow.createEmptyTemplate(
                            open_btree.getRawTran(),
                            open_btree.getConglomerate());
            SearchParameters sp = new SearchParameters(
                    newbranchrow.getRow(),
                    SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH,
                    branch_template.getRow(),
                    open_btree, false);

            parent.searchForEntry(sp);

            byte insertFlag = Page.INSERT_INITIAL;
            insertFlag |= Page.INSERT_DEFAULT;
            insertFlag |= Page.INSERT_UNDO_WITH_PURGE;
            if (parent.page.insertAtSlot(sp.resultSlot + 1, newbranchrow.getRow(), null, null, insertFlag, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD) == null) {
                throw new RuntimeException("BTREE_NO_SPACE_FOR_KEY1");
            }
            int num_rows_to_move = this.page.recordCount() - (splitpoint + 1);
            if (num_rows_to_move > 0) {
                this.page.copyAndPurge(newbranch.page, splitpoint + 1, num_rows_to_move, 1);
            }
            this.page.purgeAtSlot(splitpoint, 1, true);
            newbranch.fixChildrensParents(open_btree, null);
            BranchControlRow pagetofollow;

            if (compareIndexRowToKey(splitrow,
                    split_branch_row.getRow(),
                    split_branch_row.getRow().length - 1, 0,
                    open_btree.getConglomerate().ascDescInfo) >= 0) {
                // Follow the new branch
                pagetofollow = newbranch;
                this.release();
            } else {
                pagetofollow = this;
                newbranch.release();
            }
            return pagetofollow.splitFor(open_btree, template, parent, splitrow, flag);
        }
        if (parent != null) {
            parent.release();
        }
        BranchRow branch_template = BranchRow.createEmptyTemplate(open_btree.getRawTran(), open_btree.getConglomerate());
        SearchParameters sp = new SearchParameters(
                splitrow, SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH,
                branch_template.getRow(), open_btree, false);

        searchForEntry(sp);

        childpage = this.getChildPageAtSlot(open_btree, sp.resultSlot);

        return (childpage.splitFor(open_btree, template, this, splitrow, flag));
    }

    @Override
    public void printTree(OpenBTree btree) {

    }

    private static void growRoot(OpenBTree open_btree, DataValueDescriptor[] template, BranchControlRow root) {
        ControlRow leftchild = null;
        BranchControlRow branch = null;
        try {
            leftchild = root.getLeftChild(open_btree);
            branch = BranchControlRow.allocate(open_btree, leftchild, root.getLevel(), root);
            root.page.copyAndPurge(branch.page, 1, root.page.recordCount() - 1, 1);
            root.setLeftChild(branch);
            root.setLevel(root.getLevel() + 1);
            branch.fixChildrensParents(open_btree, leftchild);
        } finally {
            root.release();
            if (branch != null) {
                branch.release();
            }
            if (leftchild != null) {
                leftchild.release();
            }
        }
        return;
    }


    private static BranchControlRow allocate(OpenBTree open_btree, ControlRow leftchild, int level, ControlRow parent) {
        Page page = open_btree.container.addPage();
        BranchControlRow control_row = new BranchControlRow(open_btree, page, level,
                parent, false, leftchild.page.getPageNumber());
        byte insertFlag = Page.INSERT_INITIAL;
        insertFlag |= Page.INSERT_DEFAULT;
        page.insertAtSlot(Page.FIRST_SLOT_NUMBER, control_row.getRow(), null, null,
                insertFlag, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD);
        return control_row;
    }

    protected void setLeftChildPageno(long leftchild_pageno) {
        if (left_child_page == null) {
            left_child_page = new SQLLongint(leftchild_pageno);
        } else {
            this.left_child_page.setValue(leftchild_pageno);
        }
        this.page.updateFieldAtSlot(CR_SLOT, CR_LEFTCHILD, this.left_child_page);
    }

    protected void setLeftChild(ControlRow leftchild) {
        this.setLeftChildPageno(leftchild.page.getPageNumber());
    }

    private void fixChildrensParents(OpenBTree btree, ControlRow leftchild) {
        ControlRow child = null;
        try {
            if (leftchild == null) {
                child = this.getLeftChild(btree);
                child.setParent(this.page.getPageNumber());
                child.release();
                child = null;
            } else {
                leftchild.setParent(this.page.getPageNumber());
            }

            int numslots = this.page.recordCount();
            for (int slot = 1; slot < numslots; slot++) {
                child = getChildPageAtSlot(btree, slot);
                child.setParent(this.page.getPageNumber());
                child.release();
                child = null;
            }
        } finally {
            if (child != null)
                child.release();
        }
    }

    private long getChildPageIdAtSlot(OpenBTree btree, int slot) {
        long child_page_id;
        if (slot == 0) {
            child_page_id = this.getLeftChildPageno();
        } else {
            this.page.fetchFieldFromSlot(slot, btree.getConglomerate().nKeyFields, child_pageno_buf);
            child_page_id = child_pageno_buf.getLong();
        }
        return child_page_id;
    }

    public ControlRow getChildPageAtSlot(OpenBTree open_btree, int slot) {
        ControlRow child_control_row;

        if (slot == 0) {
            child_control_row = this.getLeftChild(open_btree);
        } else {
            this.page.fetchFieldFromSlot(slot, open_btree.getConglomerate().nKeyFields, child_pageno_buf);
            child_control_row = ControlRow.getPage(open_btree, child_pageno_buf.getLong());
        }
        return child_control_row;
    }

    public ControlRow getLeftChild(OpenBTree open_btree) {
        return ControlRow.getPage(open_btree, this.getLeftChildPageno());
    }

    public ControlRow getRightChild(OpenBTree open_btree) {
        ControlRow right_child;
        int num_slots = this.page.recordCount();
        if (num_slots == 1) {
            right_child = ControlRow.getPage(open_btree, this.getLeftChildPageno());
        } else {
            right_child = getChildPageAtSlot(open_btree, (num_slots - 1));
        }
        return right_child;
    }

    long getLeftChildPageno() {
        if (this.left_child_page == null) {
            this.left_child_page = new SQLLongint();
            scratch_row[CR_LEFTCHILD] = this.left_child_page;
            fetchDesc.setValidColumns(CR_LEFTCHILD_BITMAP);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return (left_child_page.getLong());
    }

    public int getTypeFormatId() {
        return StoredFormatIds.ACCESS_BTREE_BRANCHCONTROLROW_V1_ID;
    }


    public DataValueDescriptor[] getRowTemplate(OpenBTree open_btree) {
        return BranchRow.createEmptyTemplate(open_btree.getRawTran(), open_btree.getConglomerate()).getRow();
    }

    @Override
    public String toString() {
        return "BranchControlRow{" +
                "left_child_page=" + left_child_page +
                ", child_pageno_buf=" + child_pageno_buf +
                ", page=" + page +
                ", row=" + Arrays.toString(row) +
                ", scratch_row=" + Arrays.toString(scratch_row) +
                ", fetchDesc=" + fetchDesc +
                ", use_last_search_result_hint=" + use_last_search_result_hint +
                ", last_search_result=" + last_search_result +
                '}';
    }
}
