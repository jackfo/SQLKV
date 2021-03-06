package com.cfs.sqlkv.sql.activation;

import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.sql.GenericPreparedStatement;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.ExecutionFactory;
import com.cfs.sqlkv.factory.GenericExecutionFactory;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;


/**
 * @author zhengxiaokang
 * @Description 生成相应的执行计划
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 16:50
 */
public interface Activation {

    GenericPreparedStatement getPreparedStatement();

    public LanguageConnectionContext getLanguageConnectionContext();

    /**
     * 获取Statement的参数结果集
     */
    ParameterValueSet getParameterValueSet();

    ResultSet execute();

    ExecutionFactory getExecutionFactory();

    public ConstantAction getConstantAction();

    /**
     * 标记当前Activation已经在准备创建表
     */
    public void setForCreateTable();

    public void setDDLTableDescriptor(TableDescriptor td);


    TransactionManager getTransactionManager();

    void setCurrentRow(ExecRow currentRow, int resultSetNumber);

    public DataValueFactory getDataValueFactory();

    public ResultDescription getResultDescription();
}
