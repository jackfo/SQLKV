package com.cfs.sqlkv.io.storage;

import com.cfs.sqlkv.service.io.Formatable;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 14:12
 */
public interface Storable extends Formatable {

    public boolean isNull();

    public void restoreToNull();
}
