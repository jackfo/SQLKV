package com.cfs.sqlkv.io;

import com.cfs.sqlkv.store.access.transaction.TransactionTableEntry;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.transaction.TransactionId;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 16:19
 */
public class TransactionTable implements Formatable {

    private final ConcurrentHashMap<TransactionId, TransactionTableEntry> trans = new ConcurrentHashMap<>();

    @Override
    public int getTypeFormatId() {
        return 0;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

    public boolean remove(TransactionId id){
        TransactionTableEntry ent = trans.remove(id);
        return (ent == null || ent.needExclusion());
    }

    /**
     * 添加进去,并且设置事物是否是排它事务
     * */
    public void add(Transaction transaction, boolean exclude){
        TransactionId id = transaction.getId();
        TransactionTableEntry newEntry = new TransactionTableEntry(transaction, id, 0, exclude ? TransactionTableEntry.EXCLUDE : 0);
        synchronized(this) {
           trans.put(id, newEntry);
        }
    }
}
