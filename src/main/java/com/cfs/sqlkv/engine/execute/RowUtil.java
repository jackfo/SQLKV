package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.store.access.raw.FetchDescriptor;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:11
 */
public class RowUtil {

    public static final FetchDescriptor EMPTY_ROW_FETCH_DESCRIPTOR = new FetchDescriptor(0);


    private static final FetchDescriptor[] ROWUTIL_FETCH_DESCRIPTOR_CONSTANTS = {EMPTY_ROW_FETCH_DESCRIPTOR,
            new FetchDescriptor(1, 1),
            new FetchDescriptor(2, 2),
            new FetchDescriptor(3, 3),
            new FetchDescriptor(4, 4),
            new FetchDescriptor(5, 5),
            new FetchDescriptor(6, 6),
            new FetchDescriptor(7, 7)};

    public static final DataValueDescriptor[] EMPTY_ROW = new DataValueDescriptor[0];

    public static ExecRow getEmptyValueRow(int columnCount, LanguageConnectionContext lcc) {
        return lcc.getLanguageConnectionFactory().getExecutionFactory().getValueRow(columnCount);
    }

    public static boolean isRowEmpty(DataValueDescriptor[] row) {
        if (row == null) {
            return true;
        }
        if (row.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * 创建一个新模板
     */
    public static DataValueDescriptor[] newTemplate(DataValueFactory dvf, FormatableBitSet column_list, int[] format_ids) {
        int num_cols = format_ids.length;
        DataValueDescriptor[] ret_row = new DataValueDescriptor[num_cols];
        //获取列的长度
        int column_listSize = (column_list == null) ? 0 : column_list.getLength();
        for (int i = 0; i < num_cols; i++) {
            if ((column_list != null) && !((column_listSize > i) && (column_list.isSet(i)))) {
            } else {
                ret_row[i] = dvf.getNull(format_ids[i]);
            }
        }
        return ret_row;
    }

    /**
     * 根据模板获取新的数据描述
     */
    public static DataValueDescriptor[] newRowFromTemplate(DataValueDescriptor[] template) {
        DataValueDescriptor[] columns = new DataValueDescriptor[template.length];
        for (int column_index = template.length; column_index-- > 0; ) {
            if (template[column_index] != null) {
                columns[column_index] = template[column_index].getNewNull();
            }
        }
        return columns;
    }

    public static final FetchDescriptor getFetchDescriptorConstant(
            int single_column_number) {
        if (single_column_number < ROWUTIL_FETCH_DESCRIPTOR_CONSTANTS.length) {
            return (ROWUTIL_FETCH_DESCRIPTOR_CONSTANTS[single_column_number]);
        } else {
            return new FetchDescriptor(single_column_number, single_column_number);
        }
    }

    public static Object getColumn(Object[] row, FormatableBitSet columnList, int columnId) {
        if (columnList == null) {
            return columnId < row.length ? row[columnId] : null;
        }
        if (!(columnList.getLength() > columnId && columnList.isSet(columnId))) {
            return null;
        }
        return columnId < row.length ? row[columnId] : null;

    }

    public static int nextColumn(Object[] row, FormatableBitSet columnList, int startColumn) {
        if (columnList != null) {
            int size = columnList.getLength();
            for (; startColumn < size; startColumn++) {
                if (columnList.isSet(startColumn)) {
                    return startColumn;
                }
            }
            return -1;
        }
        if (row == null) {
            return -1;
        }
        return startColumn < row.length ? startColumn : -1;
    }

    public static void copyRefColumns(ExecRow to, ExecRow from, int start, int count) {
        copyRefColumns(to, 0, from, start, count);
    }

    public static void copyRefColumns(ExecRow to, int toStart, ExecRow from, int fromStart, int count) {
        for (int i = 1; i <= count; i++) {
            to.setColumn(i + toStart, from.getColumn(i + fromStart));
        }
    }
}
