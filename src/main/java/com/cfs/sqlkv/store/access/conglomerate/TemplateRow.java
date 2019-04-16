package com.cfs.sqlkv.store.access.conglomerate;


import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 22:02
 */
public class TemplateRow {


    private static DataValueDescriptor[] allocate_objects(Transaction rawtran, int num_cols_to_allocate,
                                                          FormatableBitSet column_list, int[] format_ids)   {
        DataValueDescriptor[] ret_row = new DataValueDescriptor[num_cols_to_allocate];
        int num_cols = (column_list == null ? format_ids.length : column_list.size());
        DataValueFactory dvf = rawtran.getDataValueFactory();
        for (int i = 0; i < num_cols; i++) {
            if ((column_list != null) && (!column_list.get(i))) {
            } else {
                ret_row[i] = dvf.getNull(format_ids[i]);
            }
        }
        return ret_row;
    }

    public static DataValueDescriptor[] newRow(Transaction rawtran, FormatableBitSet column_list, int[] format_ids)   {
        return allocate_objects(rawtran, format_ids.length, column_list, format_ids);
    }

    public static DataValueDescriptor[] newBranchRow(Transaction rawtran, int[] format_ids,
                                                     DataValueDescriptor page_ptr)   {
        DataValueDescriptor[] columns = allocate_objects(rawtran, format_ids.length + 1, null, format_ids);
        columns[format_ids.length] = page_ptr;
        return columns;
    }
}
