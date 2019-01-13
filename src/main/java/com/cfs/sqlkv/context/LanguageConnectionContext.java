package com.cfs.sqlkv.context;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.compile.sql.PreparedStatement;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.GenericLanguageFactory;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 15:18
 */
public interface LanguageConnectionContext extends Context{

    String CONTEXT_ID = "LanguageConnectionContext";

    LanguageConnectionFactory getLanguageConnectionFactory();

    /**
     * 通过默认的schema返回相应的上下文编译器
     *
     * @return the compiler context
     */
    public CompilerContext pushCompilerContext();

    /**
     * 根据指定的schema返回对应的上下文编译器
     * @param sd the default schema
     *
     * @return the compiler context
     */
    public	CompilerContext pushCompilerContext(SchemaDescriptor sd);

    /**
     * 先获取上下文管理器 之后根据contextId获取对应类型的上下文
     * @return 返回Statement上下文
     * */
    StatementContext getStatementContext();

    DataDictionary getDataDictionary();

    int incrementBindCount();

    public PreparedStatement prepareInternalStatement(SchemaDescriptor compilationSchema, String sqlText, boolean isForReadOnly, boolean allowInternalSyntax) throws StandardException;

    /**获取默认的模式*/
    public SchemaDescriptor getDefaultSchema();

    public int getStatementDepth();

    StatementContext pushStatementContext(boolean isAtomic, boolean isForReadOnly, String stmtText, ParameterValueSet pvs, boolean rollbackParentContext, long timeoutMillis);

    int decrementBindCount();

    GenericLanguageFactory getLanguageFactory();
}
