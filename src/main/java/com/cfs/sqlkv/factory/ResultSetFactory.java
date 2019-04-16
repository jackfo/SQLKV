package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.service.loader.GeneratedMethod;
import com.cfs.sqlkv.sql.activation.Activation;
import com.cfs.sqlkv.store.access.Qualifier;

public interface ResultSetFactory {

    public ResultSet getMiscResultSet(Activation activation);

    public ResultSet getDDLResultSet(Activation activation);

    public NoPutResultSet getBulkTableScanResultSet(Activation activation, int resultSetNumber, int resultRowTemplate, int objectNum, int rowsPerRead, Qualifier[][] qualifiers);

    public NoPutResultSet getBaseProjectResult(NoPutResultSet source, GeneratedMethod restriction, GeneratedMethod projection, int mapArrayItem, int cloneMapItem, boolean doesProjection, int resultSetNumber);

    public ResultSet getInsertResultSet(NoPutResultSet source, String schemaName, String tableName);

    NoPutResultSet getRowResultSet(Activation activation, GeneratedMethod row, int resultSetNumber);

    public NoPutResultSet getTableScanResultSet(Activation activation, int resultSetNumber, int resultRowTemplate, int objectNum, int rowsPerRead, Qualifier[][] qualifiers);

    ResultSet getUpdateResultSet(NoPutResultSet source, GeneratedMethod generationClauses, GeneratedMethod checkGM);


    public ResultSet getDeleteResultSet(NoPutResultSet source);
}
