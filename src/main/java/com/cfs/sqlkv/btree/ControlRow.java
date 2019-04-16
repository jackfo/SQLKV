package com.cfs.sqlkv.btree;


import com.cfs.sqlkv.btree.controlrow.BranchControlRow;
import com.cfs.sqlkv.btree.controlrow.LeafControlRow;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.service.io.TypedFormat;
import com.cfs.sqlkv.sql.types.SQLLongint;
import com.cfs.sqlkv.store.access.AccessFactoryGlobals;
import com.cfs.sqlkv.store.access.StorableFormatId;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 16:36
 */
public abstract class ControlRow implements TypedFormat {

    /**
     * 判定当前控制行是分支页的控制行还是叶子页的控制行
     */
    private StorableFormatId version = null;


    /**
     * 指向同级左边的页号
     */
    private SQLLongint leftSiblingPageNumber;

    /**
     * 指向同级右边的页号
     */
    private SQLLongint rightSiblingPageNumber;

    /**
     * 指向父级页
     */
    private SQLLongint parentPageNumber;

    /**
     * 当前节点在BTree的深度 leaf的level为0  第一个分支Level(leaf的父页)为1 一次类推
     */
    private SQLLongint level;

    /**
     * 判定该节点是否是根节点 1表示是根节点
     */
    private SQLLongint isRoot = null;

    /**
     * 当前控制行所在的BTree的句柄
     */
    private BTree btree = null;

    /**
     * 当前控制行所对应的页面
     */
    public Page page;

    /**
     * 当前控制行页的描述
     */
    protected DataValueDescriptor row[];

    /***/
    protected DataValueDescriptor[] scratch_row;
    protected FetchDescriptor fetchDesc;

    transient protected boolean use_last_search_result_hint = false;
    transient protected int last_search_result = 0;

    protected static final int CR_COLID_FIRST = 0;

    /**
     * 记录版本的游标
     */
    protected static final int CR_VERSION_COLID = CR_COLID_FIRST + 0;

    /**
     * 记录左页号游标
     */
    protected static final int CR_LEFTSIB_COLID = CR_COLID_FIRST + 1;
    /**
     * 记录右页号的游标
     */
    protected static final int CR_RIGHTSIB_COLID = CR_COLID_FIRST + 2;
    /**
     * 记录父页号的游标
     */
    protected static final int CR_PARENT_COLID = CR_COLID_FIRST + 3;
    /**
     * 记录当前控制行在BTree深度的游标
     */
    protected static final int CR_LEVEL_COLID = CR_COLID_FIRST + 4;
    /**
     * 记录当前控制行是否是根节点的游标
     */
    protected static final int CR_ISROOT_COLID = CR_COLID_FIRST + 5;
    /**
     * 记录当前控制行BTree句柄的游标
     */
    protected static final int CR_CONGLOM_COLID = CR_COLID_FIRST + 6;

    protected static final int CR_COLID_LAST = CR_CONGLOM_COLID;

    /**
     * 控制行描述的数目
     */
    protected static final int CR_NCOLUMNS = CR_COLID_LAST + 1;

    protected static final FormatableBitSet CR_VERSION_BITSET = new FormatableBitSet(CR_VERSION_COLID + 1);
    protected static final FormatableBitSet CR_LEFTSIB_BITSET = new FormatableBitSet(CR_LEFTSIB_COLID + 1);
    protected static final FormatableBitSet CR_RIGHTSIB_BITSET = new FormatableBitSet(CR_RIGHTSIB_COLID + 1);
    protected static final FormatableBitSet CR_PARENT_BITSET = new FormatableBitSet(CR_PARENT_COLID + 1);
    protected static final FormatableBitSet CR_LEVEL_BITSET = new FormatableBitSet(CR_LEVEL_COLID + 1);
    protected static final FormatableBitSet CR_ISROOT_BITSET = new FormatableBitSet(CR_ISROOT_COLID + 1);
    protected static final FormatableBitSet CR_CONGLOM_BITSET = new FormatableBitSet(CR_CONGLOM_COLID + 1);


