package com.cfs.sqlkv.io;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 14:12
 */
public interface Storable extends Formatable{

    public boolean isNull();

    public void restoreToNull();
}
