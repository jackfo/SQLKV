package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 21:02
 */
public abstract class JavaValueNode extends QueryTreeNode {
    public JavaValueNode(ContextManager contextManager) {
        super(contextManager);
    }
}
