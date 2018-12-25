package com.cfs.sqlkv.jdbc;

import org.apache.derby.impl.jdbc.EmbedConnection;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 17:03
 */
public class LoginCallable implements Callable<EmbedConnection> {

    private InternalDriver driver;
    private String url;
    private Properties info;

    public LoginCallable(InternalDriver driver, String url, Properties info) {
        this.driver = driver;
        this.url = url;
        this.info = info;
    }

    public EmbedConnection call() throws SQLException {
        return driver.getNewEmbedConnection(url, info);
    }

}
