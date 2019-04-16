package com.cfs.sqlkv.row;

import com.cfs.sqlkv.sql.activation.Activation;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 09:05
 */
public class IndexRow extends ValueRow implements ExecIndexRow {

    private boolean[]	orderedNulls;
    public IndexRow(int ncols) {
        super(ncols);
        orderedNulls = new boolean[ncols];
    }


}
