package com.cfs.sqlkv.btree.controlrow;

import com.cfs.sqlkv.btree.*;

import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.AccessFactoryGlobals;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-03 15:56
 */
public class LeafControlRow extends ControlRow {

    public LeafControlRow() {
    }

    @Override
    public int checkConsistency(OpenBTree btree, ControlRow parent, boolean check_other_pages)   {
        return 0;
    }

    public LeafControlRow(OpenBTree btree, Page page, ControlRow parent, boolean isRoot)   {
        super(btree, page, 0, parent, isRoot);
    }

    /**
     * 分配一个Leaf Node
     */
    private static LeafControlRow allocate(OpenBTree btree, ControlRow parent)   {
        Page page = btree.container.addPage();
        LeafControlRow control_row = new LeafControlRow(btree, page, parent, false);
        byte insertFlag = Page.INSERT_INITIAL;
        insertFlag |= Page.INSERT_DEFAULT;
        RecordId rh = page.insertAtSlot(Page.FIRST_SLOT_NUMBER, control_row.getRow(), null, null, insertFlag,
                AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD);
        return control_row;
    }

    /**
     * 返回开始槽位左边未删除的行数
     */
    private float get_left_nondeleted_rowcnt(int startslot)   {
        int non_deleted_row_count = 0;
        for (int slot = 1; slot <= startslot; slot++) {
            if (!this.page.isDeletedAtSlot(slot)) {
                non_deleted_row_count++;
            }
        }
        return (non_deleted_row_count);
    }

    @Override
    public final void controlRowInit() {
    }

    /**
     * 创建一棵空的二叉树
     */
    public static void initEmptyBtree(OpenBTree open_btree)   {
        Page page = open_btree.container.getPage(BaseContainerHandle.FIRST_PAGE_NUMBER);
        LeafControlRow control_row = new LeafControlRow(open_btree, page, null, true);
        byte insertFlag = Page.INSERT_INITIAL;
        insertFlag |= Page.INSERT_DEFAULT;
        RecordId rh = page.insertAtSlot(Page.FIRST_SLOT_NUMBER, control_row.row, null, null, insertFlag, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD);
        page.unlatch();
        return;
    }

    /**
     * 返回控制行的列数
     */
    @Override
    public final int getNumberOfControlRowColumns() {
        return this.CR_NCOLUMNS;
    }

    /**
     * 判断当前节点是否是最左边的叶子节点
     */
    @Override
    public boolean isLeftmostLeaf()   {
        return getleftSiblingPageNumber() == BaseContainerHandle.INVALID_PAGE_NUMBER;
    }

    /**
     * 判断当前节点是否是最右边的叶子节点
     */
    @Override
    public boolean isRightmostLeaf()   {
        return getrightSiblingPageNumber() == BaseContainerHandle.INVALID_PAGE_NUMBER;
    }

    @Override
    public ControlRow search(SearchParameters sp)   {
        searchForEntry(sp);
        return this;
    }

    @Override
    public ControlRow searchLeft(OpenBTree btree)   {
        return this;
    }

    @Override
    public ControlRow searchRight(OpenBTree btree)   {
        return this;
    }

    /**
     *
     */
    public boolean shrinkFor(OpenBTree btree, DataValueDescriptor[] key)   {
        boolean shrink_me = false;
        try {
            if ((this.page.recordCount() == 1) && !getIsRoot()) {
                shrink_me = unlink(btree);
            }
        } finally {
            if (!shrink_me) {
                this.release();
            }
        }
        return shrink_me;
    }

