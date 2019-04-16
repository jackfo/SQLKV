package com.cfs.sqlkv.store.access.heap;


import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.CompressedNumber;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.RecordId;
import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 13:04
 */
public class TableRowLocation extends DataType implements DataValueDescriptor {

    private long pageno;
    private int recid;
    private RecordId recordId;

    @Override
    public void readExternalFromArray(ArrayInputStream ais) throws IOException, ClassNotFoundException {
        this.pageno = ais.readCompressedLong();
        this.recid = ais.readCompressedInt();
        recordId = null;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public void restoreToNull() {

    }

    @Override
    public String getString()   {
        return toString();
    }

    @Override
    public DataValueDescriptor getNewNull() {
        return new TableRowLocation();
    }

    @Override
    public Object getObject()   {
        return this;
    }

    @Override
    public int compare(DataValueDescriptor other)   {
        TableRowLocation tableRowLocation = (TableRowLocation) other;
        long myPage = this.pageno;
        long otherPage = tableRowLocation.pageno;
        if (myPage < otherPage) {
            return -1;
        } else if (myPage > otherPage) {
            return 1;
        }
        int myRecordId = this.recid;
        int otherRecordId = tableRowLocation.recid;
        if (myRecordId == otherRecordId) {
            return 0;
        } else if (myRecordId < otherRecordId) {
            return -1;
        } else {
            return 1;
        }
    }


    public void setFrom(RecordId recordId) {
        this.pageno = recordId.getPageNumber();
        this.recid = recordId.getId();
        this.recordId = recordId;
    }

    /**
     * 获取当前类型格式Id
     */
    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.ACCESS_HEAP_ROW_LOCATION_V1_ID;
    }

    /**
     * 写入页号和记录标识
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        CompressedNumber.writeLong(out, this.pageno);
        CompressedNumber.writeInt(out, this.recid);
    }

    /**
     * 获取页号和记录标识
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.pageno = CompressedNumber.readLong(in);
        this.recid = CompressedNumber.readInt(in);
        recordId = null;
    }

    public RecordId getRecordId(BaseContainerHandle baseContainerHandle)   {
        if (recordId != null) {
            return recordId;
        }
        return recordId = baseContainerHandle.makeRecordId(this.pageno, this.recid);
    }


    public int hashCode() {
        return ((int) this.pageno) ^ this.recid;
    }


    public String toString() {
        return "(" + this.pageno + "," + this.recid + ")";
    }

    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        return new TableRowLocation(this);
    }

    public TableRowLocation() {
    }

    private TableRowLocation(TableRowLocation other) {
        this.pageno = other.pageno;
        this.recid = other.recid;
        this.recordId = other.recordId;
    }

    public TableRowLocation(RecordId recordId) {
        setFrom(recordId);
    }

    protected void setFrom(DataValueDescriptor theValue)  {
        TableRowLocation that = (TableRowLocation) theValue;
        this.pageno = that.pageno;
        this.recid = that.recid;
        this.recordId = that.recordId;
    }
}
