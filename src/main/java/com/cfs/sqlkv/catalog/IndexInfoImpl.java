package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.sql.dictionary.IndexRowGenerator;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 21:33
 */
public class IndexInfoImpl {
    //索引增长器
    private IndexRowGenerator indexRowGenerator;
    private long conglomerateNumber;
    private final CatalogRowFactory catalogRowFactory;
    //索引号
    private final int indexNumber;

    public IndexInfoImpl(int indexNumber, CatalogRowFactory crf) {
        this.catalogRowFactory = crf;
        this.indexNumber = indexNumber;
        this.conglomerateNumber = -1;
    }

    public long getConglomerateNumber() {
        return conglomerateNumber;
    }

    public void setConglomerateNumber(long conglomerateNumber) {
        this.conglomerateNumber = conglomerateNumber;
    }

    /**
     * 获取索引名
     */
    public String getIndexName() {
        return catalogRowFactory.getIndexName(indexNumber);
    }

    /**
     * 获取索引行增长器
     */
    public IndexRowGenerator getIndexRowGenerator() {
        return indexRowGenerator;
    }

    public int getColumnCount() {
        return catalogRowFactory.getIndexColumnCount(indexNumber);
    }

    public int getBaseColumnPosition(int colNumber) {
        return catalogRowFactory.getIndexColumnPositions(indexNumber)[colNumber];
    }

    public boolean isIndexUnique() {
        return catalogRowFactory.isIndexUnique(indexNumber);
    }

    public void setIndexRowGenerator(IndexRowGenerator indexRowGenerator) {
        this.indexRowGenerator = indexRowGenerator;
    }
}
