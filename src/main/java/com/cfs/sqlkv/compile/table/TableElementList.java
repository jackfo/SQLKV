package com.cfs.sqlkv.compile.table;

import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.column.ColumnDefinitionNode;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.QueryTreeNodeVector;
import com.cfs.sqlkv.engine.execute.ColumnInfo;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 19:10
 */
public class TableElementList extends QueryTreeNodeVector<TableElementNode> {

    public TableElementList(ContextManager contextManager){
        super(TableElementNode.class, contextManager);
    }

    public void addTableElement(TableElementNode tableElement) {
        addElement(tableElement);
        if (tableElement instanceof ColumnDefinitionNode) {
            numColumns++;
        }
    }

    public void setCollationTypesOnCharacterStringColumns(SchemaDescriptor sd)   {
//        for (TableElementNode te : this) {
//            if (te instanceof ColumnDefinitionNode) {
//                setCollationTypeOnCharacterStringColumn(sd, (ColumnDefinitionNode)te );
//            }
//        }
    }



    /**
     * 获取模式的编码格式设置到列中
     * 并将数据类型设置到对应的列中去
     * */
    public void setCollationTypeOnCharacterStringColumn(SchemaDescriptor sd, ColumnDefinitionNode cdn){
        int collationType = sd.getCollationType();
        DataTypeDescriptor dtd = cdn.getType();
        if ( dtd == null ) {
            //if ( !cdn.hasGenerationClause() ) {
                //throw StandardException.newException( SQLState.LANG_NEEDS_DATATYPE, cdn.getColumnName() );
            //}
        } else {
            /**如果是其类型是字符串需要设置对应的类型*/
            if (dtd.getTypeId().isStringTypeId()){
                cdn.setCollationType(collationType);
            }
        }


    }

    private int				numColumns;

    public int countNumberOfColumns() {
        return numColumns;
    }

    /**
     * 获取列的相关信息
     * */
    public int genColumnInfos(ColumnInfo[] colInfos)  {
        int	numConstraints = 0;
        int size = size();
        for (int index = 0; index < size; index++){

            if (! (elementAt(index) instanceof ColumnDefinitionNode)){
                numConstraints++;
                continue;
            }
            ColumnDefinitionNode coldef = (ColumnDefinitionNode) elementAt(index);
            colInfos[index - numConstraints] = new ColumnInfo(coldef.getColumnName(),
                            coldef.getType(),
                            coldef.getDefaultValue(),
                            coldef.getDefaultInfo(),
                            null,
                            (UUID) null,
                            coldef.getOldDefaultUUID(),
                            coldef.getAction(),
                            (coldef.isAutoincrementColumn() ?
                                    coldef.getAutoincrementStart() : 0),
                            (coldef.isAutoincrementColumn() ?
                                    coldef.getAutoincrementIncrement() : 0),
                            (coldef.isAutoincrementColumn() ?
                                    coldef.getAutoincrementCycle() : false),
                            (coldef.isAutoincrementColumn() ?
                                    coldef.getAutoinc_create_or_modify_Start_Increment() : -1));


        }

        return numConstraints;
    }



}
