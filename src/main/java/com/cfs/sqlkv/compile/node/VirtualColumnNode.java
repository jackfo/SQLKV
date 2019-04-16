package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-13 11:04
 */
public class VirtualColumnNode extends ValueNode {

    /**
     * 源结果节点
     */
    private ResultSetNode sourceResultSet;
    /**
     * 源结果列
     */
    private ResultColumn sourceColumn;

    public int columnId;

    public VirtualColumnNode(ResultSetNode sourceResultSet, ResultColumn sourceColumn, int columnId, ContextManager cm) {
        super(cm);
        this.sourceResultSet = sourceResultSet;
        this.sourceColumn = sourceColumn;
        this.columnId = columnId;
        setType(sourceColumn.getTypeServices());
    }

    public ResultSetNode getSourceResultSet() {
        return sourceResultSet;
    }

    public ResultColumn getSourceColumn() {
        return sourceColumn;
    }

    @Override
    public String getTableName() {
        return sourceColumn.getTableName();
    }

    @Override
    public String getSchemaName() {
        return sourceColumn.getSchemaName();
    }

    @Override
    public ResultColumn getSourceResultColumn() {
        return sourceColumn;
    }

    @Override
    public void generateExpression(ActivationClassBuilder acb, MethodBuilder mb) {
        int sourceResultSetNumber = sourceColumn.getResultSetNumber();
        if (sourceColumn.isRedundant()) {
            sourceColumn.getExpression().generateExpression(acb, mb);
            return;
        }
        acb.pushColumnReference(mb, sourceResultSetNumber, sourceColumn.getVirtualColumnId());
        mb.cast(sourceColumn.getTypeCompiler().interfaceName());
    }

    @Override
    public final DataTypeDescriptor getTypeServices() {
        return sourceColumn.getTypeServices();
    }

    @Override
    public final void setType(DataTypeDescriptor dtd) {
        sourceColumn.setType(dtd);
    }

    private boolean correlated = false;

    public boolean getCorrelated() {
        return correlated;
    }

    public void setCorrelated() {
        correlated = true;
    }
}
