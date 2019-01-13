package com.cfs.sqlkv.store.access.transaction;

import com.cfs.sqlkv.catalog.Formatable;
import com.cfs.sqlkv.transaction.GlobalTransactionId;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.transaction.TransactionId;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 16:20
 */
public class TransactionTableEntry implements Formatable{

    /**更新*/
    public static final int UPDATE		= 0x1;
    /**恢复*/
    public static final int RECOVERY	= 0x2;
    /**排除*/
    public static final int EXCLUDE	= 0x4;

    private TransactionId transactionId;

    private transient boolean needExclusion;

    public TransactionTableEntry(Transaction transaction,TransactionId transactionId){

    }

    private transient Transaction transaction;
    private transient boolean update;
    private transient boolean recovery;
    /**事务状态*/
    private int transactionStatus;
    private GlobalTransactionId gid;
    public TransactionTableEntry(Transaction transaction, TransactionId transactionId, int status, int attribute) {
        this.transaction = transaction;
        this.transactionId = transactionId;
        transactionStatus   = status;
        update              = (attribute & UPDATE)   != 0;
        needExclusion       = (attribute & EXCLUDE)  != 0;
        recovery            = (attribute & RECOVERY) != 0;
        if (recovery) {
            gid = transaction.getGlobalId();
        }
    }

    public boolean needExclusion() {
        return needExclusion;
    }


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
}
