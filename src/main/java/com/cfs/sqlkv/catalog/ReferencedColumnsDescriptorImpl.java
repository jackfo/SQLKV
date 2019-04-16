package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.service.io.ArrayUtil;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.io.StoredFormatIds;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 12:19
 */
public class ReferencedColumnsDescriptorImpl implements Formatable {

    private int[] referencedColumns;

    private int[] referencedColumnsInTriggerAction;

    public ReferencedColumnsDescriptorImpl(int[] referencedColumns) {
        this.referencedColumns = ArrayUtil.copy(referencedColumns);
    }

    public ReferencedColumnsDescriptorImpl(int[] referencedColumns, int[] referencedColumnsInTriggerAction) {
        this.referencedColumns = ArrayUtil.copy(referencedColumns);
        this.referencedColumnsInTriggerAction = ArrayUtil.copy(referencedColumnsInTriggerAction);
    }

    public ReferencedColumnsDescriptorImpl() {
    }

    public int[] getReferencedColumnPositions() {
        return ArrayUtil.copy(referencedColumns);
    }

    public int[] getTriggerActionReferencedColumnPositions() {
        return ArrayUtil.copy(referencedColumnsInTriggerAction);
    }

    public void readExternal(ObjectInput in) throws IOException {
        int rcLength = in.readInt();
        referencedColumns = new int[rcLength];
        for (int i = 0; i < rcLength; i++) {
            referencedColumns[i] = in.readInt();
        }

    }


    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(referencedColumns.length);
        for (int i = 0; i < referencedColumns.length; i++) {
            out.writeInt(referencedColumns[i]);
        }
    }

    public int getTypeFormatId() {
        return StoredFormatIds.REFERENCED_COLUMNS_DESCRIPTOR_IMPL_V01_ID;
    }


    public String toString() {
        if (referencedColumns == null)
            return "NULL";
        StringBuffer sb = new StringBuffer(60);
        sb.append('(');
        for (int index = 0; index < referencedColumns.length; index++) {
            if (index > 0)
                sb.append(',');
            sb.append(referencedColumns[index]);
        }
        sb.append(')');
        return sb.toString();
    }

}
