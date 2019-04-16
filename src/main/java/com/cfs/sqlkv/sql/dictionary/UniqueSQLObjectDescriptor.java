package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.UniqueTupleDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 18:27
 */
public abstract class UniqueSQLObjectDescriptor extends UniqueTupleDescriptor {

    public  UniqueSQLObjectDescriptor() { super(); }
    public  UniqueSQLObjectDescriptor( DataDictionary dd ) { super( dd ); }

}
