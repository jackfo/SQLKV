package com.cfs.sqlkv.column;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.DefaultInfo;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.DefaultNode;
import com.cfs.sqlkv.compile.node.ValueNode;
import com.cfs.sqlkv.compile.table.TableElementNode;
import com.cfs.sqlkv.engine.execute.ColumnInfo;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 19:17
 */
public class ColumnDefinitionNode extends TableElementNode {

    boolean						isAutoincrement;

    /**
     * The data type of this column.
     */
    DataTypeDescriptor type;

    DataValueDescriptor         defaultValue;
    DefaultInfo                 defaultInfo;
    DefaultNode                 defaultNode;
    boolean						keepCurrentDefault;
    long						autoincrementIncrement;
    long						autoincrementStart;
    boolean                     autoincrementCycle;

    /**
     * SQL语句中的每一列解析成ColumnDefinitionNode对象
     * */
    public ColumnDefinitionNode(String name, ValueNode defaultNode, DataTypeDescriptor dataType,
                                     long[] autoIncrementInfo, ContextManager cm){
        super(cm);
        this.type = dataType;
        this.name = name;
    }

    public final DataTypeDescriptor getType() {
        return type;
    }

    /**列的名字*/
    String	name;
    public String getColumnName() {
        return this.name;
    }

    /**
     * 获取当前列的默认DataValueDescriptor
     * */
    public DataValueDescriptor getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * 返回这一列包含的默认信息
     * */
    public DefaultInfo getDefaultInfo(){
        return defaultInfo;
    }

    public UUID getOldDefaultUUID() {
        return null;
    }

    public int getAction() {
        return ColumnInfo.CREATE;
    }

    public boolean isAutoincrementColumn(){
        return isAutoincrement;
    }

    public long getAutoincrementStart(){
        return autoincrementStart;
    }

    public long getAutoincrementIncrement(){
        return autoincrementIncrement;
    }

    public boolean getAutoincrementCycle(){
        return autoincrementCycle;
    }

    long autoinc_create_or_modify_Start_Increment;
    public long getAutoinc_create_or_modify_Start_Increment(){
        return autoinc_create_or_modify_Start_Increment;
    }

    public void setCollationType(int collationType) {
        type = getType().getCollatedType(collationType, StringDataValue.COLLATION_DERIVATION_IMPLICIT);
    }




}
