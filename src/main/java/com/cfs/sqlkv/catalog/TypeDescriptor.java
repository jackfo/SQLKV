package com.cfs.sqlkv.catalog;

/**
 * @author zhengxiaokang
 * @Description  在系统目录中TypeDescriptor代表一个类型
 *               举例:表中的columns,routines中的参数
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 18:04
 */
public interface TypeDescriptor {

    /**返回字节的最大长度*/
    public	int getMaximumWidthInBytes();
    public	boolean isNullable();
    public	int getScale();
    public	int getPrecision();
}
