package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.factory.GenericExecutionFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 11:34
 */
public class QueryTreeNodeVector<E extends QueryTreeNode> extends QueryTreeNode implements Iterable<E> {

    private final ArrayList<E> v = new ArrayList<E>();

    protected final Class<E> eltClass;

    public QueryTreeNodeVector(Class<E> eltClass, ContextManager contextManager) {
        super(contextManager);
        this.eltClass = eltClass;
    }


    public final int size() {
        return v.size();
    }

    public void addElement(E qt) {
        v.add(qt);
    }

    @Override
    public Iterator<E> iterator() {
        return v.iterator();
    }

    public final E elementAt(int index) {
        return v.get(index);
    }

    public final void insertElementAt(E qt, int index) {
        v.add(index, qt);
    }

    public final E removeElementAt(int index) {
        return v.remove(index);
    }

    public final void setElementAt(E qt, int index) {
        v.set(index, qt);
    }

    public final GenericExecutionFactory getExecutionFactory() {
        GenericExecutionFactory ef = getLanguageConnectionContext().getLanguageConnectionFactory().getExecutionFactory();
        return ef;
    }

    public final void removeElement(E qt) {
        v.remove(qt);
    }

    public final void nondestructiveAppend(QueryTreeNodeVector<E> qtnv) {
        v.addAll(qtnv.v);
    }

    public final void removeAllElements() {
        v.clear();
    }

}
