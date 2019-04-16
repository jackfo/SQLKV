package com.cfs.sqlkv.compile.node;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:23
 */

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.TypeCompiler;
import com.cfs.sqlkv.compile.Visitable;
import com.cfs.sqlkv.compile.factory.TypeCompilerFactory;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.factory.GenericConstantActionFactory;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.util.StatementUtil;


/**
 * 所有查询树的root class
 */
public class QueryTreeNode implements Visitable {

    private ContextManager contextManager;

    public QueryTreeNode(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    private int beginOffset = -1;
    private int endOffset = -1;

    public void setBeginOffset(int beginOffset) {
        this.beginOffset = beginOffset;
    }

    public int getBeginOffset() {
        return beginOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    final public DataDictionary getDataDictionary() {
        return getLanguageConnectionContext().getDataDictionary();
    }


    private LanguageConnectionContext lcc;

    /**
     * 通过上下文获取对应的语言连接上下文
     */
    protected final LanguageConnectionContext getLanguageConnectionContext() {
        if (lcc == null) {
            lcc = (LanguageConnectionContext) getContextManager().getContext(LanguageConnectionContext.CONTEXT_ID);
        }
        return lcc;
    }

    public final ContextManager getContextManager() {
        return contextManager;
    }

    protected final CompilerContext getCompilerContext() {
        return (CompilerContext) getContextManager().getContext(CompilerContext.CONTEXT_ID);
    }

    protected final ClassFactory getClassFactory() {
        return getLanguageConnectionContext().getLanguageConnectionFactory().getClassFactory();
    }


    public final SchemaDescriptor getSchemaDescriptor(String schemaName) {
        return getSchemaDescriptor(schemaName, true);
    }

    final SchemaDescriptor getSchemaDescriptor(String schemaName, boolean raiseError) {
        return StatementUtil.getSchemaDescriptor
                (
                        schemaName,
                        raiseError,
                        getDataDictionary(),
                        getLanguageConnectionContext(),
                        getCompilerContext()
                );
    }

    //获取表相关描述
    protected final TableDescriptor getTableDescriptor(String tableName, SchemaDescriptor schema) {
        DataDictionary dataDictionary = getDataDictionary();
        TransactionManager transactionManager = this.getLanguageConnectionContext().getTransactionCompile();
        TableDescriptor td = dataDictionary.getTableDescriptor(tableName, schema, transactionManager);
        return td;
    }

    public ConstantAction makeConstantAction() {
        return null;
    }

    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        throw new RuntimeException("current node haven't generate");
    }

    protected final TypeCompiler getTypeCompiler(TypeId typeId) {
        return TypeCompilerFactory.staticGetTypeCompiler(typeId);
    }

    public TableName makeTableName(String schemaName, String flatName) {
        return new TableName(schemaName, flatName, getContextManager());
    }


}
