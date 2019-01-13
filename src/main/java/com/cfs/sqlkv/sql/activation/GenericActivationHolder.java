package com.cfs.sqlkv.sql.activation;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.compile.factory.ClassFactory;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.sql.GenericPreparedStatement;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.GenericExecutionFactory;
import com.cfs.sqlkv.factory.GenericLanguageFactory;
import com.cfs.sqlkv.factory.LanguageConnectionFactory;
import com.cfs.sqlkv.loader.ClassInspector;
import com.cfs.sqlkv.loader.GeneratedClass;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 17:36
 */
public class GenericActivationHolder implements Activation {

    public BaseActivation			ac;
    GenericPreparedStatement	ps;
    GeneratedClass			gc;
    DataTypeDescriptor[]	paramTypes;
    private final LanguageConnectionContext lcc;
    protected ParameterValueSet pvs;
    public GenericActivationHolder(LanguageConnectionContext lcc, GeneratedClass gc, GenericPreparedStatement ps, boolean scrollable) throws StandardException {
        this.lcc = lcc;
        this.gc = gc;
        this.ps = ps;
        ac = new BaseActivation(lcc,ps);
    }

    @Override
    public GenericPreparedStatement getPreparedStatement() {
        return ps;
    }

    @Override
    public LanguageConnectionContext getLanguageConnectionContext() {
        return lcc;
    }

    @Override
    public ParameterValueSet getParameterValueSet() {
        return ac.getParameterValueSet();
    }

    protected ResultSet resultSet;

    @Override
    public ResultSet execute() throws StandardException {
        return ac.execute();
    }

    @Override
    public GenericExecutionFactory getExecutionFactory() {
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


}
