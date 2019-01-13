package com.cfs.sqlkv.compile.table;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.FromTable;
import com.cfs.sqlkv.compile.node.MethodCallNode;
import com.cfs.sqlkv.compile.result.ResultColumnList;
import com.cfs.sqlkv.compile.vti.VTIEnvironment;
import com.cfs.sqlkv.exception.StandardException;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 19:14
 */

public class FromVTI extends FromTable implements VTIEnvironment{

    public FromVTI(MethodCallNode invocation, String correlationName, ResultColumnList derivedRCL,
                   Properties tableProperties, ContextManager cm) throws StandardException {

    }
}
