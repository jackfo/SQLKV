package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.sql.types.*;

import java.math.BigDecimal;
import java.sql.Types;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-31 14:43
 */
public class NumericConstantNode extends ConstantNode {

    final static int K_TINYINT = 0;
    final static int K_SMALLINT = 1;
    final static int K_INT = 2;
    final static int K_BIGINT = 3;
    final static int K_DECIMAL = 4;
    final static int K_DOUBLE = 5;
    final static int K_REAL = 6;

    final int kind;

    public NumericConstantNode(TypeId t, ContextManager cm) {
        super(cm);
        setType(t, getPrecision(t, null),
                getScale(t, null),
                true,
                getMaxWidth(t, null));
        kind = getKind(t);
    }

    public NumericConstantNode(TypeId typeId, Number value, ContextManager cm) {
        super(cm);
        kind = getKind(typeId);
        setType(typeId,
                getPrecision(typeId, value),
                getScale(typeId, value),
                false,
                getMaxWidth(typeId, value));
        setValue(typeId, value);
    }

    private int getScale(TypeId t, Object val) {
        switch (t.getJDBCTypeId()) {
            case Types.INTEGER:
                return TypeId.INT_SCALE;
            default:
                return 0;
        }
    }

    private int getMaxWidth(TypeId t, Object val) {
        switch (t.getJDBCTypeId()) {
            case Types.INTEGER:
                return val != null ? TypeId.INT_MAXWIDTH : 0;
            default:
                return 0;
        }
    }

    private int getKind(TypeId t) {
        switch (t.getJDBCTypeId()) {
            case Types.INTEGER:
                return K_INT;
            default:
                return -1;
        }
    }

    private void setValue(TypeId t, Number value) {
        switch (t.getJDBCTypeId()) {
            case Types.INTEGER:
                SQLInteger sqlInteger = new SQLInteger((Integer) value);
                setValue(sqlInteger);
                break;
            default:
        }
    }


    private int getPrecision(TypeId t, Number val) {
        switch (t.getJDBCTypeId()) {
            case Types.INTEGER:
                return TypeId.INT_PRECISION;
            default:
                return 0;
        }
    }

    @Override
    public void generateConstant(ActivationClassBuilder acb, MethodBuilder mb) {
        switch (kind){
            case K_INT:
                mb.push(value.getInt());
                break;
            default:
        }
    }



    @Override
    public String toString() {
        return "NumericConstantNode{" +
                "kind=" + kind +
                ", value=" + value +
                '}';
    }
}
