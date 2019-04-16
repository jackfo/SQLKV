package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.catalog.TupleDescriptor;
import com.cfs.sqlkv.common.UUID;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.service.io.ArrayUtil;
import com.cfs.sqlkv.temp.Temp;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description 所有行工厂的父类
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 20:59
 */
public abstract class CatalogRowFactory {

    /**
     * 索引名
     */
    protected String[] indexNames;



    /**
     * 索引行的位置
     */
    protected int[][] indexColumnPositions;
    protected DataValueFactory dataValueFactory;

    private UUIDFactory uuidf = new UUIDFactory();

    public CatalogRowFactory(UUIDFactory uuidf, DataValueFactory dvf) {
        if (uuidf != null) {
            this.uuidf = uuidf;
        }
        this.dataValueFactory = dvf;
    }

    public String getIndexName(int indexNum) {
        return indexNames[indexNum];
    }

    private int indexCount;

    private int columnCount;

    private String catalogName;

    protected UUID tableUUID;
    protected UUID heapUUID;
    protected UUID[] indexUUID;
    protected boolean[] indexUniqueness;

    //获取索引数量
    public int getNumIndexes() {
        return indexCount;
    }

    public void initInfo(int columnCount,
                         String catalogName,
                         int[][] indexColumnPositions,
                         boolean[] indexUniqueness,
                         String[] uuidStrings) {
        indexCount = (indexColumnPositions != null) ? indexColumnPositions.length : 0;
        this.catalogName = catalogName;
        this.columnCount = columnCount;
        UUIDFactory uf = getUUIDFactory();
        this.tableUUID = uf.recreateUUID(uuidStrings[0]);
        this.heapUUID = uf.recreateUUID(uuidStrings[1]);
        if (indexCount > 0) {
            indexNames = new String[indexCount];
            indexUUID = new UUID[indexCount];
            for (int ictr = 0; ictr < indexCount; ictr++) {
                indexNames[ictr] = generateIndexName(ictr);
                indexUUID[ictr] = uf.recreateUUID(uuidStrings[ictr + 2]);
            }
            this.indexColumnPositions = ArrayUtil.copy2(indexColumnPositions);
            this.indexUniqueness = ArrayUtil.copy(indexUniqueness);
        }
    }

    public String generateIndexName(int indexNumber) {
        indexNumber++;
        return catalogName + "_INDEX" + indexNumber;
    }


    public ExecRow makeRow(TupleDescriptor td, TupleDescriptor parent)   {
       return null;
    }

    public ExecRow makeEmptyRow()   {
        return this.makeRow(null, null);
    }

    public int getHeapColumnCount()   {
        return columnCount;
    }

    public UUIDFactory getUUIDFactory() {
        return uuidf;
    }

    /**
     * 构建行对应的元组描述
     */
    public abstract TupleDescriptor buildDescriptor(ExecRow row, TupleDescriptor parentTuple, DataDictionary dataDictionary)  ;

    public int getIndexColumnCount(int indexNum) {
        return indexColumnPositions[indexNum].length;
    }


    /**
     * 根据索引号获取索引位置集合
     */
    public int[] getIndexColumnPositions(int indexNumber) {
        return indexColumnPositions[indexNumber];
    }

    /**
     * 检测索引是否具有唯一性
     */
    public boolean isIndexUnique(int indexNumber) {
        return (indexUniqueness != null ? indexUniqueness[indexNumber] : true);
    }

    public UUID getCanonicalIndexUUID(int indexNumber) {
        return indexUUID[indexNumber];
    }

    public UUID getCanonicalHeapUUID() {
        return heapUUID;
    }

    public UUID getCanonicalTableUUID() {
        return tableUUID;
    }

    public Properties getCreateIndexProperties(int indexNumber) {
        Properties properties = new Properties();
        return properties;
    }
}
