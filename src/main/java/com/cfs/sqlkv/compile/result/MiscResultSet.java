package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.context.StatementContext;
import com.cfs.sqlkv.engine.execute.ConstantAction;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 20:11
 */
public class MiscResultSet extends NoRowsResultSetImpl{

    public MiscResultSet(Activation activation) {
        super(activation);
    }

    /**
     * 打开结果集
     * */
    @Override
    public void open()   {
        //获取当前获取激活器的固定行为
        ConstantAction constantAction = activation.getConstantAction();
        //执行激活器对应的行为
        constantAction.executeConstantAction(activation);
    }



    private boolean isOpen;

    @Override
    public void setup()   {
        isOpen = true;
        StatementContext sc = lcc.getStatementContext();
    }
}
