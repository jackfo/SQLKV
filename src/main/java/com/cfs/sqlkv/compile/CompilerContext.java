package com.cfs.sqlkv.compile;


import com.cfs.sqlkv.compile.factory.OptimizerFactory;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.context.Context;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 18:34
 */
public interface CompilerContext extends Context {

    public static final String CONTEXT_ID = "CompilerContext";

    OptimizerFactory getOptimizerFactory();

    ParserImpl getParser();

    boolean getInUse();

    void firstOnStack();

    void resetContext();

    void setInUse(boolean inUse);

}
