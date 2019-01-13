package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 21:02
 */
public class JavaToSQLValueNode extends ValueNode{
    private JavaValueNode javaNode;
    public JavaToSQLValueNode(ContextManager contextManager) {
        super(contextManager);
    }

    public JavaToSQLValueNode(JavaValueNode value, ContextManager cm) {
        super(cm);
        this.javaNode = value;
    }

    public JavaValueNode getJavaValueNode() {
        return javaNode;
    }
}
