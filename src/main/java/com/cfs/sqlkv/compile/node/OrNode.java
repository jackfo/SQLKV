package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-31 14:05
 */
public class OrNode extends ValueNode {
    public OrNode(ValueNode leftOperand, ValueNode rightOperand, ContextManager cm) {
        super(cm);
    }
}
