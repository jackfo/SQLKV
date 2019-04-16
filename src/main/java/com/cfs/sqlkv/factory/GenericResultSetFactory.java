package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.compile.result.*;

import com.cfs.sqlkv.engine.execute.BaseProjectResult;
import com.cfs.sqlkv.engine.execute.DeleteResultSet;
import com.cfs.sqlkv.engine.execute.UpdateResultSet;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Heap;
import com.cfs.sqlkv.store.access.Qualifier;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:07
 */
public class GenericResultSetFactory implements ResultSetFactory {

    public GenericResultSetFactory() {
    }

    @Override
    public ResultSet getDDLResultSet(Activation activation) {
        return getMiscResultSet(activation);
    }

    @Override
    public ResultSet getMiscResultSet(Activation activation) {
        return new MiscResultSet(activation);
    }

    @Override
    public NoPutResultSet getBulkTableScanResultSet(Activation activation, int resultSetNumber, int resultRowTemplate, int objectNum, int rowsPerRead, Qualifier[][] qualifiers) {
        Heap heap = (Heap) activation.getPreparedStatement().getSavedObject(objectNum);
        return new BulkTableScanResultSet(heap, 0, activation, resultSetNumber, resultRowTemplate, rowsPerRead, "noinnner", qualifiers);
    }

    @Override
    public NoPutResultSet getTableScanResultSet(Activation activation, int resultSetNumber, int resultRowTemplate, int objectNum, int rowsPerRead, Qualifier[][] qualifiers) {
        Heap heap = (Heap) activation.getPreparedStatement().getSavedObject(objectNum);
        return new TableScanResultSet(heap, 0, activation, resultSetNumber, resultRowTemplate, rowsPerRead, "noinnner", qualifiers);
    }

    public NoPutResultSet getBaseProjectResult(NoPutResultSet source, GeneratedMethod restriction, GeneratedMethod projection, int mapArrayItem, int cloneMapItem, boolean doesProjection, int resultSetNumber) {
        Activation activation = source.getActivation();
        return new BaseProjectResult(source, activation, restriction, projection, mapArrayItem, cloneMapItem, doesProjection, resultSetNumber);
    }

    @Override
    public ResultSet getInsertResultSet(NoPutResultSet source, String schemaName, String tableName) {
        Activation activation = source.getActivation();
        return new InsertResultSet(source, schemaName, tableName, activation);
    }

    @Override
    public NoPutResultSet getRowResultSet(Activation activation, GeneratedMethod row, int resultSetNumber) {
        return new RowResultSet(activation, row, resultSetNumber);
    }

    @Override
    public ResultSet getUpdateResultSet(NoPutResultSet source, GeneratedMethod generationClauses, GeneratedMethod checkGM) {
        Activation activation = source.getActivation();
        return new UpdateResultSet(source, generationClauses, checkGM, activation);
    }

    public ResultSet getDeleteResultSet(NoPutResultSet source) {
        Activation activation = source.getActivation();
        return new DeleteResultSet(source, activation);
    }
}
