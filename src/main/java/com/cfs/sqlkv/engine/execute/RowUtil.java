package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.row.ExecRow;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:11
 */
public class RowUtil {

    public static ExecRow getEmptyValueRow(int columnCount, LanguageConnectionContext lcc) {
        return lcc.getLanguageConnectionFactory().getExecutionFactory().getValueRow(columnCount);
    }
}
