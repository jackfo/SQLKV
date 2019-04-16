package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.service.io.ArrayUtil;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-02 21:21
 */
public class InsertConstantAction extends WriteCursorConstantAction {

    private String schemaName;
    private String tableName;
    private String columnNames[];
    TableRowLocation[] autoincRowLocation;

    /**
     * @param tableDescriptor 表描述
     * @param conglomId       表对应的标识
     */
    public InsertConstantAction(TableDescriptor tableDescriptor, Conglomerate conglomerate, long conglomId,
                                UUID targetUUID, TableRowLocation[] autoincRowLocation) {
        super(conglomId, conglomerate, targetUUID);
        this.tableName = tableDescriptor.getName();
        this.columnNames = tableDescriptor.getColumnNamesArray();
    }


    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.INSERT_CONSTANT_ACTION_V01_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        ArrayUtil.writeArray(out, autoincRowLocation);
        out.writeObject(tableName);
        ArrayUtil.writeArray(out, columnNames);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object[] objectArray = null;
        super.readExternal(in);
        objectArray = ArrayUtil.readObjectArray(in);
        if (objectArray != null) {
            autoincRowLocation = new TableRowLocation[objectArray.length];
            for (int i = 0; i < objectArray.length; i++) {
                autoincRowLocation[i] = (TableRowLocation) objectArray[i];
            }
        }
        tableName = (String) in.readObject();
        objectArray = ArrayUtil.readObjectArray(in);
        if (objectArray != null) {
            columnNames = new String[objectArray.length];
            for (int i = 0; i < objectArray.length; i++) {
                columnNames[i] = (String) objectArray[i];
            }
        }
    }
}
