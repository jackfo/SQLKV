package com.cfs.sqlkv.compile.sql;

import java.sql.ParameterMetaData;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 19:15
 */
public class GenericParameter {

    private final GenericParameterValueSet pvs;

    public GenericParameter(GenericParameterValueSet pvs,boolean isReturnOutputParameter){
        this.pvs = pvs;
    }
}
