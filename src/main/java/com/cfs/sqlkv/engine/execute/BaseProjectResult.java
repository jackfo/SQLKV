package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.catalog.ReferencedColumnsDescriptorImpl;
import com.cfs.sqlkv.compile.result.CursorResultSet;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.NoPutResultSetImpl;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.Arrays;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-12 13:24
 */
public class BaseProjectResult extends NoPutResultSetImpl implements CursorResultSet {

    final NoPutResultSet source;
    public GeneratedMethod constantRestriction;
    public GeneratedMethod restriction;
    public boolean doesProjection;
    private GeneratedMethod projection;
    private int[] projectMapping;
    public boolean reuseResult;
    private boolean[] cloneMap;
    private ExecRow mappedResultRow;

    public BaseProjectResult(NoPutResultSet source, Activation a, GeneratedMethod r, GeneratedMethod p, int resultSetNumber, GeneratedMethod cr, boolean reuseResult) {
        super(a, resultSetNumber, 0, 0);
        this.source = source;
        restriction = r;
        projection = p;
        constantRestriction = cr;
        this.reuseResult = reuseResult;

    }

    public BaseProjectResult(NoPutResultSet source, Activation a, GeneratedMethod r, GeneratedMethod p, int mapRefItem, int cloneMapItem, boolean doesProjection, int resultSetNumber) {
        super(a, resultSetNumber, 0, 0);
        this.source = source;
        restriction = r;
        projection = p;
        projectMapping = ((ReferencedColumnsDescriptorImpl) a.getPreparedStatement().getSavedObject(mapRefItem)).getReferencedColumnPositions();
        this.doesProjection = doesProjection;
        cloneMap = ((boolean[]) a.getPreparedStatement().getSavedObject(cloneMapItem));
        if (projection == null) {
            mappedResultRow = activation.getExecutionFactory().getValueRow(projectMapping.length);
        }

    }

    public void openCore() {
        source.openCore();
        isOpen = true;
    }

    public TableRowLocation getRowLocation() {
        return ((CursorResultSet) source).getRowLocation();
    }


    @Override
    public ExecRow getNextRowCore() {
        ExecRow result = null;
        ExecRow candidateRow = source.getNextRowCore();
        if (candidateRow != null){
            result = doProjection(candidateRow);
        }
        currentRow = result;
        return result;
    }


    private ExecRow doProjection(ExecRow sourceRow) {
        ExecRow result;
        if (projection != null) {
            result = (ExecRow) projection.invoke(activation);
        } else {
            result = mappedResultRow;
        }
        for (int index = 0; index < projectMapping.length; index++) {
            if (projectMapping[index] != -1) {
                DataValueDescriptor dvd = sourceRow.getColumn(projectMapping[index]);
                result.setColumn(index + 1, dvd);
            }
        }
        setCurrentRow(result);
        return result;
    }

    public void updateRow(ExecRow row, RowChanger rowChanger) {
        source.updateRow(row, rowChanger);
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void close() {

    }

    public void markRowAsDeleted(){
        source.markRowAsDeleted();
    }

}
