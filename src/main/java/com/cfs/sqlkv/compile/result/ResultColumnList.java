package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.ColumnReference;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.compile.node.*;
import com.cfs.sqlkv.compile.predicate.PredicateList;
import com.cfs.sqlkv.engine.execute.ExecRowBuilder;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.Row;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.type.DataValueDescriptor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 23:33
 */
public class ResultColumnList extends QueryTreeNodeVector<ResultColumn> {
    public long conglomerateId;
    private String _derivedColumnName;

    public ResultColumnList(ContextManager contextManager) {
        this(null, contextManager);
    }

    public ResultColumnList(Class<ResultColumn> eltClass, ContextManager contextManager) {
        super(eltClass, contextManager);
    }

    /**
     * 将表中所有的结果列添加到结果集
     *
     * @param resultColumn
     * @return 无返回值, 主要是设置resultColumn实例相关句柄属性
     */
    public void addResultColumn(ResultColumn resultColumn) {
        resultColumn.setVirtualColumnId(size() + 1);
        addElement(resultColumn);
    }

    public String[] getColumnNames() {
        String strings[] = new String[size()];
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn resultColumn = elementAt(index);
            strings[index] = resultColumn.getName();
        }
        return strings;
    }

    public void bindResultColumnsToExpressions() {
        for (ResultColumn rc : this) {
            rc.bindResultColumnToExpression();
        }
    }

    public ResultColumnDescriptor[] makeResultDescriptors() {
        ResultColumnDescriptor colDescs[] = new ResultColumnDescriptor[size()];
        int size = size();
        for (int index = 0; index < size; index++) {
            colDescs[index] = new GenericColumnDescriptor(((ResultColumnDescriptor) elementAt(index)));
        }
        return colDescs;
    }


    public void preprocess(int numTables, FromList outerFromList, PredicateList outerPredicateList) {
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn resultColumn = elementAt(index);
            resultColumn = resultColumn.preprocess(numTables, outerFromList, outerPredicateList);
            setElementAt(resultColumn, index);
        }
    }

    protected boolean indexRow;

    public ExecRowBuilder buildRowTemplate(FormatableBitSet referencedCols, boolean skipPropagatedCols) {
        int columns = size();
        int colNum = 0;
        ExecRowBuilder builder = new ExecRowBuilder(columns, indexRow);
        for (ResultColumn rc : this) {
            builder.setColumn(colNum + 1, rc.getType());
            colNum++;
        }
        return builder;
    }

    public void setResultSetNumber(int resultSetNumber) {
        for (ResultColumn rc : this) {
            rc.setResultSetNumber(resultSetNumber);
        }
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        generateCore(acb, mb, false);
    }

    public void generateCore(ActivationClassBuilder acb, MethodBuilder mb, boolean genNulls) {
        MethodBuilder userExprFun = acb.newUserExprFun();
        generateEvaluatedRow(acb, userExprFun, genNulls, false);
        acb.pushMethodReference(mb, userExprFun);
    }

    public void generateEvaluatedRow(ActivationClassBuilder acb,
                                     MethodBuilder userExprFun, boolean genNulls, boolean forMatchingClause) {
        /**定义字段*/
        LocalField field = acb.newFieldDeclaration(Modifier.PRIVATE, ExecRow.class.getName());
        genCreateRow(acb, field, "getValueRow", ExecRow.class.getName(), size());
        ResultColumn rc;
        int size = size();
        MethodBuilder cb = acb.getConstructor();
        for (int index = 0; index < size; index++) {
            rc = elementAt(index);
            if (!genNulls) {
                ValueNode sourceExpr = rc.getExpression();
                if (sourceExpr instanceof VirtualColumnNode && !(((VirtualColumnNode) sourceExpr).getCorrelated())) {
                    continue;
                }

                if (!forMatchingClause) {
                    if (sourceExpr instanceof ColumnReference && !(((ColumnReference) sourceExpr).getCorrelated())) {
                        continue;
                    }
                }
            }
            if ((!genNulls) && (rc.getExpression() instanceof ConstantNode) &&
                    !((ConstantNode) rc.getExpression()).isNull() &&
                    !cb.statementNumHitLimit(1)) {
                cb.getField(field);
                cb.push(index + 1);
                rc.generateExpression(acb, cb);
                cb.cast(DataValueDescriptor.class.getName()); // second arg
                cb.callMethod(VMOpcode.INVOKEINTERFACE, Row.class.getName(), "setColumn", "void", 2);
                continue;
            }
            userExprFun.getField(field);
            userExprFun.push(index + 1);
            if (genNulls || ((rc.getExpression() instanceof ConstantNode) && ((ConstantNode) rc.getExpression()).isNull())) {
                userExprFun.getField(field);
                userExprFun.push(index + 1);
                userExprFun.callMethod(VMOpcode.INVOKEINTERFACE, Row.class.getName(), "getColumn", DataValueDescriptor.class.getName(), 1);

                acb.generateNullWithExpress(userExprFun, rc.getTypeCompiler(), rc.getTypeServices().getCollationType());
            } else {
                rc.generateExpression(acb, userExprFun);
            }
            userExprFun.cast(DataValueDescriptor.class.getName());
            userExprFun.callMethod(VMOpcode.INVOKEINTERFACE, Row.class.getName(), "setColumn", "void", 2);
        }
        userExprFun.getField(field);
        userExprFun.methodReturn();
        userExprFun.complete();
    }

    private void genCreateRow(ActivationClassBuilder acb,
                              LocalField field,
                              String rowAllocatorMethod,
                              String rowAllocatorType,
                              int numCols) {

        //	 fieldX = getExecutionFactory().getValueRow(# cols);
        MethodBuilder cb = acb.getConstructor();
        acb.pushGetExecutionFactoryExpression(cb); // instance
        cb.push(numCols);
        cb.callMethod(VMOpcode.INVOKEINTERFACE, null,
                rowAllocatorMethod, rowAllocatorType, 1);
        cb.setField(field);
        //忽略返回值
        cb.statementNumHitLimit(1);
    }

    public void bindExpressions(FromList fromList) {
        expandAllsAndNameColumns(fromList);
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn vn = elementAt(index);
            vn = vn.bindExpression(fromList);
            setElementAt(vn, index);
        }
    }

    public void expandAllsAndNameColumns(FromList fromList) {
        ResultColumnList allExpansion;
        TableName fullTableName = null;
        for (int index = 0; index < size(); index++) {
            ResultColumn rc = elementAt(index);
            if (rc instanceof AllResultColumn) {
                allExpansion = fromList.expandAll(fullTableName);
                removeElementAt(index);
                for (int inner = 0; inner < allExpansion.size(); inner++) {
                    insertElementAt(allExpansion.elementAt(inner), index + inner);
                }
            }
        }
    }

    public ResultColumn getResultColumn(String columnName) {
        return getResultColumn(columnName, true);
    }


    public ResultColumn getResultColumn(int position) {
        if (position <= size()) {
            ResultColumn rc = elementAt(position - 1);
            if (rc.getColumnPosition() == position) {
                return rc;
            }
        }
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn rc = elementAt(index);
            if (rc.getColumnPosition() == position) {
                return rc;
            }
        }
        return null;
    }

    public ResultColumn getResultColumn(String columnName, boolean markIfReferenced) {
        for (ResultColumn resultColumn : this) {
            if (columnName.equals(resultColumn.getName())) {
                if (markIfReferenced) {
                    resultColumn.setReferenced();
                }
                return resultColumn;
            }
        }
        return null;
    }

    public void markUpdated() {
        for (ResultColumn rc : this) {
            rc.markUpdated();
        }
    }

    public void markUpdated(ResultColumnList updateColumns) {
        for (ResultColumn updateColumn : updateColumns) {
            ResultColumn resultColumn = getResultColumn(updateColumn.getName());
            if (resultColumn != null) {
                resultColumn.markUpdated();
            }
        }
    }

    public int[] sortMe() {
        ResultColumn[] sortedResultColumns = getSortedByPosition();
        int[] sortedColumnIds = new int[sortedResultColumns.length];
        for (int ix = 0; ix < sortedResultColumns.length; ix++) {
            sortedColumnIds[ix] = sortedResultColumns[ix].getColumnPosition();
        }
        return sortedColumnIds;
    }

    public ResultColumn[] getSortedByPosition() {
        int size = size();
        ResultColumn[] result;
        result = new ResultColumn[size];
        for (int index = 0; index < size; index++) {
            result[index] = elementAt(index);
        }
        java.util.Arrays.sort(result);
        return result;
    }

    public boolean nopProjection(ResultColumnList childRCL) {
        if (this.size() != childRCL.size()) {
            return false;
        }
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn thisColumn = elementAt(index);
            ResultColumn referencedColumn;
            if (thisColumn.getExpression() instanceof VirtualColumnNode) {
                referencedColumn = ((VirtualColumnNode) (thisColumn.getExpression())).getSourceColumn();
            } else if (thisColumn.getExpression() instanceof ColumnReference) {
                referencedColumn = ((ColumnReference) (thisColumn.getExpression())).getSource();
            } else {
                return false;
            }
            ResultColumn childColumn = childRCL.elementAt(index);
            if (referencedColumn != childColumn) {
                return false;
            }
        }
        return true;
    }

    public ResultColumnList compactColumns(boolean positionedUpdate, boolean always) {
        int index;
        int colsAdded = 0;
        if (positionedUpdate) {
            return this;
        }

        ResultColumnList newCols = new ResultColumnList(getContextManager());

        int size = size();
        for (index = 0; index < size; index++) {
            ResultColumn oldCol = elementAt(index);
            if (oldCol.isReferenced()) {
                newCols.addResultColumn(oldCol);
                colsAdded++;
            }
        }
        if (colsAdded != index || always) {
            return newCols;
        } else {
            return this;
        }
    }


    public boolean allExpressionsAreColumns(ResultSetNode sourceRS) {
        for (ResultColumn rc : this) {
            ValueNode expr = rc.getExpression();
            if (!(expr instanceof VirtualColumnNode) &&
                    !(expr instanceof ColumnReference)) {
                return false;
            }
            if (expr instanceof VirtualColumnNode) {
                VirtualColumnNode vcn = (VirtualColumnNode) expr;
                if (vcn.getSourceResultSet() != sourceRS) {
                    vcn.setCorrelated();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断是否可以进行修改
     */
    public boolean forUpdate;

    public void setIndexRow(long cid, boolean forUpdate) {
        indexRow = true;
        conglomerateId = cid;
        this.forUpdate = forUpdate;
    }

    public void genVirtualColumnNodes(ResultSetNode sourceResultSet, ResultColumnList sourceResultColumnList) {
        genVirtualColumnNodes(sourceResultSet, sourceResultColumnList, true);
    }

    /**
     * 取代结果列的标识为虚拟节点
     */
    public void genVirtualColumnNodes(ResultSetNode sourceResultSet, ResultColumnList sourceResultColumnList, boolean markReferenced) {
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn resultColumn = elementAt(index);
            VirtualColumnNode virtualColumnNode = new VirtualColumnNode(sourceResultSet, sourceResultColumnList.elementAt(index), index + 1, getContextManager());
            resultColumn.setExpression(virtualColumnNode);
            if (markReferenced) {
                resultColumn.setReferenced();
            }
        }
    }

    public void doProjection() {
        int numDeleted = 0;
        ResultColumnList deletedRCL = new ResultColumnList(getContextManager());
        for (ResultColumn resultColumn : this) {
            if ((!resultColumn.isReferenced()) && (resultColumn.getExpression() instanceof VirtualColumnNode) &&
                    !(((VirtualColumnNode) resultColumn.getExpression()).getSourceColumn().isReferenced())) {
                deletedRCL.addElement(resultColumn);
                numDeleted++;
            } else {
                if (numDeleted >= 1) {
                    resultColumn.adjustVirtualColumnId(-numDeleted);
                }
                resultColumn.setReferenced();
            }
        }
        for (int index = 0; index < deletedRCL.size(); index++) {
            removeElement(deletedRCL.elementAt(index));
        }
    }

    public ColumnMapping mapSourceColumns() {
        int[] mapArray = new int[size()];
        boolean[] cloneMap = new boolean[size()];
        ResultColumn resultColumn;
        Map<Integer, Integer> seenMap = new HashMap();
        int size = size();
        for (int index = 0; index < size; index++) {
            resultColumn = elementAt(index);
            if (resultColumn.getExpression() instanceof VirtualColumnNode) {
                VirtualColumnNode vcn = (VirtualColumnNode) resultColumn.getExpression();
                if (vcn.getCorrelated()) {
                    mapArray[index] = -1;
                } else {
                    ResultColumn rc = vcn.getSourceColumn();
                    updateArrays(mapArray, cloneMap, seenMap, rc, index);
                }
            } else if (resultColumn.getExpression() instanceof ColumnReference) {
                ColumnReference cr = (ColumnReference) resultColumn.getExpression();
                if (cr.getCorrelated()) {
                    mapArray[index] = -1;
                } else {
                    ResultColumn rc = cr.getSource();
                    updateArrays(mapArray, cloneMap, seenMap, rc, index);
                }
            } else {
                mapArray[index] = -1;
            }
        }

        ColumnMapping result = new ColumnMapping(mapArray, cloneMap);
        return result;
    }

    private static void updateArrays(int[] mapArray, boolean[] cloneMap, Map<Integer, Integer> seenMap, ResultColumn rc, int index) {
        int vcId = rc.getVirtualColumnId();
        mapArray[index] = vcId;
    }

    public static class ColumnMapping {

        public final int[] mapArray;
        public final boolean[] cloneMap;

        public ColumnMapping(int[] mapArray, boolean[] cloneMap) {
            this.mapArray = mapArray;
            this.cloneMap = cloneMap;
        }
    }

    public void appendResultColumns(ResultColumnList resultColumns) {
        int oldSize = size();
        int newID = oldSize + 1;
        for (ResultColumn rc : resultColumns) {
            /* ResultColumns are 1-based */
            rc.setVirtualColumnId(newID);
            newID++;
        }
        nondestructiveAppend(resultColumns);
    }

    public void bindResultColumnsByPosition(TableDescriptor targetTableDescriptor) {
        int size = size();
        for (int index = 0; index < size; index++) {
            elementAt(index).bindResultColumn(targetTableDescriptor, index + 1);
        }
    }

    public FormatableBitSet bindResultColumnsByName(TableDescriptor targetTableDescriptor) {
        int size = size();
        FormatableBitSet columnBitSet = new FormatableBitSet(targetTableDescriptor.getNumberOfColumns());
        for (int index = 0; index < size; index++) {
            ResultColumn rc = elementAt(index);
            rc.bindResultColumn(targetTableDescriptor, index + 1);
            int colIdx = rc.getColumnPosition() - 1;
            columnBitSet.set(colIdx);
        }
        return columnBitSet;
    }

    public ResultColumnList copyListAndObjects() {
        ResultColumnList newList = new ResultColumnList(getContextManager());
        for (ResultColumn origResultColumn : this) {
            newList.addResultColumn(origResultColumn.cloneMe());
        }
        return newList;
    }

    public void bindUntypedNullsToResultColumns(ResultColumnList bindingRCL) {
        if (bindingRCL == null) {
            throw new RuntimeException("LANG_NULL_IN_VALUES_CLAUSE");
        }
        int size = size();
        for (int index = 0; index < size; index++) {
            ResultColumn bindingRC = bindingRCL.elementAt(index);
            ResultColumn thisRC = elementAt(index);
            thisRC.typeUntypedNullExpression(bindingRC);
        }
    }


    public void copyResultColumnNames(ResultColumnList nameList) {
        int size = size() - numGeneratedColumns();
        for (int index = 0; index < size; index++) {
            ResultColumn thisResultColumn = elementAt(index);
            ResultColumn nameListResultColumn = nameList.elementAt(index);
            thisResultColumn.setName(nameListResultColumn.getName());
            thisResultColumn.setNameGenerated(nameListResultColumn.isNameGenerated());
        }
    }


    private int numGeneratedColumns() {
        int numGenerated = 0;
        int sz = size();
        for (int i = sz - 1; i >= 0; i--) {
            ResultColumn rc = elementAt(i);
            if (rc.isGenerated()) {
                numGenerated++;
            }
        }
        return numGenerated;
    }

}
