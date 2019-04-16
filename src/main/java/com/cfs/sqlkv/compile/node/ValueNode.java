package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.TypeCompiler;
import com.cfs.sqlkv.compile.predicate.PredicateList;
import com.cfs.sqlkv.compile.result.ResultColumn;
import com.cfs.sqlkv.service.compiler.MethodBuilder;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 19:50
 */
public class ValueNode extends QueryTreeNode {

    public ValueNode(ContextManager contextManager) {
        super(contextManager);
    }


    public final void setType(TypeId typeId, boolean isNullable, int maximumWidth) {
        setType(new DataTypeDescriptor(typeId, isNullable, maximumWidth));
    }

    public final void setType(TypeId typeId,
                              int precision, int scale,
                              boolean isNullable,
                              int maximumWidth) {
        setType(new DataTypeDescriptor(
                typeId,
                precision,
                scale,
                isNullable,
                maximumWidth));
    }

    private DataTypeDescriptor dataTypeServices;

    public void setType(DataTypeDescriptor dataTypeServices) {
        this.dataTypeServices = dataTypeServices;
    }


    public String getColumnName() {
        return null;
    }

    public String getSchemaName() {
        return null;
    }


    public String getTableName() {
        return null;
    }

    public DataTypeDescriptor getTypeServices() {
        return dataTypeServices;
    }

    public void generateExpression(ActivationClassBuilder acb, MethodBuilder mb) {

    }

    public final TypeCompiler getTypeCompiler() {
        return getTypeCompiler(getTypeId());
    }

    public TypeId getTypeId() {
        DataTypeDescriptor dtd = getTypeServices();
        if (dtd != null) {
            return dtd.getTypeId();
        }
        return null;
    }

    public ValueNode bindExpression(FromList fromList) {
        return this;
    }

    public Object getConstantValueAsObject() {
        return null;
    }

    public boolean isBooleanTrue() {
        return false;
    }

    public ValueNode putAndsOnTop() {
        //构建一个布尔类型的常量节点
        BooleanConstantNode trueNode = new BooleanConstantNode(true, getContextManager());
        AndNode andNode = new AndNode(this, trueNode, getContextManager());
        andNode.postBindFixup();
        return andNode;
    }

    public ValueNode preprocess(int numTables, FromList outerFromList, PredicateList outerPredicateList) {
        return this;
    }

    public ResultColumn getSourceResultColumn() {
        return null;
    }

    public void copyFields(ValueNode oldVN) {
        dataTypeServices = oldVN.getTypeServices();
    }

}
