package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.context.LanguageConnectionContext;

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

    ConnectionChild(EmbedConnection conn) {
        super();
        localConn = conn;
        factory = conn.getLocalDriver();
    }

    final EmbedConnection getEmbedConnection() {
        return localConn;
    }




}
