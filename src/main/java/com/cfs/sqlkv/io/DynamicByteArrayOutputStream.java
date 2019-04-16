package com.cfs.sqlkv.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-28 21:12
 */
public class DynamicByteArrayOutputStream extends OutputStream{

    private static int INITIAL_SIZE = 4096;

    private byte[] buf;
    private int	   position;
    private int	   used;
    private int	   beginPosition;

    public DynamicByteArrayOutputStream() {
        this(INITIAL_SIZE);
    }
    public DynamicByteArrayOutputStream(int size) {
        super();
        buf = new byte[size];
    }
    public DynamicByteArrayOutputStream(byte[] data) {
        super();
        buf = data;
    }

    public DynamicByteArrayOutputStream(DynamicByteArrayOutputStream toBeCloned) {
        byte[] cbuf = toBeCloned.getByteArray();
        buf = new byte[cbuf.length];
        write(cbuf, 0, cbuf.length);
        position = toBeCloned.getPosition();
        used = toBeCloned.getUsed();
        beginPosition = toBeCloned.getBeginPosition();
    }

    @Override
    public void write(int b) {
        if (position >= buf.length){
            expandBuffer(INITIAL_SIZE);
        }
        buf[position++] = (byte) b;
        if (position > used){
            used = position;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if ((position+len) > buf.length){
            expandBuffer(len);
        }

        System.arraycopy(b, off, buf, position, len);
        position += len;
        if (position > used){
            used = position;
        }
    }

    public void writeCompleteStream(InputStream dataIn, int len) throws IOException {
        if ((position+len) > buf.length){
            expandBuffer(len);
        }
        InputStreamUtil.readFully(dataIn, buf, position, len);
        position += len;
        if (position > used){
            used = position;
        }
    }

    @Override
    public void close() {
        buf = null;
        reset();
    }

    public void reset() {
        position = 0;
        beginPosition = 0;
        used = 0;
    }
    public byte[] getByteArray()
    {
        return buf;
    }
    public int getUsed()
    {
        return used;
    }
    public int getPosition()
    {
        return position;
    }
    public int getBeginPosition()
    {
        return beginPosition;
    }
    public void setPosition(int newPosition) {
        if (newPosition > position) {
            if (newPosition > buf.length){
                expandBuffer(newPosition - buf.length);
            }
        }
        position = newPosition;

        if (position > used){
            used = position;
        }
        return ;
    }
    public void setBeginPosition(int newBeginPosition) {
        if (newBeginPosition > buf.length){
            return;
        }
        beginPosition = newBeginPosition;
    }
    public void discardLeft(int amountToShrinkBy) {

        System.arraycopy(buf, amountToShrinkBy, buf, 0,
                used - amountToShrinkBy);

        position -= amountToShrinkBy;
        used -= amountToShrinkBy;
    }

    private void expandBuffer(int minExtension) {
        if (buf.length < (128 * 1024)) {
            if (minExtension < INITIAL_SIZE){
                minExtension = INITIAL_SIZE;
            }
        } else if (buf.length < (1024 * 1024)) {
            if (minExtension < (128 * 1024)){
                minExtension = (128 * 1024);
            }
        } else {
            if (minExtension < (1024 * 1024)){
                minExtension = 1024 * 1024;
            }
        }
        int newsize = buf.length + minExtension;
        byte[] newbuf = new byte[newsize];
        System.arraycopy(buf, 0, newbuf, 0, buf.length);
        buf = newbuf;
    }

}
