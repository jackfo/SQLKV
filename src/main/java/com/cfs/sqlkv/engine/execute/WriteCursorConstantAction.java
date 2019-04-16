package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.conglomerate.Conglomerate;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-02 21:17
 */
public abstract class WriteCursorConstantAction implements ConstantAction, Formatable {

    public long conglomId;
    public UUID targetUUID;
    public Conglomerate conglomerate;

    public WriteCursorConstantAction(long conglomId, Conglomerate conglomerate, UUID targetUUID) {
        this.targetUUID = targetUUID;
        this.conglomId = conglomId;
        this.conglomerate = conglomerate;
    }

    public WriteCursorConstantAction() {
    }

    //todo:在写入的时候什么也不做
    @Override
    public void executeConstantAction(Activation activation) {

    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        conglomId = in.readLong();
        targetUUID = (UUID) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(conglomId);
        out.writeObject(targetUUID);
    }
}
