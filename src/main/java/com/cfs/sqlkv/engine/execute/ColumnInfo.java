package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.catalog.types.BasicProviderInfo;
import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.DefaultInfo;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.io.ArrayUtil;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description 列的信息
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:55
 */
public class ColumnInfo {

    public static final int CREATE					= 0;
    public static final int DROP					= 1;
    int                         action;
    String                      name;
    DataTypeDescriptor          dataType;
    DefaultInfo                 defaultInfo;
    BasicProviderInfo[]         providers;
    DataValueDescriptor         defaultValue;
    UUID                        newDefaultUUID;
    UUID                        oldDefaultUUID;
    long                        autoincStart;
    long                        autoincInc;
    boolean                        autoincCycle;

    long                        autoinc_create_or_modify_Start_Increment = -1;

    public ColumnInfo(){}

    public	ColumnInfo(        String						    name,
                               DataTypeDescriptor			dataType,
                               DataValueDescriptor			defaultValue,
                               DefaultInfo					defaultInfo,
                               BasicProviderInfo[]			providers,
                               UUID							newDefaultUUID,
                               UUID							oldDefaultUUID,
                               int							action,
                               long							autoincStart,
                               long							autoincInc,
                               boolean						autoincCycle,
                               long							autoinc_create_or_modify_Start_Increment){


        this.name = name;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.defaultInfo = defaultInfo;
        this.providers = ArrayUtil.copy(providers);
        this.newDefaultUUID = newDefaultUUID;
        this.oldDefaultUUID = oldDefaultUUID;
        this.action = action;
        this.autoincStart = autoincStart;
        this.autoincInc = autoincInc;
        this.autoincCycle = autoincCycle;
    }

}
