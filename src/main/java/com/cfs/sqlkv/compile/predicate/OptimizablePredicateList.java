package com.cfs.sqlkv.compile.predicate;

import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 16:29
 */
public interface OptimizablePredicateList {

    public void generateQualifiers(ActivationClassBuilder acb, MethodBuilder mb, Optimizable optTable, boolean absolute);

    public int size();
}
