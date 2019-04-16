package com.cfs.sqlkv.jdbc;



import com.cfs.sqlkv.common.Attribute;
import com.cfs.sqlkv.common.SQLState;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.context.ConnectionContext;
import com.cfs.sqlkv.io.FormatableProperties;

import java.sql.*;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * @Description jdbc驱动
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:38
 */
public class InternalDriver implements Driver{

    private ContextService contextServiceFactory;
    private static InternalDriver activeDriver;
    static {
        try {
            activeDriver = new InternalDriver();
            DriverManager.registerDriver(activeDriver);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public InternalDriver(){
        contextServiceFactory = ContextService.getInstance();
        active =true;
    }



    public static final InternalDriver activeDriver() {
        return activeDriver;
    }


    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        System.out.println("进行网络连接");
        return connect(url, info, DriverManager.getLoginTimeout());
    }

    /**
     * 核心连接过程
     * */
    public Connection connect(String url, Properties info, int loginTimeoutSeconds ) throws SQLException {

        if (!acceptsURL(url)) {
            return null;
        }

        boolean current = url.equals(Attribute.SQLJ_NESTED);

        if(current){
            ConnectionContext connContext = getConnectionContext();
        }

        FormatableProperties finfo = null;

        try {
            finfo = getAttributes(url, info);
            //如果获取到数据库的操作 dbname不为空
            if (InternalDriver.getDatabaseName(url, finfo).length() == 0) {

            }
            EmbedConnection conn;
            if ( loginTimeoutSeconds <= 0 ) {
                conn = getNewEmbedConnection( url, finfo );
            } else {
                conn = timeLogin( url, finfo, loginTimeoutSeconds );
            }

            // if this is not the correct driver a EmbedConnection
            // object is returned in the closed state.
            if (conn.isClosed()) {
                return null;
            }

            return conn;
        }finally {
            if (finfo != null){
                finfo.clearDefaults();
            }
        }
    }

    /**
     * 进行登录连接,设定超时时间
     * */
    private EmbedConnection timeLogin(String url, Properties info, int loginTimeoutSeconds)throws SQLException{
       return null;
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

    private ConnectionContext getConnectionContext() {
        ContextManager cm = getCurrentContextManager();
        ConnectionContext localCC = null;
        if (cm != null) {
            localCC = (ConnectionContext) (cm.getContext(ConnectionContext.CONTEXT_ID));
        }
        return localCC;
    }

    /**
     * 通过上下文服务获取上下文管理器
     * */
    private ContextManager getCurrentContextManager() {
        return contextServiceFactory.getCurrentContextManager();
    }

    protected FormatableProperties getAttributes(String url, Properties info){
        FormatableProperties finfo = new FormatableProperties(info);
        info = null;
        StringTokenizer st = new StringTokenizer(url, ";");
        st.nextToken();
        while (st.hasMoreTokens()) {
            String v = st.nextToken();
            int eqPos = v.indexOf('=');
            if (eqPos == -1) {
                throw new RuntimeException("没有匹配的URL");
            }
            finfo.put((v.substring(0, eqPos)).trim(), (v.substring(eqPos + 1)).trim());
        }
        return finfo;
    }

    /**
     * 根据URL解析数据库名
     * */
    public static String getDatabaseName(String url, Properties info) {
        if (url.equals(Attribute.SQLJ_NESTED)) {
            return "";
        }
        int attributeStart = url.indexOf(';');
        String dbname;
        if (attributeStart == -1){
            dbname = url.substring(Attribute.PROTOCOL.length());
        }else{
            dbname = url.substring(Attribute.PROTOCOL.length(), attributeStart);
        }
        if (dbname.length() == 0) {
            if (info != null){
                dbname = info.getProperty(Attribute.DBNAME_ATTR, dbname);
            }
        }
        dbname = dbname.trim();
        return dbname;
    }

    public EmbedConnection getNewEmbedConnection( final String url, final Properties info){
        final   InternalDriver  myself = this;
        return new EmbedConnection(myself, url, info);
    }

    public final ContextService getContextService() {
        return contextServiceFactory;
    }

}
