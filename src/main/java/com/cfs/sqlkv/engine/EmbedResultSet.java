package com.cfs.sqlkv.engine;

import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.context.StatementContext;

import com.cfs.sqlkv.jdbc.ConnectionChild;
import com.cfs.sqlkv.jdbc.EmbedConnection;
import com.cfs.sqlkv.jdbc.EmbedStatement;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-10 20:48
 */
public class EmbedResultSet extends ConnectionChild implements EngineResultSet {


    private boolean isOnInsertRow;
    private ExecRow currentRow;
    private ResultSet theResults;
    private final ResultDescription resultDescription;

    private long maxRows;
    private static long fetchedRowBase = 0L;
    private long NumberofFetchedRows = fetchedRowBase;

    private final int concurrencyOfThisResultSet;
    private final ExecRow updateRow;

    protected boolean wasNull;

    private final boolean isAtomic;
    private final EmbedStatement stmt;
    private EmbedStatement owningStmt;
    private Statement applicationStmt;
    protected static final int FIRST = 1;
    protected static final int NEXT = 2;
    protected static final int LAST = 3;
    protected static final int PREVIOUS = 4;
    protected static final int BEFOREFIRST = 5;
    protected static final int AFTERLAST = 6;
    protected static final int ABSOLUTE = 7;
    protected static final int RELATIVE = 8;

    public EmbedResultSet(EmbedConnection conn, ResultSet resultsToWrap, boolean forMetaData, EmbedStatement stmt, boolean isAtomic) {
        super(conn);
        theResults = resultsToWrap;
        resultDescription = theResults.getResultDescription();
        /**
         * 默认结果集是只支持只读,当指定参数当前设定当前结果集可直接修改,需要结果集类型本身支持
         * */
        if (stmt == null) {
            concurrencyOfThisResultSet = java.sql.ResultSet.CONCUR_READ_ONLY;
        } else if (stmt.resultSetConcurrency == java.sql.ResultSet.CONCUR_READ_ONLY) {
            concurrencyOfThisResultSet = java.sql.ResultSet.CONCUR_READ_ONLY;
        } else {
            if (!isForUpdate()) {
                concurrencyOfThisResultSet = java.sql.ResultSet.CONCUR_READ_ONLY;
            } else {
                concurrencyOfThisResultSet = java.sql.ResultSet.CONCUR_UPDATABLE;
            }
        }
        this.isAtomic = isAtomic;
        this.applicationStmt = this.stmt = owningStmt = stmt;
        if (concurrencyOfThisResultSet == java.sql.ResultSet.CONCUR_UPDATABLE) {
            final int columnCount = resultDescription.getColumnCount();

            columnGotUpdated = new boolean[columnCount];
            updateRow = new ValueRow(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                updateRow.setColumn(i, resultDescription.getColumnDescriptor(i).getType().getNull());
            }
            initializeUpdateRowModifiers();

        } else {
            updateRow = null;
        }

    }

    @Override
    public boolean next() throws SQLException {
        /**
         * 检测下一行是否大于最大限定行
         * */
        if (maxRows != 0) {
            NumberofFetchedRows++;
            if (NumberofFetchedRows > maxRows) {
                closeCurrentStream();
                return false;
            }
        }
        return movePosition(NEXT, 0, "next");
    }


