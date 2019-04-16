package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.TupleDescriptor;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 09:49
 */
public class ColumnDescriptor extends TupleDescriptor {

    private DefaultInfo columnDefaultInfo;
    private TableDescriptor table;
    private String columnName;
    private int columnPosition;
    private DataTypeDescriptor columnType;
    private DataValueDescriptor columnDefault;
    private UUID uuid;
    private UUID defaultUUID;
    private long autoincStart;
    private long autoincInc;
    private long autoincValue;
    private boolean autoincCycle;


    /**
     * ColumnDescriptor的构造器
     *
     * @param columnName        列的名字
     * @param columnPosition    列的位置
     * @param columnType        列的DataTypeDescriptor
     * @param columnDefault     列的默认值DataValueDescriptor
     * @param columnDefaultInfo 列的默认信息
     * @param table             列所在的表描述
     * @param defaultUUID       默认的UUID
     * @param autoincStart      默认自增长开始值
     * @param autoincInc        是否是自增长列
     */

    public ColumnDescriptor(String columnName, int columnPosition,
                            DataTypeDescriptor columnType, DataValueDescriptor columnDefault,
                            DefaultInfo columnDefaultInfo,
                            TableDescriptor table,
                            UUID defaultUUID, long autoincStart, long autoincInc, boolean autoincCycle) {
        this.columnName = columnName;
        this.columnPosition = columnPosition;
        this.columnType = columnType;
        this.columnDefault = columnDefault;
        this.columnDefaultInfo = columnDefaultInfo;
        this.defaultUUID = defaultUUID;
        if (table != null) {
            this.table = table;
            this.uuid = table.getUUID();
        }
        this.autoincStart = autoincStart;
        this.autoincValue = autoincStart;
        this.autoincInc = autoincInc;
        this.autoincCycle = autoincCycle;
    }

    long autoinc_create_or_modify_Start_Increment = -1;

    public ColumnDescriptor(String columnName, int columnPosition,
                            DataTypeDescriptor columnType, DataValueDescriptor columnDefault,
                            DefaultInfo columnDefaultInfo,
                            UUID uuid,
                            UUID defaultUUID,
                            long autoincStart, long autoincInc, long autoincValue, boolean autoincCycle) {
        this.columnName = columnName;
        this.columnPosition = columnPosition;
        this.columnType = columnType;
        this.columnDefault = columnDefault;
        this.columnDefaultInfo = columnDefaultInfo;
        this.uuid = uuid;
        this.defaultUUID = defaultUUID;
        this.autoincStart = autoincStart;
        this.autoincValue = autoincValue;
        this.autoincInc = autoincInc;
        this.autoincCycle = autoincCycle;
    }

    public ColumnDescriptor(String columnName, int columnPosition,
                            DataTypeDescriptor columnType, DataValueDescriptor columnDefault,
                            DefaultInfo columnDefaultInfo,
                            TableDescriptor table,
                            UUID defaultUUID, long autoincStart, long autoincInc,
                            long userChangedWhat, boolean autoincCycle) {
        this(columnName, columnPosition, columnType, columnDefault,
                columnDefaultInfo, table, defaultUUID, autoincStart,
                autoincInc, autoincCycle);
        autoinc_create_or_modify_Start_Increment = userChangedWhat;
    }


    public DataTypeDescriptor getType() {
        return columnType;
    }

    public UUID getReferencingUUID() {
        return uuid;
    }

    /**
     * 自动增长的开始值
     */
    public long getAutoincStart() {
        return autoincStart;
    }

    public boolean getAutoincCycle() {
        return autoincCycle;
    }

    public long getAutoincInc() {
        return autoincInc;
    }

    /**
     * 获取增长列当前的值
     */
    public long getAutoincValue() {
        return autoincValue;
    }

    public void setPosition(int columnPosition) {
        this.columnPosition = columnPosition;
    }

    public boolean isAutoincrement() {
        return (autoincInc != 0);
    }

    public String getColumnName() {
        return columnName;
    }

    /**
     * 获取列的位置
     */
    public int getPosition() {
        return columnPosition;
    }

    public long getAutoinc_create_or_modify_Start_Increment() {
        return autoinc_create_or_modify_Start_Increment;
    }

    /**
     * 获取列默认信息
     */
    public DefaultInfo getDefaultInfo() {
        return columnDefaultInfo;
    }

    /**
     * 获取列默认值
     */
    public DataValueDescriptor getDefaultValue() {
        return columnDefault;
    }

    public UUID getDefaultUUID() {
        return defaultUUID;
    }

    public void setTableDescriptor(TableDescriptor tableDescriptor) {
        this.table = tableDescriptor;
    }
}
