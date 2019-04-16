package com.cfs.sqlkv.store.access.raw;

import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.Qualifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 20:31
 */
public class FetchDescriptor {
    /**
     * 行的长度
     */
    private int row_length;

    private FormatableBitSet validColumns;

    private Qualifier[][] qualifier_list;
    private int[] materialized_cols;
    private int maxFetchColumnId;

    private static final int ZERO_FILL_LENGTH = 100;
    private static final int[] zero_fill_array = new int[ZERO_FILL_LENGTH];

    private int[] validColumnsArray;

    public FetchDescriptor() {
    }

    public FetchDescriptor(int input_row_length) {
        row_length = input_row_length;
    }

    public FetchDescriptor(int input_row_length, int single_valid_column_number) {
        row_length = input_row_length;
        maxFetchColumnId = single_valid_column_number;
        validColumnsArray = new int[maxFetchColumnId + 1];
        validColumnsArray[single_valid_column_number] = 1;
    }

    public FetchDescriptor(int input_row_length, FormatableBitSet input_validColumns, Qualifier[][] input_qualifier_list) {
        row_length = input_row_length;
        qualifier_list = input_qualifier_list;

        if (qualifier_list != null) {
            materialized_cols = new int[row_length];
        }
        setValidColumns(input_validColumns);
    }

    public final void setValidColumns(FormatableBitSet input_validColumns) {
        validColumns = input_validColumns;
        setMaxFetchColumnId();
        if (validColumns != null) {
            validColumnsArray = new int[maxFetchColumnId + 1];
            for (int i = maxFetchColumnId; i >= 0; i--) {
                validColumnsArray[i] = ((validColumns.isSet(i)) ? 1 : 0);
            }
        }
    }

    public final int[] getValidColumnsArray() {
        return validColumnsArray;
    }


    private final void setMaxFetchColumnId() {
        maxFetchColumnId = row_length - 1;
        if (validColumns != null) {
            int vCol_length = validColumns.getLength();
            if (vCol_length < maxFetchColumnId + 1) {
                maxFetchColumnId = vCol_length - 1;
            }
            for (; maxFetchColumnId >= 0; maxFetchColumnId--) {
                if (validColumns.isSet(maxFetchColumnId))
                    break;
            }
        }
    }

    public final int getMaxFetchColumnId() {
        return maxFetchColumnId;
    }

    public final FormatableBitSet getValidColumns() {
        return validColumns;
    }

    public final Qualifier[][] getQualifierList() {
        return qualifier_list;
    }

    public final void reset() {
        int[] cols = materialized_cols;
        if (cols != null) {
            if (cols.length <= ZERO_FILL_LENGTH) {
                System.arraycopy(zero_fill_array, 0, cols, 0, cols.length);
            } else {
                int offset = 0;
                int howMany = cols.length;
                while (howMany > 0) {
                    int count = howMany > zero_fill_array.length ? zero_fill_array.length : howMany;
                    System.arraycopy(zero_fill_array, 0, cols, offset, count);
                    howMany -= count;
                    offset += count;
                }
            }
        }
    }


    public final int[] getMaterializedColumns() {
        return materialized_cols;
    }
}
