package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.compile.sql.Statement;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.store.TransactionManager;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 15:21
 */
public interface LanguageConnectionFactory {

    public static final String module = LanguageConnectionFactory.class.getName();

    /**
     * 获取相应的Statement
     * */
    Statement getStatement(SchemaDescriptor compilationSchema, String statementText, boolean forReadOnly);

    /**创建相应的解析实例*/
    public ParserImpl newParser(CompilerContext compilerContext);

    /**创建语言连接上下文*/
    LanguageConnectionContext newLanguageConnectionContext(ContextManager cm, TransactionManager tc, GenericLanguageFactory lf, Database db, String userName, String drdaID, String dbname)  ;

    ClassFactory getClassFactory();

    GenericExecutionFactory	getExecutionFactory();
}
