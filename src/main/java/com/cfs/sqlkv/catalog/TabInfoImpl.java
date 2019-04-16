package com.cfs.sqlkv.catalog;


import com.cfs.sqlkv.row.ExecIndexRow;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.sql.dictionary.CatalogRowFactory;
import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;
import com.cfs.sqlkv.sql.dictionary.IndexRowGenerator;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 18:01
 */
public class TabInfoImpl {

    private long tableConglomerate;
    private final CatalogRowFactory catalogRowFactory;
    /**
     * 记录表的相关索引信息
     */
    private IndexInfoImpl[] indexes;
    static final int ROWNOTDUPLICATE = -1;

    /**
     * 已经设置的索引数
     */
    private int numIndexesSet;

    public TabInfoImpl(CatalogRowFactory catalogRowFactory) {
        this.tableConglomerate = -1;
        this.catalogRowFactory = catalogRowFactory;
        //获取索引的数量
        int numIndexes = catalogRowFactory.getNumIndexes();
        if (numIndexes > 0) {
            indexes = new IndexInfoImpl[numIndexes];
            for (int indexCtr = 0; indexCtr < numIndexes; indexCtr++) {
                indexes[indexCtr] = new IndexInfoImpl(indexCtr, catalogRowFactory);
            }
        }
    }

    /**
     * 获取目录行工厂
     */
    public CatalogRowFactory getCatalogRowFactory() {
        return catalogRowFactory;
    }

    public int insertRow(ExecRow row, TransactionManager tc)   {
        TableRowLocation[] notUsed = new TableRowLocation[1];
        return insertRowListImpl(new ExecRow[]{row}, tc, notUsed);
    }

    /**
     * 插入行集合
     */
    public int insertRowList(ExecRow[] rowList, TransactionManager tc)   {
        TableRowLocation[] notUsed = new TableRowLocation[1];
        return insertRowListImpl(rowList, tc, notUsed);
    }

    /**
     * 插入行集合
     * <p>
     * 第一步:打开容器
     */
    private int insertRowListImpl(ExecRow[] rowList, TransactionManager tc, TableRowLocation[] rowLocationOut)   {
        ConglomerateController tableController;
        TableRowLocation tableRowLocation;
        //索引行
        ExecIndexRow indexableRow;
        int retCode = ROWNOTDUPLICATE;
        //获取表索引的数目
        int indexCount = catalogRowFactory.getNumIndexes();
        ConglomerateController[] indexControllers = new ConglomerateController[indexCount];
        tableController = tc.openConglomerate(getTableConglomerate(), false);
        for (int ictr = 0; ictr < indexCount; ictr++) {
            long conglomNumber = getIndexConglomerate(ictr);
            if (conglomNumber > -1) {
                //打开对应的控制器
                indexControllers[ictr] = tc.openConglomerate(conglomNumber, false);
            }
        }
        //创建行位置模板
        tableRowLocation = new TableRowLocation();
        rowLocationOut[0] = tableRowLocation;
        /**
         * 遍历所有的行
         * 将行插入到对应的位置
         * */
        for (int rowNumber = 0; rowNumber < rowList.length; rowNumber++) {
            ExecRow row = rowList[rowNumber];
            tableController.insertAndFetchLocation(row.getRowArray(), tableRowLocation);
            for (int ictr = 0; ictr < indexCount; ictr++) {
                if (indexControllers[ictr] == null) {
                    continue;
                }
                IndexRowGenerator indexRowGenerator = indexes[ictr].getIndexRowGenerator();
                indexableRow = getIndexRowFromTableRow(indexRowGenerator, tableRowLocation, row);
                indexControllers[ictr].insert(indexableRow.getRowArray());
            }
        }


        //关闭索引和当前表对应的控制器
        for (int ictr = 0; ictr < indexCount; ictr++) {
            if (indexControllers[ictr] == null) {
                continue;
            }
            indexControllers[ictr].close();
        }
        tableController.close();
        return retCode;
    }

    /**
     * 根据表行获取对应的索引行
     */
    private ExecIndexRow getIndexRowFromTableRow(IndexRowGenerator indexRowGenerator, TableRowLocation tableRowLocation, ExecRow tableRow)   {
        ExecIndexRow indexRow;
        IndexDescriptor indexDescriptor = indexRowGenerator.getId();
        indexRow = new IndexRow(indexDescriptor.baseColumnPositions().length + 1);
        indexRowGenerator.getIndexRow(tableRow, tableRowLocation, indexRow, null);
        return indexRow;
    }

    public long getTableConglomerate() {
        return tableConglomerate;
    }

    public long getIndexConglomerate(int indexID) {
        IndexInfoImpl indexInfo = indexes[indexID];
        return indexInfo.getConglomerateNumber();
    }

    /**
     * 为表对应的索引设置conglomerateNumber
     */
    public void setIndexConglomerate(int index, long indexConglomerate) {
        indexes[index].setConglomerateNumber(indexConglomerate);
        numIndexesSet++;
    }



    public void setIndexConglomerate(ConglomerateDescriptor cd) {
        int index;
        String indexName = cd.getConglomerateName();
        for (index = 0; index < indexes.length; index++) {
            if (indexes[index].getIndexName().equals(indexName)) {
                indexes[index].setConglomerateNumber(cd.getConglomerateNumber());
                break;
            }
        }
        numIndexesSet++;
    }

    private boolean heapSet;

    public void setTableConglomerate(long tableConglomerate) {
        this.tableConglomerate = tableConglomerate;
        heapSet = true;
    }

    public long getHeapConglomerate() {
        return tableConglomerate;
    }

    /**
     * 获取索引增长器
     */
    public IndexRowGenerator getIndexRowGenerator(int indexNumber) {
        return indexes[indexNumber].getIndexRowGenerator();
    }

    public int getNumberOfIndexes() {
        if (indexes == null) {
            return 0;
        } else {
            return indexes.length;
        }
    }

    public int getIndexColumnCount(int indexNumber) {
        return indexes[indexNumber].getColumnCount();
    }


    public int getBaseColumnPosition(int indexNumber, int colNumber) {
        return indexes[indexNumber].getBaseColumnPosition(colNumber);
    }

    public boolean isIndexUnique(int indexNumber) {
        return indexes[indexNumber].isIndexUnique();
    }

    public void setIndexRowGenerator(int indexNumber, IndexRowGenerator irg) {
        indexes[indexNumber].setIndexRowGenerator(irg);
    }

    public Properties getCreateIndexProperties(int indexNumber) {
        return catalogRowFactory.getCreateIndexProperties(indexNumber);
    }

    @Override
    public String toString() {
        return "TabInfoImpl{" +
                "tableConglomerate=" + tableConglomerate +
                ", catalogRowFactory=" + catalogRowFactory +
                ", indexes=" + Arrays.toString(indexes) +
                ", numIndexesSet=" + numIndexesSet +
                ", heapSet=" + heapSet +
                '}';
    }
}