    public static final int SPLIT_FLAG_LAST_ON_PAGE = 0x000000001;
    public static final int SPLIT_FLAG_LAST_IN_TABLE = 0x000000002;
    public static final int SPLIT_FLAG_FIRST_ON_PAGE = 0x000000004;
    public static final int SPLIT_FLAG_FIRST_IN_TABLE = 0x000000008;


    protected static final int CR_SLOT = 0;

    static {
        CR_VERSION_BITSET.set(CR_VERSION_COLID);
        CR_LEFTSIB_BITSET.set(CR_LEFTSIB_COLID);
        CR_RIGHTSIB_BITSET.set(CR_RIGHTSIB_COLID);
        CR_PARENT_BITSET.set(CR_PARENT_COLID);
        CR_LEVEL_BITSET.set(CR_LEVEL_COLID);
        CR_ISROOT_BITSET.set(CR_ISROOT_COLID);
        CR_CONGLOM_BITSET.set(CR_CONGLOM_COLID);
    }

    protected ControlRow() {
        this.scratch_row = new DataValueDescriptor[getNumberOfControlRowColumns()];
        this.fetchDesc = new FetchDescriptor(this.scratch_row.length, null, null);
        this.version = new StorableFormatId(getTypeFormatId());
    }

    /**
     * 创建新的控制行
     *
     * @param bTree  btree的静态信息
     * @param page   当前控制行的页描述
     * @param parent 当前页的父级控制行
     * @param isRoot 判断当前页是否是Btree的根节点
     */
    protected ControlRow(OpenBTree bTree, Page page, int level, ControlRow parent, boolean isRoot) {
        this.page = page;
        leftSiblingPageNumber = new SQLLongint(BaseContainerHandle.INVALID_PAGE_NUMBER);
        rightSiblingPageNumber = new SQLLongint(BaseContainerHandle.INVALID_PAGE_NUMBER);
        parentPageNumber = new SQLLongint((parent == null ? BaseContainerHandle.INVALID_PAGE_NUMBER : parent.page.getPageNumber()));
        this.isRoot = new SQLLongint(isRoot ? 1 : 0);
        this.level = new SQLLongint(level);
        this.version = new StorableFormatId(getTypeFormatId());
        if (isRoot) {
            this.btree = bTree.getConglomerate();
        } else {
            this.btree = new BTree();
        }
        this.row = new DataValueDescriptor[getNumberOfControlRowColumns()];
        this.row[CR_VERSION_COLID] = this.version;
        this.row[CR_LEFTSIB_COLID] = this.leftSiblingPageNumber;
        this.row[CR_RIGHTSIB_COLID] = this.rightSiblingPageNumber;
        this.row[CR_PARENT_COLID] = this.parentPageNumber;
        this.row[CR_LEVEL_COLID] = this.level;
        this.row[CR_ISROOT_COLID] = this.isRoot;
        this.row[CR_CONGLOM_COLID] = this.btree;

        page.setControlRow(this);
    }


    protected ControlRow(Page page) {
        this.page = page;
    }

