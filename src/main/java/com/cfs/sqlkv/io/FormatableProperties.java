package com.cfs.sqlkv.io;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 10:51
 */

import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.io.StoredFormatIds;

import java.util.Enumeration;
import java.util.Properties;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

public class FormatableProperties extends Properties implements Formatable {

    public FormatableProperties() {
        this(null);
    }

    public FormatableProperties(Properties defaults) {
        super(defaults);
    }

    public void clearDefaults() {
        defaults = null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size());
        for (Enumeration e = keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            out.writeUTF(key);
            out.writeUTF(getProperty(key));
        }
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException {
        int size = in.readInt();
        for (; size > 0; size--) {
            put(in.readUTF(), in.readUTF());
        }
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.FORMATABLE_PROPERTIES_V01_ID;
    }

}
