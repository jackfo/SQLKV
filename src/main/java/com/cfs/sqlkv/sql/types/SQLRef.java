package com.cfs.sqlkv.sql.types;


import com.cfs.sqlkv.catalog.TypeDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:51
 */
public class SQLRef extends DataType implements RefDataValue {

    public TableRowLocation value;

    public SQLRef() {
    }

    public SQLRef(TableRowLocation rowLocation) {
        value = rowLocation;
    }

    @Override
    public void setValue(TableRowLocation rowLocation) {
        value = rowLocation;
    }


    @Override
    public String getString() {
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }


    @Override
    public Object getObject() {
        return value;
    }

    public void setFrom(DataValueDescriptor theValue) {
        if (theValue.isNull()) {
            setToNull();
        } else {
            value = (TableRowLocation) theValue.getObject();
        }
    }

    public int getLength() {
        return TypeDescriptor.MAXIMUM_WIDTH_UNKNOWN;
    }


    public String getTypeName() {
        return TypeId.REF_NAME;
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.SQL_REF_ID;
    }

    public boolean isNull() {
        return (value == null);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (TableRowLocation) in.readObject();
    }

    public void restoreToNull() {
        value = null;
    }

    @Override
    public boolean compare(int op, DataValueDescriptor other, boolean orderedNulls, boolean unknownRV) {
        return value.compare(op, ((SQLRef) other).value, orderedNulls, unknownRV);
    }

    @Override
    public int compare(DataValueDescriptor other) {
        return value.compare(((SQLRef) other).value);
    }


    public DataValueDescriptor cloneValue(boolean forceMaterialization) {
        if (value == null) {
            return new SQLRef();
        } else {
            return new SQLRef((TableRowLocation) value.cloneValue(false));
        }
    }

    public DataValueDescriptor getNewNull() {
        return new SQLRef();
    }


    public String toString() {
        if (value == null) {
            return "NULL";
        } else {
            return value.toString();
        }
    }

    public int hashCode() {
        if (value == null) {
            return 0;
        } else {
            return value.hashCode();
        }
    }

}
