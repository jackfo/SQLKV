package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.factory.OptimizerFactory;
import com.cfs.sqlkv.compile.factory.TypeCompilerFactory;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 16:28
 */
public class CompilerContextImpl extends ContextImpl implements CompilerContext {

    private static final int SCOPE_CELL = 0;

    private final ParserImpl parser;
    private final LanguageConnectionContext lcc;
    private final LanguageConnectionFactory lcf;
    private TypeCompilerFactory	typeCompilerFactory;

    private boolean	firstOnStack;

    private boolean inUse;

    public CompilerContextImpl(ContextManager cm, LanguageConnectionContext lcc, TypeCompilerFactory typeCompilerFactory){
        super(cm, CompilerContext.CONTEXT_ID);
        this.lcc = lcc;
        lcf = lcc.getLanguageConnectionFactory();
        this.parser = lcf.newParser(this);
        this.typeCompilerFactory = typeCompilerFactory;
    }

    @Override
    public OptimizerFactory getOptimizerFactory() {
        return null;
    }

    @Override
    public ParserImpl getParser() {
        return parser;
    }

    @Override
    public boolean getInUse() {
        return inUse;
    }

    @Override
    public void firstOnStack() {
        firstOnStack = true;
    }

    @Override
    public void resetContext(){

    }

    @Override
    public void setInUse(boolean inUse){
        this.inUse = inUse;
    }
}
