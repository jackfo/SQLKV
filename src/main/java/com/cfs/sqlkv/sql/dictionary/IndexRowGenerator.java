package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.catalog.IndexDescriptor;
import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.IndexDescriptorImpl;
import com.cfs.sqlkv.column.ColumnDescriptorList;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.engine.execute.GenericExecutionContext;

import com.cfs.sqlkv.factory.GenericExecutionFactory;
import com.cfs.sqlkv.row.ExecIndexRow;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 18:31
 */
public class IndexRowGenerator implements IndexDescriptor, Formatable {
    private IndexDescriptor id;
    private GenericExecutionFactory ef;

    /**
     * Constructor for an IndexRowGeneratorImpl
     *
     * @param indexType                  The type of index
     * @param isUnique                   True means the index is unique
     * @param isUniqueWithDuplicateNulls means the index is almost unique
     *                                   i.e. unique only for non null keys
     * @param isUniqueDeferrable         True means the index represents a PRIMARY
     *                                   KEY or a UNIQUE NOT NULL constraint which
     *                                   is deferrable.
     * @param hasDeferrableChecking      True if the index is used to back a
     *                                   deferrable constraint
     * @param baseColumnPositions        An array of column positions in the base
     *                                   table.  Each index column corresponds to a
     *                                   column position in the base table.
     * @param isAscending                An array of booleans telling asc/desc on each
     *                                   column.
     * @param numberOfOrderedColumns     In the future, it will be possible
     *                                   to store non-ordered columns in an
     *                                   index.  These will be useful for
     *                                   covered queries.
     */
    public IndexRowGenerator(String indexType,
                             boolean isUnique,
                             boolean isUniqueWithDuplicateNulls,
                             boolean isUniqueDeferrable,
                             boolean hasDeferrableChecking,
                             int[] baseColumnPositions,
                             boolean[] isAscending,
                             int numberOfOrderedColumns) {
        id = new IndexDescriptorImpl(indexType,
                isUnique,
                isUniqueWithDuplicateNulls,
                isUniqueDeferrable,
                hasDeferrableChecking,
                baseColumnPositions,
                isAscending,
                numberOfOrderedColumns);

    }

    public IndexDescriptor getId() {
        return id;
    }

    /**
     * Constructor for an IndexRowGeneratorImpl
     *
     * @param indexDescriptor An IndexDescriptor to delegate calls to
     */
    public IndexRowGenerator(IndexDescriptor indexDescriptor) {
        id = indexDescriptor;
    }

    /**
     * 获取执行工厂
     */
    private GenericExecutionFactory getExecutionFactory() {
        if (ef == null) {
            GenericExecutionContext genericExecutionContext;
            genericExecutionContext = (GenericExecutionContext) getContext(GenericExecutionContext.CONTEXT_ID);
            ef = genericExecutionContext.getExecutionFactory();
        }
        return ef;
    }

    /**
     * 获取一个空的索引模板
     *
     * @param columnList       基础表的列描述
     * @param tableRowLocation 空的行位置
     * @  thrown on error.
     */
    public ExecIndexRow getNullIndexRow(ColumnDescriptorList columnList, TableRowLocation tableRowLocation)   {
        int[] baseColumnPositions = id.baseColumnPositions();
        //创建索引的模板
        ExecIndexRow indexRow = new IndexRow(baseColumnPositions.length + 1);
        //设置对应类型的空值
        for (int i = 0; i < baseColumnPositions.length; i++) {
            DataTypeDescriptor dtd = columnList.elementAt(baseColumnPositions[i] - 1).getType();
            indexRow.setColumn(i + 1, dtd.getNull());
        }
        indexRow.setColumn(baseColumnPositions.length + 1, tableRowLocation);
        return indexRow;
    }

    /**
     * Get an index row for this index given a row from the base table
     * and the TableRowLocation of the base row.  This method can be used
     * to get the new index row for inserts, and the old and new index
     * rows for deletes and updates.  For updates, the result row has
     * all the old column values followed by all of the new column values,
     * so you must form a row using the new column values to pass to
     * this method to get the new index row.
     *
     * @param baseRow          A row in the base table
     * @param TableRowLocation The TableRowLocation of the row in the base table
     * @param indexRow         A template for the index row.  It must have the
     *                         correct number of columns.
     * @param bitSet           If non-null, then baseRow is a partial row and the
     *                         set bits in bitSet represents the column mapping for
     *                         the partial row to the complete base row. <B> WARNING:
     *                         </B> ONE based!!!
     * @  Thrown on error
     */
    public void getIndexRow(ExecRow baseRow, TableRowLocation TableRowLocation, ExecIndexRow indexRow, FormatableBitSet bitSet)   {
        int[] baseColumnPositions = id.baseColumnPositions();
        int colCount = baseColumnPositions.length;

        if (bitSet == null) {
            for (int i = 0; i < colCount; i++) {
                indexRow.setColumn(i + 1, baseRow.getColumn(baseColumnPositions[i]));
            }
        } else {
            for (int i = 0; i < colCount; i++) {
                int fullColumnNumber = baseColumnPositions[i];
                int partialColumnNumber = 0;
                for (int index = 1; index <= fullColumnNumber; index++) {
                    if (bitSet.get(index)) {
                        partialColumnNumber++;
                    }
                }
                indexRow.setColumn(i + 1, baseRow.getColumn(partialColumnNumber));
            }
        }
        indexRow.setColumn(colCount + 1, TableRowLocation);
    }

