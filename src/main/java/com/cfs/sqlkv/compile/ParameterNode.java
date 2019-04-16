package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.ValueNode;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-28 11:42
 */
public class ParameterNode extends ValueNode{
    public ParameterNode(ContextManager contextManager) {
        super(contextManager);
    }
}
