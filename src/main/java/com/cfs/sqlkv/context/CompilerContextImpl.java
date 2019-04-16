package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.factory.OptimizerFactory;
import com.cfs.sqlkv.compile.factory.TypeCompilerFactory;
import com.cfs.sqlkv.compile.parse.ParserImpl;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.service.loader.ClassFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private TypeCompilerFactory typeCompilerFactory;

    private boolean firstOnStack;

    private boolean inUse;

    public CompilerContextImpl(ContextManager cm, LanguageConnectionContext lcc, TypeCompilerFactory typeCompilerFactory) {
        super(cm, CompilerContext.CONTEXT_ID);
        this.lcc = lcc;
        lcf = lcc.getLanguageConnectionFactory();
        this.parser = lcf.newParser(this);
        this.typeCompilerFactory = typeCompilerFactory;
        UUIDFactory uuidFactory = new UUIDFactory();
        classPrefix = "ac" + uuidFactory.createUUID().toString().replace('-', 'x');
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
    public void resetContext() {

    }

    @Override
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    private String classPrefix;
    private long nextClassName;

    @Override
    public String getUniqueClassName() {
        return classPrefix.concat(Long.toHexString(nextClassName++));
    }

    private int nextTableNumber;

    public int getNextTableNumber() {
        return nextTableNumber++;
    }

    @Override
    public ClassFactory getClassFactory() {
        return lcf.getClassFactory();
    }

    private int nextResultSetNumber;

    @Override
    public int getNumResultSets() {
        return nextResultSetNumber;
    }

    private List<Object> savedObjects;

    @Override
    public Object[] getSavedObjects() {
        if (savedObjects == null) {
            return null;
        }
        Object[] retVal = savedObjects.toArray();
        savedObjects = null; // erase to start over
        return retVal;
    }

    @Override
    public int addSavedObject(Object obj) {
        if (savedObjects == null) {
            savedObjects = new ArrayList();
        }

        savedObjects.add(obj);
        return savedObjects.size() - 1;
    }

    @Override
    public void setSavedObjects(List<Object> objs) {
        Iterator<Object> it = objs.iterator();
        while (it.hasNext()) {
            addSavedObject(it.next());
        }
    }

    @Override
    public int getNumTables() {
        return nextTableNumber;
    }

    public int getNextResultSetNumber() {
        return nextResultSetNumber++;
    }
}
