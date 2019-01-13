package com.cfs.sqlkv.util;

import com.cfs.sqlkv.common.SQLState;
import com.cfs.sqlkv.exception.StandardException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 15:41
 */
public abstract class IdUtil{

    /**
     * 检查标识符长度
     * */
    public static void checkIdentifierLengthLimit(String identifier, int identifier_length_limit) throws StandardException {
        if (identifier.length() > identifier_length_limit){
            throw new RuntimeException(String.format("表示符长度超过限制 identifier %s identifier_length_limit d%",identifier,identifier_length_limit));
        }
    }

}
