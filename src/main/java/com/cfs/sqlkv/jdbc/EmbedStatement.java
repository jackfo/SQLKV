package com.cfs.sqlkv.jdbc;


import java.sql.*;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:14
 */
public class EmbedStatement extends ConnectionChild implements Statement {

    private final java.sql.Connection applicationConnection;

    /**
     * 由于继承ConnectionChild 所以带相应的连接和驱动
     * */
    public EmbedStatement (EmbedConnection connection, boolean forMetaData, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        super(connection);
        applicationConnection = getEmbedConnection().getApplicationConnection();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    /**
     * 执行一条SQL语句
     * */
    @Override
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    /**
     * @param sql 需要执行的SQL语句
     * @param executeQuery 是否是查询语句
     * @param executeUpdate 是否是更新语句
     * @param autoGeneratedKeys 自动生成主键标识
     * @param columnIndexes 列的索引
     * @param columnNames 列名
     * */
    private boolean execute(String sql,boolean executeQuery, boolean executeUpdate,int autoGeneratedKeys, int[] columnIndexes, String[] columnNames){

        //在执行语句的时候需要锁住整个连接对象,从而实现当前连接的操作时串行化
        synchronized(getConnectionSynchronization()){

            //TODO:检查执行状态

            if(sql==null){
                throw new RuntimeException("SQL语句不能为空");
            }

            //TODO:清除结果集


        }
    }



    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }


    final Object getConnectionSynchronization() {
        return localConn.getConnectionSynchronization();
    }

    /**
     * @param executeQuery 标识当前语句是否是查询语句
     * @param executeUpdate 标识当前语句是否是更新语句
     * */
    boolean executeStatement(boolean executeQuery,boolean executeUpdate){

        synchronized (getConnectionSynchronization()){
            //设置当前上下文

            //获取参数结果集



        }
    }
}
