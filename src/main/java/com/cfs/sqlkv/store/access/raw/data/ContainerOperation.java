package com.cfs.sqlkv.store.access.raw.data;



/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 19:02
 */
public class ContainerOperation {

    public static final byte CREATE = (byte)1;
    public static final byte DROP = (byte)2;
    public static final byte REMOVE = (byte)4;

    public byte operation;

    public ContainerOperation(BaseContainerHandle hdl, byte operation)   {
        //super(hdl);
        this.operation = operation;
    }
}
