package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.result.NoPutResultSet;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.service.classfile.VMOpcode;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-31 13:51
 */
public class RowResultSetNode extends FromTable {

    public RowResultSetNode(ResultColumnList resultColumns, Properties tableProperties, ContextManager contextManager) {
        super(null,contextManager);
        setResultColumns(resultColumns);

    }

    public String statementToString() {
        return "VALUES";
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        assignResultSetNumber();
        acb.pushGetResultSetFactoryExpression(mb);
        acb.pushThisAsActivation(mb);
        //获取结果列来进行生成,将列添加到行结果集里面
        getResultColumns().generate(acb, mb);
        mb.push(getResultSetNumber());
        mb.callMethod(VMOpcode.INVOKEINTERFACE, null, "getRowResultSet", NoPutResultSet.class.getName(), 3);
    }
}
