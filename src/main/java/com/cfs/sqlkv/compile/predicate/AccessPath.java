package com.cfs.sqlkv.compile.predicate;

import com.cfs.sqlkv.sql.dictionary.ConglomerateDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 16:53
 */
public interface AccessPath {

    public void setConglomerateDescriptor(ConglomerateDescriptor cd);

    public ConglomerateDescriptor getConglomerateDescriptor();
}
