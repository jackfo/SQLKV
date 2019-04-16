package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-11 18:37
 */
public class ScrollInsensitiveResultSetNode extends SingleChildResultSetNode {

    ScrollInsensitiveResultSetNode(ResultSetNode childResult, ResultColumnList rcl, Properties tableProperties, ContextManager cm) {
        super(childResult, cm);
        setResultColumns(rcl);
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        assignResultSetNumber();
        acb.addItem(makeResultDescription());
        acb.pushGetResultSetFactoryExpression(mb);
        childResult.generate(acb, mb);
    }
}
