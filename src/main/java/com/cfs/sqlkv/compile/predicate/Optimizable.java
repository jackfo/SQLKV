package com.cfs.sqlkv.compile.predicate;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 16:55
 */
public interface Optimizable {

    public Optimizable modifyAccessPath();

    public void initAccessPaths(Optimizer optimizer);

    /**获取下一个可利用路径执行优化器*/
    boolean nextAccessPath(Optimizer optimizer, OptimizablePredicateList predList);

    public void optimizeIt(Optimizer optimizer, OptimizablePredicateList predList);


    public boolean pushOptPredicate(OptimizablePredicate optimizablePredicate);
}
