package com.cfs.sqlkv.sql.activation;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.result.CursorResultSet;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.compile.sql.ExecPreparedStatement;
import com.cfs.sqlkv.compile.sql.GenericPreparedStatement;
import com.cfs.sqlkv.compile.sql.ParameterValueSet;
import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;

import com.cfs.sqlkv.engine.execute.CursorActivation;
import com.cfs.sqlkv.factory.*;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.service.loader.*;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 17:58
 */
public abstract class BaseActivation implements CursorActivation, GeneratedByteCode {

    public static final String className = BaseActivation.class.getName();

    public ExecRow[] row;
    private LanguageConnectionContext lcc;
    protected ContextManager cm;
    protected GenericPreparedStatement preStmt;
    protected ResultSet resultSet;
    protected ParameterValueSet pvs;

    private TableDescriptor ddlTableDescriptor;


    public BaseActivation() {
    }

    public BaseActivation(LanguageConnectionContext languageConnectionContext) {
        lcc = (LanguageConnectionContext) languageConnectionContext.getContextManager().getContext(LanguageConnectionContext.CONTEXT_ID);
    }


    public BaseActivation(LanguageConnectionContext languageConnectionContext, GenericPreparedStatement genericPreparedStatement) {
        this.lcc = (LanguageConnectionContext) languageConnectionContext.getContextManager().getContext(LanguageConnectionContext.CONTEXT_ID);
        this.preStmt = genericPreparedStatement;
    }


    @Override
    public GenericPreparedStatement getPreparedStatement() {
        return preStmt;
    }

    @Override
    public LanguageConnectionContext getLanguageConnectionContext() {
        return lcc;
    }


    @Override
    public ParameterValueSet getParameterValueSet() {
        if (pvs == null) {
            setParameterValueSet(0, false);
        }
        return pvs;
    }

    protected final void setParameterValueSet(int paramCount, boolean hasReturnParam) {
        GenericLanguageFactory genericLanguageFactory = lcc.getLanguageFactory();
        LanguageConnectionFactory languageConnectionFactory = lcc.getLanguageConnectionFactory();
        ClassFactory classFactory = languageConnectionFactory.getClassFactory();
        ClassInspector classInspector = classFactory.getClassInspector();
        pvs = genericLanguageFactory.newParameterValueSet(classInspector, paramCount, hasReturnParam);
    }

    /**
     * 通过激活器创建结果集,在这里会将执行器封装到结果集
     */
    @Override
    public ResultSet execute() {
        if (resultSet == null) {
            resultSet = createResultSet();
        }
        return resultSet;
    }

    public static void allocateQualArray(Qualifier[][] qualifiers, int position, int length) {
        qualifiers[position] = new Qualifier[length];
    }

    public static void setQualifier(Qualifier[][] qualifiers, Qualifier qualifier,
                                    int position_1, int position_2) {
        qualifiers[position_1][position_2] = qualifier;
    }

    @Override
    public ExecutionFactory getExecutionFactory() {
        return getLanguageConnectionContext().getLanguageConnectionFactory().getExecutionFactory();
    }

    @Override
    public ConstantAction getConstantAction() {
        return preStmt.getConstantAction();
    }

    private boolean forCreateTable;

    @Override
    public void setForCreateTable() {
        forCreateTable = true;
    }

    @Override
    public void setDDLTableDescriptor(TableDescriptor td) {
        ddlTableDescriptor = td;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return lcc.getTransactionExecute();
    }

    @Override
    public void setCurrentRow(ExecRow currentRow, int resultSetNumber) {

    }


    public final DataValueDescriptor getColumnFromRow(int rsNumber, int colId) {
        if (row[rsNumber] == null) {
            return null;
        }
        return row[rsNumber].getColumn(colId);
    }

    public static void reinitializeQualifiers(Qualifier[][] qualifiers) {
        if (qualifiers != null) {
            for (int term = 0; term < qualifiers.length; term++) {
                for (int i = 0; i < qualifiers[term].length; i++) {
                    qualifiers[term][i].reinitialize();
                }
            }
        }
    }

    public DataValueFactory getDataValueFactory() {
        return getLanguageConnectionContext().getDataValueFactory();
    }

    @Override
    public ResultDescription getResultDescription() {
        return resultDescription;
    }

    public final void setupActivation(GenericPreparedStatement ps) {
        resultDescription = ps.getResultDescription();
    }

    public ResultSet createResultSet() {
        return getResultSetFactory().getDDLResultSet(this);
    }

    public final ResultSetFactory getResultSetFactory() {
        return getExecutionFactory().getResultSetFactory();
    }

    @Override
    public CursorResultSet getTargetResultSet() {
        return null;
    }

    @Override
    public CursorResultSet getCursorResultSet() {
        return null;
    }

    protected boolean closed;

    public boolean isClosed() {
        return closed;
    }

    private String cursorName;

    public void setCursorName(String cursorName) {
        if (isCursorActivation())
            this.cursorName = cursorName;
    }

    public boolean isCursorActivation() {
        return false;
    }


    private boolean scrollable;
    protected ResultDescription resultDescription;

    public final void setupActivation(GenericPreparedStatement ps, boolean scrollable) {
        preStmt = ps;
        if (ps != null) {
            resultDescription = ps.getResultDescription();
            this.scrollable = scrollable;
        } else {
            resultDescription = null;
            this.scrollable = false;
        }
    }


    private boolean isValid;
    protected String UUIDString;
    protected UUID UUIDValue;
    private volatile boolean inUse;

    public final void initFromContext(Context context) {
        this.cm = context.getContextManager();
        lcc = (LanguageConnectionContext) cm.getContext(LanguageConnectionContext.CONTEXT_ID);
        inUse = true;
        lcc.addActivation(this);
        isValid = true;
        UUIDFactory uuidFactory = new UUIDFactory();
        UUIDValue = uuidFactory.createUUID();
        UUIDString = UUIDValue.toString();
    }

    private GeneratedClass gc;

    public final void setGC(GeneratedClass gc) {
        this.gc = gc;
    }

    public final GeneratedClass getGC() {
        return gc;
    }

    public final GeneratedMethod getMethod(String methodName) {
        return getGC().getMethod(methodName);
    }

    public Object e0() {
        return null;
    }

    public Object e1() {
        return null;
    }

    public Object e2() {
        return null;
    }

    public Object e3() {
        return null;
    }

    public Object e4() {
        return null;
    }

    public Object e5() {
        return null;
    }

    public Object e6() {
        return null;
    }

    public Object e7() {
        return null;
    }

    public Object e8() {
        return null;
    }

    public Object e9() {
        return null;
    }


}
