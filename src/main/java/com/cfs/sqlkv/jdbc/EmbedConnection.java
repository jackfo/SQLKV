package com.cfs.sqlkv.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:59
 */
public class EmbedConnection {

    TransactionResourceImpl transactionResourceImpl;

    /**root connection是所有行为的基础连接*/
    final EmbedConnection rootConnection;

    private InternalDriver internalDriver;


    private java.sql.Connection applicationConnection;


    /**基于已有的连接创建连接*/
    public EmbedConnection(EmbedConnection inputConnection){
        this.rootConnection = inputConnection.rootConnection;
    }

    /**
     * 通过驱动的连接方式是root connection
     * */
    public EmbedConnection(InternalDriver driver, String url, Properties info)throws SQLException {
           rootConnection = this;
           internalDriver = driver;
           transactionResourceImpl = new TransactionResourceImpl(driver,url,info);

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
    public final Statement createStatement() throws SQLException{
        return createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY,ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    /**
     * @param resultSetType 结果集类型 TYPE_FORWARD_ONLY只能向前滚动 TYPE_SCROLL_INSENSITIVE这两个方法都能够实现任意的前后滚动，
     * @param resultSetConcurrency 设置ResultSet对象能否修改的 CONCUR_READ_ONLY设置为只读类型的参数 CONCUR_UPDATABLE设置为可修改类型的参数
     * @param resultSetHoldability 结果集提交后结果集是否打开 HOLD_CURSORS_OVER_COMMIT表示修改提交时ResultSet不关闭 CLOSE_CURSORS_AT_COMMIT表示修改提交时ResultSet关闭
     * */
    public final Statement createStatement(int resultSetType,int resultSetConcurrency,int resultSetHoldability)throws SQLException{
        return new EmbedStatement(this, false, resultSetType, resultSetConcurrency, resultSetHoldability);
    }


    public final java.sql.Connection getApplicationConnection(){
        return applicationConnection;
    }


    final protected Object getConnectionSynchronization() {
        return rootConnection;
    }

}
