package com.cfs.sqlkv.store.access.raw.log;

import com.cfs.sqlkv.service.io.Formatable;

/**
 * @author zhengxiaokang
 * @Description 描述日志的位置
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-14 20:14
 */
public interface LogInstant extends Formatable {

    public static final long INVALID_LOG_INSTANT = 0;
}
