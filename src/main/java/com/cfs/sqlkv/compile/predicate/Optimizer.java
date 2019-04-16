package com.cfs.sqlkv.compile.predicate;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 16:11
 */
public interface Optimizer {

    public static final String module = Optimizer.class.getName();

    public void modifyAccessPaths();

    public boolean getNextPermutation();

    public boolean getNextDecoratedPermutation();
    public void initAccessPaths(Optimizer optimizer);

    public void costPermutation();


}
