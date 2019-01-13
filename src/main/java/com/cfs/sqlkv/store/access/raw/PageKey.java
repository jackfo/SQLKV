package com.cfs.sqlkv.store.access.raw;

import com.cfs.sqlkv.io.CompressedNumber;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-13 10:26
 */
public class PageKey {

    private final ContainerKey	container;
    private final long	pageNumber;

    public PageKey(ContainerKey key, long pageNumber) {
        container = key;
        this.pageNumber = pageNumber;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public ContainerKey getContainerId() {
        return container;
    }

    /**
     * PageKey有ContainerKey和pageNumber两部分组成
     * 所以先写入ContainerKey 再写入pageNumber
     * */
    public void writeExternal(ObjectOutput out) throws IOException {
        container.writeExternal(out);
        CompressedNumber.writeLong(out, pageNumber);
    }

    /**
     *
     * */
    public static PageKey read(ObjectInput in) throws IOException {
        ContainerKey c = ContainerKey.read(in);
        long pn = CompressedNumber.readLong(in);
        return new PageKey(c, pn);
    }
}
