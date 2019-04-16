package com.cfs.sqlkv.catalog;

/**
 * 索引描述
 * */
public interface IndexDescriptor
{
    /**
     * Returns true if the index is unique.
     */
    boolean			isUnique();
    /**
     * Returns true if the index is duplicate keys only for null key parts.
     * This is effective only if isUnique is false.
     */
    boolean			isUniqueWithDuplicateNulls();

    /**
     * The index represents a PRIMARY KEY or a UNIQUE NOT NULL constraint which
     * is deferrable.
     * {@code true} implies {@code isUnique() == false} and
     * {@code isUniqueWithDuplicateNulls() == false} and
     * {@code hasDeferrableChecking() == true}.

     * @return {@code true} if the index represents such a constraint
     */
    boolean isUniqueDeferrable();

    /**
     * Returns true if the index is used to support a deferrable constraint.
     */
    boolean hasDeferrableChecking();

    /**
     * 返回在基础表中列的位置
     * 每个索引列在索引表对应一个位置
     * */
    public int[] baseColumnPositions();

    /**
     * Returns the postion of a column.
     * <p>
     * Returns the position of a column within the key (1-based).
     * 0 means that the column is not in the key.  Same as the above
     * method, but it uses int instead of Integer.
     */
    public int getKeyColumnPosition(int heapColumnPosition);

    /**
     * Returns the number of ordered columns.
     * <p>
     * In the future, it will be
     * possible to store non-ordered columns in an index.  These will be
     * useful for covered queries.  The ordered columns will be at the
     * beginning of the index row, and they will be followed by the
     * non-ordered columns.
     *
     * For now, all columns in an index must be ordered.
     */
    int				numberOfOrderedColumns();

    /**
     * Returns the type of the index.  For now, we only support B-Trees,
     * so the value "BTREE" is returned.
     */
    String			indexType();

    /**
     * Returns array of boolean telling asc/desc info for each index
     * key column for convenience of using together with baseColumnPositions
     * method.  Both methods return an array with subscript starting from 0.
     */
    public boolean[]	isAscending();

    /**
     * Returns true if the specified column is ascending in the index
     * (1-based).
     */
    boolean			isAscending(Integer keyColumnPosition);

    /**
     * Returns true if the specified column is descending in the index
     * (1-based).  In the current release, only ascending columns are
     * supported.
     */
    boolean			isDescending(Integer keyColumnPosition);

    /**
     * set the baseColumnPositions field of the index descriptor.  This
     * is for updating the field in operations such as "alter table drop
     * column" where baseColumnPositions is changed.
     */
    public void     setBaseColumnPositions(int[] baseColumnPositions);

    /**
     * set the isAscending field of the index descriptor.  This
     * is for updating the field in operations such as "alter table drop
     * column" where isAscending is changed.
     */
    public void     setIsAscending(boolean[] isAscending);

    /**
     * set the numberOfOrderedColumns field of the index descriptor.  This
     * is for updating the field in operations such as "alter table drop
     * column" where numberOfOrderedColumns is changed.
     */
    public void     setNumberOfOrderedColumns(int numberOfOrderedColumns);
}

