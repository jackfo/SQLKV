package com.cfs.sqlkv.compile.predicate;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.RelationalOperator;
import com.cfs.sqlkv.compile.node.AndNode;
import com.cfs.sqlkv.compile.node.QueryTreeNode;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 16:08
 */
public class Predicate extends QueryTreeNode implements OptimizablePredicate, Comparable<Predicate> {

    public AndNode andNode;
    public boolean pushable;
    protected boolean isQualifier;
    public int indexPosition;

    public Predicate(AndNode andNode, ContextManager cm) {
        super(cm);
        this.andNode = andNode;
        pushable = false;
    }

    @Override
    public int compareTo(Predicate other) {
        return 0;
    }

    public int getIndexPosition() {
        return indexPosition;
    }

    RelationalOperator getRelop() {

        if (andNode.getLeftOperand() instanceof RelationalOperator) {
            return (RelationalOperator) andNode.getLeftOperand();
        } else {
            return null;
        }
    }

    public boolean isQualifier() {
        return isQualifier;
    }

    public void markQualifier() {
        isQualifier = true;
    }

    public AndNode getAndNode() {
        return andNode;
    }

    public void clearScanFlags() {
        isQualifier = false;
    }


}
