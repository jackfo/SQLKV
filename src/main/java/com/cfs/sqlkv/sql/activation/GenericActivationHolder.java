package com.cfs.sqlkv.sql.activation;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.sql.GenericPreparedStatement;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;

import com.cfs.sqlkv.factory.*;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.ClassInspector;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 17:36
 */
public class GenericActivationHolder implements Activation {

    public BaseActivation ac;
    GenericPreparedStatement ps;
    GeneratedClass gc;
    DataTypeDescriptor[] paramTypes;
    private final LanguageConnectionContext lcc;
    protected ParameterValueSet pvs;

    public GenericActivationHolder(LanguageConnectionContext lcc, GeneratedClass gc, GenericPreparedStatement ps, boolean scrollable) {
        this.lcc = lcc;
        this.gc = gc;
        this.ps = ps;
        ac = (BaseActivation) gc.newInstance(lcc);
        ac.setupActivation(ps, scrollable);
        paramTypes = ps.getParameterTypes();
        ac.setupActivation(ps);
    }

    @Override
    public GenericPreparedStatement getPreparedStatement() {
        return ps;
    }

    @Override
    public LanguageConnectionContext getLanguageConnectionContext() {
        return lcc;
    }

    public DataValueFactory getDataValueFactory() {
        return ac.getDataValueFactory();
    }

    @Override
    public ResultDescription getResultDescription() {
        return ac.getResultDescription();
    }

    @Override
    public ParameterValueSet getParameterValueSet() {
        return ac.getParameterValueSet();
    }

    protected ResultSet resultSet;

    @Override
    public ResultSet execute() {
        return ac.execute();
    }

    @Override
    public ExecutionFactory getExecutionFactory() {
        return ac.getExecutionFactory();
    }

    @Override
    public ConstantAction getConstantAction() {
        return ac.getConstantAction();
    }

    @Override
    public void setForCreateTable() {
        ac.setForCreateTable();
    }

    @Override
    public void setDDLTableDescriptor(TableDescriptor td) {
        ac.setDDLTableDescriptor(td);
    }

    @Override
    public TransactionManager getTransactionManager() {
        return null;
    }

    @Override
    public void setCurrentRow(ExecRow currentRow, int resultSetNumber) {

    }


}
