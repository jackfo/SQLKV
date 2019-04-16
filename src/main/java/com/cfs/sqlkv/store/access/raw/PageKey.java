package com.cfs.sqlkv.store.access.raw;

import com.cfs.sqlkv.service.io.CompressedNumber;

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
        this.container = key;
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
     * 从输入流中读取到对应的PageKey
     * */
    public static PageKey read(ObjectInput in) throws IOException {
        ContainerKey c = ContainerKey.read(in);
        long pageNumber = CompressedNumber.readLong(in);
        return new PageKey(c, pageNumber);
    }

    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + container.hashCode();
        hash = 79 * hash + (int) (pageNumber ^ (pageNumber >>> 32));
        return hash;
    }

    public String toString() {
        return "Page(" + pageNumber + "," + container.toString() + ")";
    }

    public boolean equals(Object other) {
        if (other instanceof PageKey) {
            PageKey otherKey = (PageKey) other;
            return (pageNumber == otherKey.pageNumber) && container.equals(otherKey.container);
        }
        return false;
    }

}