    /**
     * Return an array of collation ids for this table.
     * <p>
     * Return an array of collation ids, one for each column in the
     * columnDescriptorList.  This is useful for passing collation id info
     * down to store, for instance in createConglomerate() to create
     * the index.
     * <p>
     * This is only expected to get called during ddl, so object allocation
     * is ok.
     *
     * @param columnList ColumnDescriptors describing the base table.
     * @  Standard exception policy.
     **/
    public int[] getColumnCollationIds(ColumnDescriptorList columnList)
              {
        int[] base_cols = id.baseColumnPositions();
        int[] collation_ids = new int[base_cols.length + 1];

        for (int i = 0; i < base_cols.length; i++) {
            collation_ids[i] =
                    columnList.elementAt(
                            base_cols[i] - 1).getType().getCollationType();
        }

        // row location column at end is always basic collation type.
        collation_ids[collation_ids.length - 1] =
                StringDataValue.COLLATION_TYPE_UCS_BASIC;

        return (collation_ids);
    }


    /**
     * Get the IndexDescriptor that this IndexRowGenerator is based on.
     */
    public IndexDescriptor getIndexDescriptor() {
        return id;
    }

    /**
     * Zero-argument constructor for Formatable interface
     */
    public IndexRowGenerator() {
    }

    /**
     * @see IndexDescriptor#isUniqueWithDuplicateNulls
     */
    public boolean isUniqueWithDuplicateNulls() {
        return id.isUniqueWithDuplicateNulls();
    }

    public boolean hasDeferrableChecking() {
        return id.hasDeferrableChecking();
    }


    public boolean isUniqueDeferrable() {
        return id.isUniqueDeferrable();
    }


    /**
     * @see IndexDescriptor#isUnique
     */
    public boolean isUnique() {
        return id.isUnique();
    }

    /**
     * @see IndexDescriptor#baseColumnPositions
     */
    public int[] baseColumnPositions() {
        return id.baseColumnPositions();
    }

    /**
     * @see IndexDescriptor#getKeyColumnPosition
     */
    public int getKeyColumnPosition(int heapColumnPosition) {
        return id.getKeyColumnPosition(heapColumnPosition);
    }

    /**
     * @see IndexDescriptor#numberOfOrderedColumns
     */
    public int numberOfOrderedColumns() {
        return id.numberOfOrderedColumns();
    }

    /**
     * @see IndexDescriptor#indexType
     */
    public String indexType() {
        return id.indexType();
    }

    public String toString() {
        return id.toString();
    }

    /**
     * @see IndexDescriptor#isAscending
     */
    public boolean isAscending(Integer keyColumnPosition) {
        return id.isAscending(keyColumnPosition);
    }

    /**
     * @see IndexDescriptor#isDescending
     */
    public boolean isDescending(Integer keyColumnPosition) {
        return id.isDescending(keyColumnPosition);
    }

    /**
     * @see IndexDescriptor#isAscending
     */
    public boolean[] isAscending() {
        return id.isAscending();
    }

    /**
     * @see IndexDescriptor#setBaseColumnPositions
     */
    public void setBaseColumnPositions(int[] baseColumnPositions) {
        id.setBaseColumnPositions(baseColumnPositions);
    }

    /**
     * @see IndexDescriptor#setIsAscending
     */
    public void setIsAscending(boolean[] isAscending) {
        id.setIsAscending(isAscending);
    }

    /**
     * @see IndexDescriptor#setNumberOfOrderedColumns
     */
    public void setNumberOfOrderedColumns(int numberOfOrderedColumns) {
        id.setNumberOfOrderedColumns(numberOfOrderedColumns);
    }


    ////////////////////////////////////////////////////////////////////////////
    //
    // EXTERNALIZABLE
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @throws IOException            Thrown on read error
     * @throws ClassNotFoundException Thrown on read error
     * @see java.io.Externalizable#readExternal
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (IndexDescriptor) in.readObject();
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
    }

    /**
     * 获取索引格式
     */
    public int getTypeFormatId() {
        return StoredFormatIds.INDEX_ROW_GENERATOR_V01_ID;
    }


    /**
     * 获取上下文
     */
    private static Context getContext(final String contextID) {
        return ContextService.getContext(contextID);
    }


    public ExecIndexRow getIndexRowTemplate() {
        return new IndexRow(id.baseColumnPositions().length + 1);
    }
}
