package com.cfs.sqlkv.context;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.jdbc.EmbedConnection;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 13:51
 */
public class EmbedConnectionContext extends ContextImpl implements ConnectionContext {

    /**
     * 采用若连接的方式
     * */
    private java.lang.ref.SoftReference<EmbedConnection> connRef;

    public EmbedConnectionContext(ContextManager cm, EmbedConnection conn) {
        super(cm, ConnectionContext.CONTEXT_ID);
        connRef = new java.lang.ref.SoftReference<EmbedConnection>(conn);
    }

    protected EmbedConnectionContext(ContextManager cm, String id) {
        super(cm, id);
    }
}
