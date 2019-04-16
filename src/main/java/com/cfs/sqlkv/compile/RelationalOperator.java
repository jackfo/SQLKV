package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.compile.predicate.Optimizable;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 11:29
 */
public interface RelationalOperator {

    public final int EQUALS_RELOP = 1;
    public final int NOT_EQUALS_RELOP = 2;
    public final int GREATER_THAN_RELOP = 3;
    public final int GREATER_EQUALS_RELOP = 4;
    public final int LESS_THAN_RELOP = 5;
    public final int LESS_EQUALS_RELOP = 6;
    public final int IS_NULL_RELOP = 7;
    public final int IS_NOT_NULL_RELOP = 8;

    public void generateAbsoluteColumnId(MethodBuilder mb, Optimizable optTable);

    public void generateOperator(MethodBuilder mb, Optimizable optTable);

    public void generateQualMethod(ActivationClassBuilder acb, MethodBuilder mb, Optimizable optTable);

    public void generateOrderedNulls(MethodBuilder mb);
}
