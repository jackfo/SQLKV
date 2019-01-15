package com.cfs.sqlkv.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-13 15:07
 */
public class FormatableBitSet implements Formatable {

    private byte[]	value;
    private	byte	bitsInLastByte;
    private transient int	lengthAsBits;

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
