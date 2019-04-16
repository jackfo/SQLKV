package com.cfs.sqlkv.factory;

import com.cfs.sqlkv.common.UUID;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 18:11
 */
public class UUIDFactory {


    public UUID recreateUUID(String idString) {
        return new UUID(idString);
    }

    private long currentValue;
    private long timemillis;

    private static final long MODULUS = (1L << 32);
    private static final long MULTIPLIER = ((1L << 14) + 1);
    private static final long STEP = ((1L << 27) + 1);
    private static final long INITIAL_VALUE = (2551218188L);

    public synchronized UUID createUUID() {
        long cv = currentValue = ((MULTIPLIER * currentValue) + STEP) % MODULUS;
        if (cv == INITIAL_VALUE) {
            bumpMajor();
        }
        int sequence = (int) cv;

        return new UUID(majorId, timemillis, sequence);
    }

    private long majorId;

    private void bumpMajor() {

        // 48 bits only
        majorId = (majorId + 1L) & 0x0000ffffffffffffL;
        if (majorId == 0L)
            resetCounters();

    }

    private void resetCounters() {
        timemillis = System.currentTimeMillis();
        currentValue = INITIAL_VALUE;
    }
}
