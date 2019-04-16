package com.cfs.sqlkv.compile.predicate;

import com.cfs.sqlkv.catalog.DataDictionary;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 16:52
 */
public class OptimizerImpl implements Optimizer {

    private DataDictionary dDictionary;
    private OptimizableList optimizableList;
    private OptimizablePredicateList predicateList;
    private int joinPosition;
    private int[] proposedJoinOrder;
    /**
     * 需要优化对象的数目
     */
    private int numOptimizables;

    /**
     * @param optimizableList 需要进行优化的对象
     * @param predicateList   优化的条件
     * @param dDictionary     数据字典
     */
    public OptimizerImpl(OptimizableList optimizableList, OptimizablePredicateList predicateList, DataDictionary dDictionary) {
        this.optimizableList = optimizableList;
        this.predicateList = predicateList;
        this.dDictionary = dDictionary;
        numOptimizables = optimizableList.size();
    }

    @Override
    public void modifyAccessPaths() {
        for (int ictr = 0; ictr < numOptimizables; ictr++) {
            Optimizable optimizable = optimizableList.getOptimizable(ictr);
            optimizableList.setOptimizable(ictr, optimizable.modifyAccessPath());
        }
    }

    @Override
    public boolean getNextPermutation() {
        if (numOptimizables < 1) {
            return false;
        }
        numOptimizables--;
        optimizableList.initAccessPaths(this);
        //获取第一个优化器,即第一个FromTable
        Optimizable optimizable = optimizableList.getOptimizable(0);
        pushPredicates(optimizable);
        return true;
    }

    public void pushPredicates(Optimizable curTable) {
        int numPreds = predicateList.size();
        Predicate pred;


    }

    public boolean getNextDecoratedPermutation() {
        Optimizable curOpt = optimizableList.getOptimizable(0);
        boolean retval = curOpt.nextAccessPath(this, null);
        return retval;
    }

    public void costPermutation() {
        Optimizable optimizable = optimizableList.getOptimizable(0);
        optimizable.optimizeIt(this, predicateList);
    }

    @Override
    public void initAccessPaths(Optimizer optimizer) {

    }


}
