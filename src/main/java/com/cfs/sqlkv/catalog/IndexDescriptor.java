package com.cfs.sqlkv.catalog;

/**
 * 索引描述
 */
public interface IndexDescriptor {

    boolean isUnique();

    boolean isUniqueWithDuplicateNulls();

    boolean isUniqueDeferrable();

    boolean hasDeferrableChecking();

    public int[] baseColumnPositions();

    public int getKeyColumnPosition(int heapColumnPosition);

    int numberOfOrderedColumns();

    String indexType();


    public boolean[] isAscending();


    boolean isAscending(Integer keyColumnPosition);


    boolean isDescending(Integer keyColumnPosition);


    public void setBaseColumnPositions(int[] baseColumnPositions);


    public void setIsAscending(boolean[] isAscending);


    public void setNumberOfOrderedColumns(int numberOfOrderedColumns);
}

