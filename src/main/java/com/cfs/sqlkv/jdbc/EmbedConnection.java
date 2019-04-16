package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.Database;
import com.cfs.sqlkv.common.Attribute;
import com.cfs.sqlkv.common.PersistentService;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.EmbedConnectionContext;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.db.BasicDatabase;

import com.cfs.sqlkv.factory.StorageFactory;
import com.cfs.sqlkv.io.BaseStorageFactory;
import com.cfs.sqlkv.io.storage.DirStorageFactory;
import com.cfs.sqlkv.io.storage.StorageFile;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:59
 */
public class EmbedConnection implements Connection {

    TransactionResourceImpl transactionResourceImpl;

    /**root connection是所有行为的基础连接*/
    final EmbedConnection rootConnection;

    private InternalDriver internalDriver;

    private final StorageFactory storageFactory;
    private java.sql.Connection applicationConnection;

    private boolean	active;

    /**基于已有的连接创建连接*/
    public EmbedConnection(EmbedConnection inputConnection){
        this.rootConnection = inputConnection.rootConnection;
        storageFactory = inputConnection.getStorageFactory();
    }

    public StorageFactory getStorageFactory(){
        return this.storageFactory;
    }

    /**
     * 通过驱动的连接方式是root connection
     * */
    public EmbedConnection(InternalDriver driver, String url, Properties info){
           applicationConnection=rootConnection = this;
           internalDriver = driver;
           //创建数据库
           Database database = new BasicDatabase();
           //将数据拒进行初始化和管理
           transactionResourceImpl = new TransactionResourceImpl(driver,url,info,database);
           storageFactory = new DirStorageFactory(System.getProperty(PersistentService.ROOT));
           storageFactory.setDataDirectory(transactionResourceImpl.getDbname());
           active = true;
           try {
                setupContextStack();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try{
                EmbedConnectionContext context = pushConnectionContext(transactionResourceImpl.getContextManager());
            }finally {
            }
    }

    private static boolean isTrue(Properties p, String attribute) {
        return Boolean.valueOf(p.getProperty(attribute)).booleanValue();
    }

    protected final void setupContextStack() throws SQLException {
        getTransactionResourceImpl().setupContextStack();
    }

    protected final void restoreContextStack() throws SQLException {
        TransactionResourceImpl tr = getTransactionResourceImpl();
        tr.restoreContextStack();
    }

    /**
     * 通过连接来获取网络驱动
     * */
    public final InternalDriver getLocalDriver(){

        return getTransactionResourceImpl().getDriver();

    }

    final protected TransactionResourceImpl getTransactionResourceImpl(){
        return rootConnection.transactionResourceImpl;
    }



    /**
     * 默认的Statement创建方式
     * 设置为只能向前滚动 只读 修改之后不关闭
     * */
    @Override
    public final Statement createStatement() throws SQLException{
        return createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return null;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {

    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return false;
    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {

    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {

    }

    @Override
    public String getCatalog() throws SQLException {
        return null;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {

    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

    }

    @Override
    public void setHoldability(int holdability) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return null;
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {

    }

    /**
     * @param resultSetType 结果集类型 TYPE_FORWARD_ONLY只能向前滚动 TYPE_SCROLL_INSENSITIVE这两个方法都能够实现任意的前后滚动，
     * @param resultSetConcurrency 设置ResultSet对象能否修改的 CONCUR_READ_ONLY设置为只读类型的参数 CONCUR_UPDATABLE设置为可修改类型的参数
     * @param resultSetHoldability 结果集提交后结果集是否打开 HOLD_CURSORS_OVER_COMMIT表示修改提交时ResultSet不关闭 CLOSE_CURSORS_AT_COMMIT表示修改提交时ResultSet关闭
     * */
    public final Statement createStatement(int resultSetType,int resultSetConcurrency,int resultSetHoldability)throws SQLException{
        return new EmbedStatement(this, false, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return null;
    }

    @Override
    public Clob createClob() throws SQLException {
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException {
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException {
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return false;
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {

    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {

    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;
    }

    @Override
    public void setSchema(String schema) throws SQLException {

    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }


    public final java.sql.Connection getApplicationConnection(){
        return applicationConnection;
    }



    final protected Object getConnectionSynchronization() {
        return rootConnection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }


    private EmbedConnectionContext pushConnectionContext(ContextManager cm) {
        return new EmbedConnectionContext(cm, this);
    }

    public LanguageConnectionContext getLcc(){
        return getTransactionResourceImpl().getLcc();
    }

    public void commitIfNeeded() throws SQLException {
        getTransactionResourceImpl().commit();
    }

}
