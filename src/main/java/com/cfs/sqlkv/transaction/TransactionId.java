package com.cfs.sqlkv.transaction;

import com.cfs.sqlkv.catalog.Formatable;
import com.cfs.sqlkv.io.CompressedNumber;
import com.cfs.sqlkv.io.StoredFormatIds;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 15:24
 */
public class TransactionId implements Formatable{

    private long id;

    public TransactionId(long id){
        this.id = id;
    }

    public TransactionId(){
        super();
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.RAW_STORE_TRANSACTION_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        CompressedNumber.writeLong(out, id);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = CompressedNumber.readLong(in);
    }

    protected long getId() {
        return id;
    }
}
