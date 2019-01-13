package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 11:34
 */
public class QueryTreeNodeVector<E extends QueryTreeNode> extends QueryTreeNode implements Iterable<E>{

    private final ArrayList<E> v = new ArrayList<E>();

    protected final Class<E> eltClass;
    public QueryTreeNodeVector(Class<E> eltClass,ContextManager contextManager) {
        super(contextManager);
        this.eltClass = eltClass;
    }


    public final int size(){
        return v.size();
    }

    public void addElement(E qt) {
        v.add(qt);
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    public final E elementAt(int index) {
        return v.get(index);
    }
}
