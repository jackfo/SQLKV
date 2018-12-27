package com.cfs.sqlkv.jdbc;



import com.cfs.sqlkv.common.SQLState;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @Description jdbc驱动
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
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

    /**
     * 核心连接过程
     * */
    public Connection connect(String url, Properties info, int loginTimeoutSeconds ) throws SQLException {

        if (!acceptsURL(url)) {
            return null;
        }

        /**对连接进行控制,防止连接过多,将数据库击穿*/
        //TODO:待实现


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

    protected boolean active;
    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return active && embeddedDriverAcceptsURL( url );
    }

    /**检测URL是否有效*/
    public static boolean embeddedDriverAcceptsURL(String url) throws SQLException {
        //如果URL为空,无法进行连接
        if(url==null){
            Util.generateCsSQLException(SQLState.UN_EXCEPTION,url);
        }
        return true;
    }
}
