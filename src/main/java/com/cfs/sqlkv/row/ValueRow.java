package com.cfs.sqlkv.row;


import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:19
 */
public class ValueRow implements ExecRow {

    /**
     * 列的数据描述信息
     */
    private DataValueDescriptor[] column;

    private int ncols;

    public ValueRow(int ncols) {
        column = new DataValueDescriptor[ncols];
        this.ncols = ncols;
    }

    @Override
    public void setColumn(int position, DataValueDescriptor columnTemplate) {
        /**
         * 代表者新增加了列
         * */
        if (position > column.length) {
            realloc(position);
        }
        column[position - 1] = columnTemplate;
    }

    @Override
    public int nColumns() {
        return ncols;
    }

    @Override
    public DataValueDescriptor getColumn(int position) {
        if (position <= column.length) {
            return column[position - 1];
        } else {
            return null;
        }
    }

    protected void realloc(int ncols) {
        DataValueDescriptor[] newcol = new DataValueDescriptor[ncols];
        System.arraycopy(column, 0, newcol, 0, column.length);
        column = newcol;
    }

    @Override
    public DataValueDescriptor[] getRowArray() {
        return column;
    }

    @Override
    public void setRowArray(DataValueDescriptor[] rowArray) {
        column = rowArray;
    }

    public DataValueDescriptor[] getRowArrayClone() {
        int numColumns = column.length;
        DataValueDescriptor[] columnClones = new DataValueDescriptor[numColumns];
        for (int colCtr = 0; colCtr < numColumns; colCtr++) {
            if (column[colCtr] != null) {
                columnClones[colCtr] = column[colCtr].cloneValue(false);
            }
        }
        return columnClones;
    }


}
