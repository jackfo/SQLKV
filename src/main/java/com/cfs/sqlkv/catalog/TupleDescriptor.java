package com.cfs.sqlkv.catalog;

/**
 * @author zhengxiaokang
 * @Description 元组描述
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:59
 */
public class TupleDescriptor {

    /**数据词典*/
    private DataDictionary dataDictionary;

    public TupleDescriptor() {}

    public TupleDescriptor(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
    }

    protected DataDictionary getDataDictionary() {
        return dataDictionary;
    }

    protected void setDataDictionary(DataDictionary dataDictionary) {
        dataDictionary = dataDictionary;
    }

    /**数据是否需要持久化*/
    public boolean isPersistent() {
        return true;
    }
}
