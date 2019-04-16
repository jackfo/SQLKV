package com.cfs.sqlkv.compile.name;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.QueryTreeNode;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 11:22
 */
public class TableName extends QueryTreeNode {

    /**表名*/
    String	tableName;
    /**模式名*/
    String	schemaName;
    /**是否存在模式*/
    private boolean hasSchema;
    /**设置开始游标*/
    private int	beginOffset = -1;
    /**设置结束游标*/
    private int	endOffset = -1;

    public TableName(ContextManager contextManager) {
        super(contextManager);
    }

    /**
     *
     * */
    public TableName(String schemaName, String tableName, ContextManager cm) {
        super(cm);
        hasSchema = schemaName != null;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    /**
     * 既有表名也有模式名的构造器
     *
     * @param schemaName	被引用的模式名
     * @param tableName     被引用的表名
     * @param tokBeginOffset token开始的位置
     * @param tokEndOffset   token结束的位置
     * @param cm            The context manager
     */
    public TableName(String schemaName, String tableName, int tokBeginOffset, int tokEndOffset, ContextManager cm) {
        this(schemaName, tableName, cm);
        this.setBeginOffset(tokBeginOffset);
        this.setEndOffset(tokEndOffset);
    }

    @Override
    public void	setBeginOffset( int beginOffset ) {
        this.beginOffset = beginOffset;
    }

    @Override
    public void	setEndOffset( int endOffset ) {
        this.endOffset = endOffset;
    }


    public String getSchemaName(){
        return schemaName;
    }

    public String getTableName(){
        return tableName;
    }

    public void bind(){
        schemaName = getSchemaDescriptor(schemaName).getSchemaName();
    }
}
