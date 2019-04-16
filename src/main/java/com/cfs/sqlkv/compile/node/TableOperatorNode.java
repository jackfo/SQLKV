package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 21:12
 */
public class TableOperatorNode extends FromTable {
    public TableOperatorNode(ContextManager contextManager) {
        super(null,contextManager);
    }
}
