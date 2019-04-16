package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.compile.sql.GenericStatement;
import com.cfs.sqlkv.compile.sql.Statement;
import com.cfs.sqlkv.context.GenericLanguageConnectionContext;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.service.reflect.ReflectClasses;
import com.cfs.sqlkv.store.TransactionManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 15:23
 */
public class GenericLanguageConnectionFactory implements LanguageConnectionFactory{

    private ClassFactory classFactory = new ReflectClasses();

    public GenericLanguageConnectionFactory(){
    }

    /**
     * 获取相应的Statement
     * */
    @Override
    public Statement getStatement(SchemaDescriptor compilationSchema, String statementText, boolean forReadOnly) {
        return new GenericStatement(compilationSchema,statementText,forReadOnly);
    }

    @Override
    public ParserImpl newParser(CompilerContext compilerContext) {
        return new ParserImpl(compilerContext);
    }

    @Override
    public LanguageConnectionContext newLanguageConnectionContext(ContextManager cm, TransactionManager tc, GenericLanguageFactory lf, Database db, String userName, String drdaID, String dbname)   {
        return new GenericLanguageConnectionContext(cm,
                tc,
                lf,
                this,
                db,
                userName,
                0,
                drdaID,
                dbname);
    }

    @Override
    public ClassFactory getClassFactory() {
        return classFactory;
    }

    private GenericExecutionFactory	ef = new GenericExecutionFactory();
    @Override
    public GenericExecutionFactory getExecutionFactory() {
        return ef;
    }


}
