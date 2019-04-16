package com.cfs.sqlkv.compile.predicate;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 17:42
 */
public interface OptimizableList {

    public int size();

    public Optimizable getOptimizable(int index);

    public void setOptimizable(int n, Optimizable optimizable);


    public void initAccessPaths(Optimizer optimizer);



}
