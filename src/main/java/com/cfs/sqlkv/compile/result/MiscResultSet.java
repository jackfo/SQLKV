package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.context.StatementContext;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:11
 */
public class MiscResultSet extends NoRowsResultSetImpln{

    public MiscResultSet(Activation activation) {
        super(activation);
    }

    @Override
    public void open() throws StandardException {
        activation.getConstantAction().executeConstantAction(activation);
    }

    private boolean isOpen;

    void setup() throws StandardException {
        isOpen = true;
        StatementContext sc = lcc.getStatementContext();
    }
}
