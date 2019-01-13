package com.cfs.sqlkv.compile.node;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:23
 */

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.Visitable;
import com.cfs.sqlkv.context.LanguageConnectionContext;

/**
 * 所有查询树的root class
 * */
public class QueryTreeNode implements Visitable{

    private ContextManager contextManager;
    public QueryTreeNode(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    private int	beginOffset = -1;
    private int	endOffset = -1;

    public void setBeginOffset( int beginOffset ) {
        this.beginOffset = beginOffset;
    }
    public	int	getBeginOffset() {
        return beginOffset;
    }

    public int getEndOffset(){
        return endOffset;
    }

    public void setEndOffset( int endOffset ) {
        this.endOffset = endOffset;
    }

    final public DataDictionary getDataDictionary() {
        return getLanguageConnectionContext().getDataDictionary();
    }


    private LanguageConnectionContext lcc;
    /**
     * 通过上下文获取对应的语言连接上下文
     * */
    protected final LanguageConnectionContext getLanguageConnectionContext() {
        if (lcc == null) {
            lcc = (LanguageConnectionContext) getContextManager().getContext(LanguageConnectionContext.CONTEXT_ID);
        }
        return lcc;
    }

    final ContextManager getContextManager() {
        return contextManager;
    }

    protected final CompilerContext getCompilerContext() {
        return (CompilerContext) getContextManager().getContext(CompilerContext.CONTEXT_ID);
    }

}
