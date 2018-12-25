package com.cfs.sqlkv.jdbc;



import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @Description jdbc驱动
 * @auther zhengxiaokang
 * @Email zhengxiaokang@qq.com
 * @create 2018-12-15 16:38
 */
public class InternalDriver implements Driver{

    static {


    }

    private static InternalDriver activeDriver;

    public static final InternalDriver activeDriver() {
        return activeDriver;
    }


    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return connect(url, info, DriverManager.getLoginTimeout());
    }

    public Connection connect( String url, Properties info, int loginTimeoutSeconds ){


    }

    /**
     * 进行登录连接,设定超时时间
     * */
    private EmbedConnection timeLogin(String url, Properties info, int loginTimeoutSeconds)throws SQLException{

    }

    EmbedConnection getConnection( final String url, final Properties info) throws SQLException{
        return new EmbedConnection(this, url, info);
    }


    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
