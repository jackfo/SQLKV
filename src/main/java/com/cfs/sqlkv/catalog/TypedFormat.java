package com.cfs.sqlkv.catalog;

/**
 * @author zhengxiaokang
 * @Description 标识格式id
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 16:14
 */
public interface TypedFormat {
    /**
     * 获取格式id
     * @return 标识符。(一个UUID的16字节数组)。
     * */
    int getTypeFormatId();
}
