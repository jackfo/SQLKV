package com.cfs.sqlkv.btree;

import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.sql.types.SQLLongint;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Arrays;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-03 18:13
 */
public class BranchRow {

    public static final long DUMMY_PAGE_NUMBER = 0xffffffffffffffffL;
    private DataValueDescriptor[] branchrow = null;

    private BranchRow() {
    }

    private BranchRow(Transaction rawtran, BTree btree)   {
        SQLLongint child_page = new SQLLongint(BaseContainerHandle.INVALID_PAGE_NUMBER);
        branchrow = btree.createBranchTemplate(rawtran, child_page);
    }

    private SQLLongint getChildPage() {
        return (SQLLongint) branchrow[branchrow.length - 1];
    }

    public static BranchRow createEmptyTemplate(Transaction rawtran, BTree btree)   {
        return new BranchRow(rawtran, btree);
    }

    public BranchRow createBranchRowFromOldBranchRow(long childpageno) {
        BranchRow newbranch = new BranchRow();
        newbranch.branchrow = new DataValueDescriptor[this.branchrow.length];
        System.arraycopy(this.branchrow, 0, newbranch.branchrow, 0, newbranch.branchrow.length - 1);
        newbranch.branchrow[newbranch.branchrow.length - 1] = new SQLLongint(childpageno);
        return newbranch;
    }

    public static BranchRow createBranchRowFromOldLeafRow(DataValueDescriptor[] leafrow, long childpageno) {
        BranchRow newbranch = new BranchRow();
        newbranch.branchrow = new DataValueDescriptor[leafrow.length + 1];
        System.arraycopy(leafrow, 0, newbranch.branchrow, 0, leafrow.length);
        newbranch.branchrow[newbranch.branchrow.length - 1] = new SQLLongint(childpageno);
        return newbranch;
    }

    public DataValueDescriptor[] getRow() {
        return this.branchrow;
    }

    public void setPageNumber(long page_number) {
        getChildPage().setValue(page_number);
    }


    @Override
    public String toString() {
        return "BranchRow{" +
                "branchrow=" + Arrays.toString(branchrow) +
                '}';
    }
}
