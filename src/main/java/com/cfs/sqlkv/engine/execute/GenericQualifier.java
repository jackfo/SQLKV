package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 19:13
 */
public class GenericQualifier implements Qualifier {

    private int columnId;
    private int operator;
    private GeneratedMethod orderableGetter;
    private Activation activation;
    private boolean orderedNulls;

    public GenericQualifier(int columnId, int operator, GeneratedMethod orderableGetter, Activation activation, boolean orderedNulls) {
        this.columnId = columnId;
        this.operator = operator;
        this.orderableGetter = orderableGetter;
        this.activation = activation;
        this.orderedNulls = orderedNulls;
    }

    public int getColumnId() {
        return columnId;
    }

    @Override
    public DataValueDescriptor getOrderable() {
        return (DataValueDescriptor) (orderableGetter.invoke(activation));
    }

    public int getOperator() {
        return operator;
    }

    public boolean getOrderedNulls() {
        return orderedNulls;
    }

    public void reinitialize() {

    }

    private DataValueDescriptor orderableCache = null;
    @Override
    public void clearOrderableCache() {
        orderableCache = null;
    }

    @Override
    public String toString() {
        return "GenericQualifier{" +
                "columnId=" + columnId +
                ", operator=" + operator +
                ", orderableGetter=" + orderableGetter +
                ", activation=" + activation +
                ", orderedNulls=" + orderedNulls +
                '}';
    }
}
