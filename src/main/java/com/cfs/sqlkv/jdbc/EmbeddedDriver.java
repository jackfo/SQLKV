package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.common.Attribute;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 12:35
 */
public class EmbeddedDriver implements Driver{

    /**在加载驱动的时候开始执行*/
    static {
        EmbeddedDriver.init();
    }

    public EmbeddedDriver(){

    }

    /**
     * 根据jdbc做初始化
     * */
    public static void init(){
        new JDBC().init(Attribute.PROTOCOL);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        System.out.println();
        return null;
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
