package com.cfs.sqlkv.column;

import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.common.UUID;

import java.util.ArrayList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 19:05
 */
public class ColumnDescriptorList extends ArrayList<ColumnDescriptor> {

    public ColumnDescriptor elementAt(int n) {
        return get(n);
    }

    /**
     * 根据表id和列id获取列相关描述
     */
    public ColumnDescriptor getColumnDescriptor(UUID tableID, int columnID) {
        ColumnDescriptor returnValue = null;
        for (ColumnDescriptor columnDescriptor : this) {
            if ((columnID == columnDescriptor.getPosition()) && tableID.equals(columnDescriptor.getReferencingUUID())) {
                returnValue = columnDescriptor;
                break;
            }
        }
        return returnValue;
    }

    public ColumnDescriptor getColumnDescriptor(UUID tableID, String columnName) {
        ColumnDescriptor returnValue = null;
        for (ColumnDescriptor columnDescriptor : this) {
            if (columnName.equals(columnDescriptor.getColumnName()) &&
                    tableID.equals(columnDescriptor.getReferencingUUID())) {
                returnValue = columnDescriptor;
                break;
            }
        }
        return returnValue;
    }
}