    /**
     * 获取当前控制行的版本,如果版本为空,设置获取行从页面第0个槽位去获取数据
     */
    protected int getVersion() {
        if (this.version == null) {
            this.version = new StorableFormatId();
            scratch_row[CR_VERSION_COLID] = this.version;
            fetchDesc.setValidColumns(CR_VERSION_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return this.version.getValue();
    }

    protected void setVersion(int version) {
        if (this.version == null) {
            this.version = new StorableFormatId();
        }
        this.page.updateFieldAtSlot(CR_SLOT, CR_VERSION_COLID, this.version);
    }


    /**
     * 获取左边的控制行
     * 首先获取页号,之后根据页号获取对应的控制行
     */
    public ControlRow getLeftSibling(OpenBTree btree) {
        ControlRow controlRow;
        long pageNumber = this.getleftSiblingPageNumber();
        if (pageNumber == BaseContainerHandle.INVALID_PAGE_NUMBER) {
            return null;
        }
        controlRow = ControlRow.getPage(btree, pageNumber);
        return controlRow;
    }

    protected void setLeftSibling(ControlRow leftSiblingControlRow) {
        long left_sibling_pageno;
        if (leftSiblingControlRow == null) {
            left_sibling_pageno = BaseContainerHandle.INVALID_PAGE_NUMBER;
        } else {
            left_sibling_pageno = leftSiblingControlRow.page.getPageNumber();
        }
        if (leftSiblingPageNumber == null) {
            leftSiblingPageNumber = new SQLLongint(left_sibling_pageno);
        } else {
            this.leftSiblingPageNumber.setValue(left_sibling_pageno);
        }


        this.page.updateFieldAtSlot(CR_SLOT, CR_LEFTSIB_COLID, this.leftSiblingPageNumber);

    }

    /**
     * 根据BTree获取右边的控制行
     */
    protected ControlRow getRightSibling(OpenBTree open_btree) {
        long pageNumber = this.getrightSiblingPageNumber();
        if (pageNumber == BaseContainerHandle.INVALID_PAGE_NUMBER) {
            return null;
        } else {
            return ControlRow.getPage(open_btree, pageNumber);
        }
    }

    protected void setRightSibling(ControlRow rightSibling) {
        long right_sibling_pageno;
        if (rightSibling == null) {
            right_sibling_pageno = BaseContainerHandle.INVALID_PAGE_NUMBER;
        } else {
            right_sibling_pageno = rightSibling.page.getPageNumber();
        }
        if (rightSiblingPageNumber == null) {
            rightSiblingPageNumber = new SQLLongint(right_sibling_pageno);
        } else {
            this.rightSiblingPageNumber.setValue(right_sibling_pageno);
        }

        this.page.updateFieldAtSlot(CR_SLOT, CR_RIGHTSIB_COLID, this.rightSiblingPageNumber);

    }


    /**
     * 获取左边的页号
     */
    public long getleftSiblingPageNumber() {
        if (this.leftSiblingPageNumber == null) {
            this.leftSiblingPageNumber = new SQLLongint();
            scratch_row[CR_LEFTSIB_COLID] = this.leftSiblingPageNumber;
            fetchDesc.setValidColumns(CR_LEFTSIB_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return leftSiblingPageNumber.getLong();
    }


    public long getrightSiblingPageNumber() {
        if (this.rightSiblingPageNumber == null) {
            this.rightSiblingPageNumber = new SQLLongint();
            scratch_row[CR_RIGHTSIB_COLID] = this.rightSiblingPageNumber;
            fetchDesc.setValidColumns(CR_RIGHTSIB_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return rightSiblingPageNumber.getLong();
    }

    /**
     * 获取父级页号
     */
    public long getParentPageNumber() {
        if (this.parentPageNumber == null) {
            this.parentPageNumber = new SQLLongint();
            scratch_row[CR_PARENT_COLID] = this.parentPageNumber;
            fetchDesc.setValidColumns(CR_PARENT_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        long pageno = parentPageNumber.getLong();
        return pageno;
    }

    public void setParent(long parent) {
        if (parentPageNumber == null) {
            parentPageNumber = new SQLLongint();
        }
        this.parentPageNumber.setValue(parent);

        this.page.updateFieldAtSlot(CR_SLOT, CR_PARENT_COLID, this.parentPageNumber);

        return;
    }

    protected int getLevel() {
        if (this.level == null) {
            this.level = new SQLLongint();
            scratch_row[CR_LEVEL_COLID] = this.level;
            fetchDesc.setValidColumns(CR_LEVEL_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return (int) this.level.getLong();
    }

    protected void setLevel(int newlevel) {
        if (this.level == null) {
            this.level = new SQLLongint();
        }
        this.level.setValue((long) newlevel);
        this.page.updateFieldAtSlot(CR_SLOT, CR_LEVEL_COLID, this.level);
    }


    /**
     * 判断当前节点是否是根节点
     */
    protected boolean getIsRoot() {
        if (this.isRoot == null) {
            this.isRoot = new SQLLongint();
            scratch_row[CR_ISROOT_COLID] = this.isRoot;
            fetchDesc.setValidColumns(CR_ISROOT_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return this.isRoot.getLong() == 1;
    }

    public BTree getConglom(int format_id) {
        if (this.btree == null) {
            this.btree = new BTree();
            scratch_row[CR_CONGLOM_COLID] = this.btree;
            fetchDesc.setValidColumns(CR_CONGLOM_BITSET);
            this.page.fetchFromSlot(null, CR_SLOT, scratch_row, fetchDesc, false);
        }
        return this.btree;
    }


    /**
     * 根据页号获取控制行
     */
    public static ControlRow getPage(OpenBTree open_btree, long pageNumber) {
        Page page = open_btree.container.getPage(pageNumber);
        if (page == null) {
            return null;
        }
        return getControlRowForPage(page);
    }

    public static ControlRow getPage(BaseContainerHandle containerHandle, long pageNumber) {
        Page page = containerHandle.getPage(pageNumber);
        if (page == null) {
            return null;
        }
        return getControlRowForPage(page);
    }

    protected static ControlRow getControlRowForPage(Page page) {
        ControlRow controlRow = page.getControlRow();
        if (controlRow != null) {
            return controlRow;
        }
        StorableFormatId version = new StorableFormatId();
        DataValueDescriptor[] version_ret = new DataValueDescriptor[1];
        version_ret[0] = version;
        FetchDescriptor fetchDescriptor = new FetchDescriptor(1, CR_VERSION_BITSET, null);
        page.fetchFromSlot(null, CR_SLOT, version_ret, fetchDescriptor, false);
        //根据版本号创建对应的控制行
        switch (version.getValue()) {
            case StoredFormatIds.ACCESS_BTREE_LEAFCONTROLROW_V1_ID:
                controlRow = new LeafControlRow();
                break;
            case StoredFormatIds.ACCESS_BTREE_BRANCHCONTROLROW_V1_ID:
                controlRow = new BranchControlRow();
                break;
            default:
                throw new RuntimeException("current version isn't existed!");
        }
        controlRow.page = page;
        controlRow.controlRowInit();
        page.setControlRow(controlRow);
        return controlRow;
    }

    public void release() {
        if (page != null) {
            page.unlatch();
        }
    }


    /**
     * 查找索引页
     * 通过二分查找的方式 将结果注入last_search_result
     */
    public void searchForEntry(SearchParameters params) {
        //获取页面的记录范围
        int leftrange = 1;
        int rightrange = page.recordCount() - 1;

        int leftslot = 0;
        int rightslot = rightrange + 1;

        int midslot;
        int compare_ret;

        //获取最后查找结果的槽位
        if (this.use_last_search_result_hint) {
            if (this.last_search_result == 0) {
                midslot = 1;
            } else {
                midslot = this.last_search_result;
            }
            if (midslot > rightrange) {
                midslot = rightrange;
            }
        } else {
            midslot = (leftrange + rightrange) / 2;
        }
        //开始进行二分比较
        while (leftslot != (rightslot - 1)) {

            compare_ret = compareIndexRowFromPageToKey(this, midslot,
                    params.template, params.searchKey,
                    params.btree.getConglomerate().nUniqueColumns,
                    params.partial_key_match_op,
                    params.btree.getConglomerate().ascDescInfo);

            if (compare_ret == 0) {
                params.resultSlot = midslot;
                params.resultExact = true;
                use_last_search_result_hint = (midslot == this.last_search_result) ? true : false;
                this.last_search_result = midslot;
                return;
            } else if (compare_ret > 0) {
                rightslot = midslot;
                rightrange = midslot - 1;
            } else {
                leftslot = midslot;
                leftrange = midslot + 1;
            }
            midslot = (leftrange + rightrange) / 2;
        }
        this.use_last_search_result_hint = (leftslot == this.last_search_result);
        this.last_search_result = leftslot;
        params.resultSlot = leftslot;
        params.resultExact = false;
        return;
    }


    /**
     * 将所有的索引键进行比较
     *
     * @param indexpage 被比较索引所在的页
     * @param indexrow  装载页上索引值的数据值描述
     * @param key       比较索引的数据描述
     */
    public static int compareIndexRowFromPageToKey(ControlRow indexpage,
                                                   int slot, DataValueDescriptor[] indexrow, DataValueDescriptor[] key,
                                                   int nCompareCols, int partialKeyOrder, boolean[] ascOrDesc) {
        int partialKeyCols = key.length;
        indexpage.page.fetchFromSlot(null, slot, indexrow, null, true);
        for (int i = 0; i < nCompareCols; i++) {
            if (i >= partialKeyCols) {
                return partialKeyOrder;
            }
            int r = indexrow[i].compare(key[i]);
            if (r != 0) {
                if (ascOrDesc[i]) {
                    return r;
                } else {
                    return -r;
                }
            }
        }
        return 0;
    }


    public static int compareIndexRowToKey(DataValueDescriptor[] indexrow,
                                           DataValueDescriptor[] key, int nCompareCols, int partialKeyOrder,
                                           boolean[] ascOrDesc) {

        int partialKeyCols = key.length;
        for (int i = 0; i < nCompareCols; i++) {
            if (i >= partialKeyCols) {
                return partialKeyOrder;
            }
            DataValueDescriptor indexcol = indexrow[i];
            DataValueDescriptor keycol = key[i];
            int r = indexcol.compare(keycol);
            if (r != 0) {
                if (ascOrDesc[i]) {
                    return r;
                } else {
                    return -r;
                }
            }
        }
        return 0;
    }


    /**
     * 将当前行插入到目标行的右边
     *
     * @param target 目标行
     */
    public void linkRight(OpenBTree btree, ControlRow target) {
        ControlRow rightSibling = null;
        try {
            rightSibling = target.getRightSibling(btree);
            this.setRightSibling(rightSibling);
            this.setLeftSibling(target);
            if (rightSibling != null) {
                rightSibling.setLeftSibling(this);
            }
            target.setRightSibling(this);
        } finally {
            if (rightSibling != null) {
                rightSibling.release();
            }
        }
    }

    /**
     * 解除当前行在BTree上的链接
     */
    public boolean unlink(OpenBTree btree) {
        ControlRow leftsib = null;
        ControlRow rightsib = null;
        try {
            leftsib = this.getLeftSibling(btree);
            rightsib = this.getRightSibling(btree);
            if (leftsib != null) {
                leftsib.setRightSibling(rightsib);
            }
            if (rightsib != null) {
                rightsib.setLeftSibling(leftsib);
            }
            btree.container.removePage(this.page);
            return true;
        } finally {
            if (leftsib != null) {
                leftsib.release();
            }
            if (rightsib != null) {
                rightsib.release();
            }
        }
    }

    public Page getPage() {
        return page;
    }

    /**
     * 获取当前控制行的数据描述
     */
    public final DataValueDescriptor[] getRow() {
        return row;
    }

    abstract public int checkConsistency(OpenBTree btree, ControlRow parent, boolean check_other_pages);

    public abstract ControlRow getLeftChild(OpenBTree btree);

    public abstract ControlRow getRightChild(OpenBTree btree);

    public abstract void controlRowInit();

    public abstract boolean isLeftmostLeaf();

    public abstract boolean isRightmostLeaf();

    public abstract ControlRow search(SearchParameters search_params);

    public abstract int getNumberOfControlRowColumns();

    public abstract ControlRow searchLeft(OpenBTree btree);

    public abstract ControlRow searchRight(OpenBTree btree);

    public abstract boolean shrinkFor(OpenBTree btree, DataValueDescriptor[] key);

    public abstract long splitFor(OpenBTree open_btree, DataValueDescriptor[] template, BranchControlRow parentpage, DataValueDescriptor[] row, int flag);

    public abstract void printTree(OpenBTree btree);

    public DataValueDescriptor[] getRowTemplate(OpenBTree open_btree) {
        return open_btree.getConglomerate().createTemplate(open_btree.getRawTran());
    }


    public void controlRowInvalidated() {
        version = null;
        leftSiblingPageNumber = null;
        rightSiblingPageNumber = null;
        parentPageNumber = null;
        level = null;
        isRoot = null;
        page = null;
    }

}
