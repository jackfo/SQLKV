package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description 唯一描述
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 16:03
 */
public abstract class UniqueTupleDescriptor extends TupleDescriptor{

    public  UniqueTupleDescriptor() { super(); }
    public  UniqueTupleDescriptor( DataDictionary dd ) { super( dd ); }

    /**返回唯一id的描述*/
    public abstract UUID getUUID();
}
