package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.common.Limits;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 20:30
 */
public interface TypeCompiler {

    public static final int DEFAULT_DECIMAL_PRECISION	= Limits.DB2_DEFAULT_DECIMAL_PRECISION;
    public static final int DEFAULT_DECIMAL_SCALE 		= Limits.DB2_DEFAULT_DECIMAL_SCALE;
    public static final int MAX_DECIMAL_PRECISION_SCALE = Limits.DB2_MAX_DECIMAL_PRECISION_SCALE;

    public String interfaceName();

    public void generateDataValue(MethodBuilder mb, int collationType, LocalField field);

    public void generateNull(MethodBuilder mb, int collationType);
}
