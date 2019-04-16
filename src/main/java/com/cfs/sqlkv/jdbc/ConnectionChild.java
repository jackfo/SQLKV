package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.context.LanguageConnectionContext;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.SQLException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 21:30
 */
public abstract class ConnectionChild {

    /**
     * 本地连接
     * */
    EmbedConnection localConn;

    /**
     * 连接对应的驱动
     * */
    final InternalDriver factory;

    public LanguageConnectionContext lcc;

    public ConnectionChild(EmbedConnection conn) {
        super();
        localConn = conn;
        factory = conn.getLocalDriver();
    }

    public final EmbedConnection getEmbedConnection() {
        return localConn;
    }


    /**
     * 获取连接的锁
     * */
    public final Object getConnectionSynchronization() {
        return localConn.getConnectionSynchronization();
    }

    public final void setupContextStack() throws SQLException {
        localConn.setupContextStack();
    }

    public final void restoreContextStack() throws SQLException {
        localConn.restoreContextStack();
    }

    /**
     * 获取连接对应的语言连接上下文
     * */
    public LanguageConnectionContext getLanguageConnectionContext( final EmbedConnection conn ) {
        if ( lcc == null ) {
            lcc = getLCC( conn );
        }
        return lcc;
    }

    public static LanguageConnectionContext	getLCC(final EmbedConnection conn ) {
        return conn.getLcc();
    }


    public final void commitIfNeeded() throws SQLException {
        localConn.commitIfNeeded();
    }

}
