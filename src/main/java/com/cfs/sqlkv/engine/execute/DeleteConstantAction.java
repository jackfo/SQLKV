package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.Heap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-16 10:32
 */
public class DeleteConstantAction extends WriteCursorConstantAction {

    public int numColumns;
    public ResultDescription resultDescription;

    public DeleteConstantAction() {
        super();
    }

    public DeleteConstantAction(long conglomId, Heap heap, UUID targetUUID, int numColumns, ResultDescription resultDescription) {
        super(conglomId, heap, targetUUID);
        this.numColumns = numColumns;
        this.resultDescription = resultDescription;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        numColumns = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(numColumns);
    }

    public int getTypeFormatId() {
        return StoredFormatIds.DELETE_CONSTANT_ACTION_V01_ID;
    }
}
