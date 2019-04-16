package com.cfs.sqlkv.compile.sql;

import com.cfs.sqlkv.service.loader.ClassInspector;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 19:10
 */
public class GenericParameterValueSet implements ParameterValueSet {

    private final GenericParameter[] parms;
    final ClassInspector  ci;
    private	final boolean hasReturnOutputParam;

    public GenericParameterValueSet(ClassInspector ci, int numParms, boolean hasReturnOutputParam){
        this.ci = ci;
        this.hasReturnOutputParam = hasReturnOutputParam;
        parms = new GenericParameter[numParms];
        for (int i = 0; i < numParms; i++) {
            parms[i] = new GenericParameter(this, (hasReturnOutputParam && i == 0));
        }
    }
}
