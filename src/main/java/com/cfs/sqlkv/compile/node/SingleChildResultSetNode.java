package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.predicate.Optimizable;
import com.cfs.sqlkv.compile.predicate.Optimizer;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-08 15:38
 */
public class SingleChildResultSetNode extends FromTable {

    public ResultSetNode childResult;

    public SingleChildResultSetNode(ResultSetNode childResult, ContextManager cm) {
        super(null, cm);
        this.childResult = childResult;
    }

    public ResultSetNode getChildResult() {
        return childResult;
    }

    public void setChildResult(ResultSetNode childResult) {
        this.childResult = childResult;
    }

    @Override
    public ResultSetNode preprocess(int numTables, FromList fromList) {
        childResult = childResult.preprocess(numTables, fromList);
        return this;
    }

    @Override
    public void initAccessPaths(Optimizer optimizer) {
        super.initAccessPaths(optimizer);
        if (childResult instanceof Optimizable) {
            ((Optimizable) childResult).initAccessPaths(optimizer);
        }
    }
}
