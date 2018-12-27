package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.Formatable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 16:21
 */
public class BaseTypeIdImpl implements Formatable {

    private int formatId;

    String unqualifiedName;

    /**jdbc的id类型*/
    transient int JDBCTypeId;

    public BaseTypeIdImpl() {}

    /**根据formatId创建相应的BaseTypeIdImpl实例*/
    public BaseTypeIdImpl(int formatId) {
        this.formatId = formatId;
        setTypeIdSpecificInstanceVariables();
    }

    /**
     * 根据formatId设置unqualifiedName和JDBCTypeId
     * */
    private void setTypeIdSpecificInstanceVariables(){
        switch (getTypeFormatId()){

        }
    }

    @Override
    public int getTypeFormatId() {
        if ( formatId != 0 ) {
            return formatId;
        }else {
            return 0;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
