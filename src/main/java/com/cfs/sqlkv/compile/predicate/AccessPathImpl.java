package com.cfs.sqlkv.compile.predicate;

import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 16:51
 */
public class AccessPathImpl implements AccessPath{

    public ConglomerateDescriptor cd = null;
    Optimizer optimizer;

    public AccessPathImpl(Optimizer optimizer) {
        this.optimizer = optimizer;
    }

    public void setConglomerateDescriptor(ConglomerateDescriptor cd) {
        this.cd = cd;
    }

    public ConglomerateDescriptor getConglomerateDescriptor() {
        return cd;
    }

    public void copy(AccessPath copyFrom) {
        setConglomerateDescriptor(copyFrom.getConglomerateDescriptor());
    }
}
