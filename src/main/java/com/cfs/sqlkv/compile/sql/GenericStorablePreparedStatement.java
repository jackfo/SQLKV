package com.cfs.sqlkv.compile.sql;

import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.util.ByteArray;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description 可以持久化的PreparedStatement
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-03 15:04
 */
public class GenericStorablePreparedStatement extends GenericPreparedStatement implements Formatable, StorablePreparedStatement {

    private ByteArray byteCode;
    private String 	  className;
    public GenericStorablePreparedStatement() {
        super();
    }
    public GenericStorablePreparedStatement(Statement stmt) {
        super(stmt);
    }
    @Override
    public ByteArray getByteCodeSaver() {
        if (byteCode == null) {
            byteCode = new ByteArray();
        }
        return byteCode;
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
