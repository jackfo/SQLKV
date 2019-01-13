package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 14:03
 */
public class DefaultNode extends ValueNode{

    /**列的名字*/
    private String columnName;
    /**默认文本*/
    private String defaultText;
    /**默认的树*/
    private ValueNode defaultTree;

    public DefaultNode(ContextManager contextManager) {
        super(contextManager);
    }

    public DefaultNode(ValueNode defaultTree, String defaultText, ContextManager cm) {
        super(cm);
        this.defaultTree = defaultTree;
        this.defaultText = defaultText;
    }
}
