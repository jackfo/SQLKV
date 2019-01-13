package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;

/**
 * @author zhengxiaokang
 * @Description CursorNode表示可以返回给客户端的结果集。
 *              游标可以是由DECLARE CURSOR语句创建的命名游标，
 *              或者它可以是与SELECT语句关联的未命名游标（更多
 *              确切地说，是一个将表返回给客户端的表表达式。 在里面
 *              后一种情况，光标没有名称
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 19:39
 */
public class CursorNode extends DMLStatementNode {

    public final static int READ_ONLY = 1;

    public CursorNode(ContextManager contextManager) {
        super(contextManager);
    }
}
