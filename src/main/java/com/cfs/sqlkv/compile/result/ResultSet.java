package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.exception.StandardException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 19:43
 */
public interface ResultSet {

    void open() throws StandardException;
}
