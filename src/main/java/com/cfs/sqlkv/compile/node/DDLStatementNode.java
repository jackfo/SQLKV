package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.name.TableName;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 15:24
 */
public abstract class DDLStatementNode extends StatementNode {

    private TableName tableName;
    private boolean		initOk;
    boolean implicitCreateSchema;

    public DDLStatementNode(TableName tableName, ContextManager cm) {
        super(cm);
        this.tableName = tableName;
        initOk = true;
    }

    public DDLStatementNode(ContextManager contextManager) {
        super(contextManager);
    }

    @Override
    public int activationKind() {
        return StatementNode.NEED_DDL_ACTIVATION;
    }


    protected final SchemaDescriptor getSchemaDescriptor(boolean ownerCheck, boolean doSystemSchemaCheck)  {
        String schemaName = tableName.getSchemaName();
        SchemaDescriptor sd = null;
        //获取编译上下文
        CompilerContext cc = getCompilerContext();

        if(sd==null){
            sd  = new SchemaDescriptor(getDataDictionary(), schemaName, (String) null, (UUID)null, false);
        }
        return sd;
    }
}
