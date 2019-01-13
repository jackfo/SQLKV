package com.cfs.sqlkv.store.access.raw;

import com.cfs.sqlkv.io.CompressedNumber;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-10 13:17
 */
public final class ContainerKey{

    /**段落标识*/
    private final long	segmentId;

    private final long	containerId;

    public ContainerKey(long segmentId, long containerId) {
        this.segmentId = segmentId;
        this.containerId = containerId;
    }

    public long getSegmentId(){
        return segmentId;
    }

    public long getContainerId(){
        return containerId;
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        CompressedNumber.writeLong(out, segmentId);
        CompressedNumber.writeLong(out, containerId);
    }

    public static ContainerKey read(ObjectInput in) throws IOException {
        long sid = CompressedNumber.readLong(in);
        long cid = CompressedNumber.readLong(in);
        return new ContainerKey(sid, cid);
    }
}