    /**
     * 进行页分裂
     *
     * @param flag 决定选择的分裂点
     */
    public long splitFor(OpenBTree open_btree, DataValueDescriptor[] template, BranchControlRow parent_page,
                            DataValueDescriptor[] splitrow, int flag)   {
        //获取当前页页号
        long current_leaf_pageno = this.page.getPageNumber();

        boolean HaveRecordCount = this.page.recordCount() - 1 < BTree.maxRowsPerPage;

        boolean HaveSpaceForInsert = this.page.spaceForInsert(splitrow, null, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD);

        if (HaveRecordCount && HaveSpaceForInsert) {
            return current_leaf_pageno;
        }

        //判断当前叶子节点是否是根节点,如果是则需要将其设置为
        if (this.getIsRoot()) {
            growRoot(open_btree, template, this);
            //因为原有叶子节点是根节点 则只要一层,将其增长之后其Level是1即BTree.ROOTPAGEID
            ControlRow new_root = ControlRow.getPage(open_btree, BTree.ROOTPAGEID);
            //此时new_root是一个BranchControlRow,根据其进行分裂,基于其分裂后直接返回
            return new_root.splitFor(open_btree, template, null, splitrow, flag);

        }

        //对当前叶子节点进行分裂
        //找到中间的位置
        int splitpoint = (this.page.recordCount() - 1) / 2 + 1;
        if ((flag & ControlRow.SPLIT_FLAG_FIRST_ON_PAGE) != 0) {
            splitpoint = 1;
        } else if ((flag & ControlRow.SPLIT_FLAG_LAST_ON_PAGE) != 0) {
            splitpoint = this.page.recordCount() - 1;
        }

        DataValueDescriptor[] split_leaf_row = open_btree.getConglomerate().createTemplate(open_btree.getRawTran());
        this.page.fetchFromSlot(null, splitpoint, split_leaf_row, null, true);
        BranchRow branchrow = BranchRow.createBranchRowFromOldLeafRow(split_leaf_row, BranchRow.DUMMY_PAGE_NUMBER);
        if (!parent_page.page.spaceForInsert(branchrow.getRow(), null, AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD)) {
            return BranchControlRow.restartSplitFor(open_btree, template, parent_page, this,
                    branchrow.getRow(), splitrow, flag);

        }
        LeafControlRow newleaf = LeafControlRow.allocate(open_btree, parent_page);
        branchrow.setPageNumber(newleaf.page.getPageNumber());
        newleaf.linkRight(open_btree, this);
        int num_rows_to_move = this.page.recordCount() - splitpoint;
        if (num_rows_to_move != 0) {
            this.page.copyAndPurge(newleaf.page, splitpoint, num_rows_to_move, 1);
        }

        BranchRow branch_template = BranchRow.createEmptyTemplate(open_btree.getRawTran(), open_btree.getConglomerate());

        SearchParameters sp = new SearchParameters(branchrow.getRow(),
                SearchParameters.POSITION_LEFT_OF_PARTIAL_KEY_MATCH, branch_template.getRow(), open_btree, false);
        parent_page.searchForEntry(sp);
        byte insertFlag = Page.INSERT_INITIAL;
        insertFlag |= Page.INSERT_DEFAULT;
        insertFlag |= Page.INSERT_UNDO_WITH_PURGE;
        if (parent_page.page.insertAtSlot(
                sp.resultSlot + 1,
                branchrow.getRow(),
                null,
                null,
                insertFlag,
                AccessFactoryGlobals.BTREE_OVERFLOW_THRESHOLD) == null) {

            throw new RuntimeException("BTREE_NO_SPACE_FOR_KEY");
        }

        page.setRepositionNeeded();
        parent_page.release();
        this.release();  // XXX (nat) Not good form to unlatch self.

        long new_leaf_pageno = newleaf.page.getPageNumber();
        newleaf.release();
        return new_leaf_pageno;
    }


    private static void growRoot(OpenBTree open_btree, DataValueDescriptor[] template, LeafControlRow leafroot)   {
        //基于存在的leafroot分配一个新的叶子节点
        LeafControlRow newleaf = LeafControlRow.allocate(open_btree, leafroot);

        //将原有叶子节点的内容拷贝到新的叶子节点
        leafroot.page.copyAndPurge(newleaf.page, 1, leafroot.page.recordCount() - 1, 1);

        //创建一个分支控制行,将原来的LeafControlRow给封装,因为存在叶子节点,则其不再是Leaf
        BranchControlRow branchroot = new BranchControlRow(open_btree, leafroot.page, 1, null, true, newleaf.page.getPageNumber());
        leafroot = null;

        //更新控制行信息到页面
        branchroot.page.updateAtSlot(0, branchroot.getRow(), null);

        branchroot.page.setRepositionNeeded();

        if (branchroot != null) {
            branchroot.release();
        }
        if (leafroot != null) {
            leafroot.release();
        }
        if (newleaf != null) {
            newleaf.release();
        }
    }

    public ControlRow getLeftChild(OpenBTree btree) {
        return null;
    }

    public ControlRow getRightChild(OpenBTree btree) {
        return null;
    }

    public void printTree(OpenBTree btree)   {
    }

    public int getTypeFormatId() {
        return StoredFormatIds.ACCESS_BTREE_LEAFCONTROLROW_V1_ID;
    }

}
