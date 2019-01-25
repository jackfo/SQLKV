package com.cfs.sqlkv.io;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-19 14:03
 */
public class ArrayOutputStream {

    private byte[] pageData;

    private int		start;
    private int		end;
    private int		position;

    public ArrayOutputStream() {
        super();
    }

    public ArrayOutputStream(byte[] data) {
        super();
        setData(data);
    }

    public void setData(byte[] data) {
        pageData = data;
        start = 0;
        if (data != null){
            end = data.length;
        } else{
            end = 0;
        }
        position = 0;

    }

    public void write(int b) throws IOException {
        if (position >= end){
            throw new EOFException();
        }
        pageData[position++] = (byte) b;

    }

    public void write(byte b[], int off, int len) throws IOException {
        if ((position + len) > end){
            throw new EOFException();
        }
        System.arraycopy(b, off, pageData, position, len);
        position += len;
    }

    public int getPosition() {
        return position;
    }

    /**
     * 设置当前数据对应的位置
     */
    public void setPosition(int newPosition) throws IOException {
        if ((newPosition < start) || (newPosition > end)){
            throw new EOFException();
        }
        position = newPosition;
    }

    public void setLimit(int length) throws IOException {
        if (length < 0) {
            throw new EOFException();
        }

        if ((position + length) > end) {
            throw new EOFException();
        }

        start = position;
        end = position + length;

        return;
    }

    public int clearLimit() {

        int unwritten = end - position;

        end = pageData.length;

        return unwritten;
    }
}
