package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.loader.GeneratedClass;
import com.cfs.sqlkv.util.ByteArray;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 19:43
 */
public abstract class StatementNode extends QueryTreeNode{

    public StatementNode(ContextManager contextManager){
        super(contextManager);
    }

    /**
     * 绑定Statement
     * */
    public void bindStatement() throws StandardException{

    }

    public ConstantAction makeConstantAction() throws StandardException {
        return	null;
    }




}
