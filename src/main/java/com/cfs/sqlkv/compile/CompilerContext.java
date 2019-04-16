package com.cfs.sqlkv.compile;


import com.cfs.sqlkv.compile.factory.OptimizerFactory;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.service.loader.ClassFactory;

import java.util.List;

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


    public String getUniqueClassName();

    int getNextTableNumber();

    public ClassFactory getClassFactory();

    /**
     * 获取结果集的数量
     */
    public int getNumResultSets();

    public Object[] getSavedObjects();

    public int addSavedObject(Object o);

    public void setSavedObjects(List<Object> objs);

    public int getNumTables();

    public int getNextResultSetNumber();

}
