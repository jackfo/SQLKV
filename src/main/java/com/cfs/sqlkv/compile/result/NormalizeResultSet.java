package com.cfs.sqlkv.compile.result;


import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-03 12:14
 */
public class NormalizeResultSet extends NoPutResultSetImpl implements CursorResultSet {

    public NoPutResultSet source;
    private ExecRow normalizedRow;
    private int numCols;
    private int startCol;
    private ResultDescription resultDescription;
    /**
     * 数据类型描述
     */
    private DataTypeDescriptor[] desiredTypes;

    private final DataValueDescriptor[] cachedDestinations;

    public NormalizeResultSet(NoPutResultSet source, Activation activation,
                              int erdNumber, int resultSetNumber, boolean forUpdate) {
        super(activation, resultSetNumber, 0, 0);
        this.source = source;
        this.resultDescription = (ResultDescription) activation.getPreparedStatement().getSavedObject(erdNumber);
        numCols = resultDescription.getColumnCount();
        startCol = computeStartColumn(forUpdate, resultDescription);
        normalizedRow = new ValueRow(numCols);
        cachedDestinations = new DataValueDescriptor[numCols];
    }

    public static int computeStartColumn(boolean isUpdate, ResultDescription desc) {
        int count = desc.getColumnCount();
        return (isUpdate) ? ((count - 1) / 2) + 1 : 1;
    }

    public void openCore() {
        source.openCore();
        isOpen = true;
    }

    public void reopenCore() {
        source.reopenCore();
    }

    @Override
    public ExecRow getNextRowCore() {
        ExecRow sourceRow = null;
        ExecRow result = null;
        if (!isOpen) {
            throw new RuntimeException("ResultSet not open");
        }
        sourceRow = source.getNextRowCore();
        if (sourceRow != null) {
            result = normalizeRow(sourceRow);
        }
        return null;
    }

    private ExecRow normalizeRow(ExecRow sourceRow) {
        int count = resultDescription.getColumnCount();
        for (int i = 1; i <= count; i++) {
            DataValueDescriptor sourceCol = sourceRow.getColumn(i);
            if (sourceCol != null) {
                DataValueDescriptor normalizedCol;
                if (i < startCol) {
                    normalizedCol = sourceCol;
                } else {
                    normalizedCol = normalizeColumn(
                            getDesiredType(i), sourceRow, i,
                            getCachedDestination(i), resultDescription);
                }
                normalizedRow.setColumn(i, normalizedCol);
            }
        }
        return normalizedRow;
    }

    private DataTypeDescriptor getDesiredType(int col) {
        if (desiredTypes == null) {
            desiredTypes = fetchResultTypes(resultDescription);
        }
        return desiredTypes[col - 1];
    }

    /**
     * 获取结果描述的类型
     */
    private DataTypeDescriptor[] fetchResultTypes(ResultDescription desc) {
        int count = desc.getColumnCount();
        DataTypeDescriptor[] result = new DataTypeDescriptor[count];
        for (int i = 1; i <= count; i++) {
            ResultColumnDescriptor colDesc = desc.getColumnDescriptor(i);
            DataTypeDescriptor dtd = colDesc.getType();
            result[i - 1] = dtd;
        }
        return result;
    }

    public static DataValueDescriptor normalizeColumn(DataTypeDescriptor dtd, ExecRow sourceRow, int sourceColumnPosition, DataValueDescriptor resultCol, ResultDescription desc) {
        DataValueDescriptor sourceCol = sourceRow.getColumn(sourceColumnPosition);
        DataValueDescriptor returnValue = dtd.normalize(sourceCol, resultCol);
        return returnValue;
    }

    /**
     * 获取缓存的描述
     */
    private DataValueDescriptor getCachedDestination(int col) {
        int index = col - 1;
        if (cachedDestinations[index] == null) {
            cachedDestinations[index] = getDesiredType(col).getNull();
        }
        return cachedDestinations[index];
    }

    public TableRowLocation getRowLocation() {
        return ((CursorResultSet) source).getRowLocation();
    }

    public void markRowAsDeleted(){
        source.markRowAsDeleted();
    }

}