    protected boolean movePosition(int position, int row, String positionText) throws SQLException {
        closeCurrentStream();
        //检查结果集和连接是否打开
        checkExecIfClosed(positionText);
        //如果当前查询的行是正在插入的行
        if (isOnInsertRow) {
            moveToCurrentRow();
        }

        synchronized (getConnectionSynchronization()) {
            setupContextStack();
        }
        try {
            LanguageConnectionContext lcc = getLanguageConnectionContext(getEmbedConnection());
            final ExecRow newRow;
            try {
                boolean isForReadOnly = concurrencyOfThisResultSet == java.sql.ResultSet.CONCUR_READ_ONLY;
                StatementContext statementContext = lcc.pushStatementContext(isAtomic, isForReadOnly, getSQLText(), getParameterValueSet(), false, 0);
                switch (position) {
                    case NEXT:
                        newRow = theResults.getNextRow();
                        break;
                    default:
                        newRow = null;
                }
                lcc.popStatementContext(statementContext, null);
                boolean onRow = (currentRow = newRow) != null;
                return onRow;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } finally {
            restoreContextStack();
        }

        throw new RuntimeException("");
    }

    private String getSQLText() {
        if (stmt == null) {
            return null;
        }
        return stmt.getSQLText();
    }

    private ParameterValueSet getParameterValueSet() {
        if (stmt == null) {
            return null;
        }
        return stmt.getParameterValueSet();
    }


    /**
     * 检查结果集和连接是否打开
     */
    public final void checkExecIfClosed(String operation) throws SQLException {
        if (theResults.isClosed()) {
            throw new RuntimeException(String.format("ResultSet not open，operation is %s", operation));
        }
        java.sql.Connection appConn = getEmbedConnection().getApplicationConnection();
        if (appConn == null) {
            throw new RuntimeException("no current connection");
        }
        if (appConn.isClosed()) {
            throw new RuntimeException("current connection is closed");
        }
    }

    private Object currentStream;

    /**
     * 关闭当前流
     */
    private final void closeCurrentStream() {
        if (currentStream != null) {
            try {
                synchronized (this) {
                    if (currentStream != null) {
                        if (currentStream instanceof java.io.Reader) {
                            ((java.io.Reader) currentStream).close();
                        } else {
                            ((java.io.InputStream) currentStream).close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }


    @Override
    public String getString(int columnIndex) throws SQLException {
        int columnType = getColumnType(columnIndex);
        try {

            DataValueDescriptor dvd = getColumn(columnIndex);
            if (wasNull = dvd.isNull()) {
                return null;
            }
            String value = dvd.getString();
            return value;

        } catch (Throwable t) {
            t.getCause();
            throw new RuntimeException(t.getMessage());
        }
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        DataValueDescriptor dataValueDescriptor = getColumn(columnIndex);
        if (wasNull = dataValueDescriptor.isNull()) {
            return 0;
        }
        return dataValueDescriptor.getInt();
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(String columnName) throws SQLException {
        return (getInt(findColumnName(columnName)));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {

    }

    @Override
    public void afterLast() throws SQLException {

    }

    @Override
    public boolean first() throws SQLException {
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        return 0;
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
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
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return concurrencyOfThisResultSet;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        checkExecIfClosed("moveToCurrentRow");

        //检查结果集是否支持更新
        checkUpdatableCursor("moveToCurrentRow");

        /**
         * 获取当前连接,保证操作原子性
         * 之后更新columnGotUpdated和currentRowHasBeenUpdated来设置当前行和对应的列是否可以进行更新
         * */
        synchronized (getConnectionSynchronization()) {
            //初始化insertRow/updateRow的插入状态
            if (isOnInsertRow) {
                initializeUpdateRowModifiers();
                isOnInsertRow = false;
            }
        }
    }

    /**
     * 判断列是否可以进行更新
     */
    private boolean[] columnGotUpdated;
    private boolean currentRowHasBeenUpdated;

    private void initializeUpdateRowModifiers() {
        currentRowHasBeenUpdated = false;
        Arrays.fill(columnGotUpdated, false);
    }


    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    /**
     * 返回列对应的JDBC类型
     *
     * @throws SQLException ResultSet 不是一行或者列索引超过范围
     */
    public final int getColumnType(int columnIndex) throws SQLException {
        //检测当前列是存在
        if (!isOnInsertRow) {
            checkOnRow();
        }

        if (columnIndex < 1 || columnIndex > resultDescription.getColumnCount()) {
            throw new RuntimeException(String.format("Column ''s%'' not found.", columnIndex));
        }
        return resultDescription.getColumnDescriptor(columnIndex).getType().getJDBCTypeId();
    }


    protected final void checkOnRow() throws SQLException {
        if (currentRow == null) {
            throw new RuntimeException("Invalid cursor state - no current row.");
        }
    }

    private void checkUpdatableCursor(String operation) throws SQLException {
        if (getConcurrency() != java.sql.ResultSet.CONCUR_UPDATABLE) {
            throw new RuntimeException(String.format("s% not allowed because the ResultSet is not an updatable ResultSet.", operation));
        }
    }

    public final boolean isForUpdate() {
        if (theResults instanceof NoPutResultSet) {
            return ((NoPutResultSet) theResults).isForUpdate();
        }
        return false;
    }

    /**
     * 根据列名找索引
     */
    public int findColumnName(String columnName) throws SQLException {
        if (columnName == null) {
            throw new RuntimeException("Column name cannot be null");
        }
        int position = resultDescription.findColumnInsenstive(columnName);
        if (position == -1) {
            throw new RuntimeException(String.format("Column ''s%'' not found.", columnName));
        } else {
            return position;
        }
    }

    protected final DataValueDescriptor getColumn(int columnIndex)
            throws SQLException {

        closeCurrentStream();
        if (columnIndex < 1 || columnIndex > resultDescription.getColumnCount()) {
            throw new RuntimeException(String.format("column not found [%d]", columnIndex));
        }
        if (isOnInsertRow || currentRowHasBeenUpdated && columnGotUpdated[columnIndex - 1]) {
            return updateRow.getColumn(columnIndex);
        } else {
            checkOnRow(); // make sure there's a row
            return currentRow.getColumn(columnIndex);
        }
    }

}
