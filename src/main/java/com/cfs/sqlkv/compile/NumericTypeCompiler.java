package com.cfs.sqlkv.compile;

import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.NumberDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 13:29
 */
public class NumericTypeCompiler extends BaseTypeCompiler {

    public String interfaceName()
    {
        return NumberDataValue.class.getName();
    }

    @Override
    public String nullMethodName() {
        int formatId = getStoredFormatIdFromTypeId();
        switch (formatId) {
            case StoredFormatIds.INT_TYPE_ID:
                return "getNullInteger";
            default:
                throw new RuntimeException("\"unexpected formatId in nullMethodName() - \" + formatId)");
        }
    }


}
