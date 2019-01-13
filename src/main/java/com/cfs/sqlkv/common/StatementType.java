package com.cfs.sqlkv.common;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 15:38
 */
public interface StatementType {

    public static final int UNKNOWN	= 0;
    public static final int INSERT	= 1;
    public static final int BULK_INSERT_REPLACE = 2;
    public static final int UPDATE	= 3;
    public static final int DELETE	= 4;
    public static final int ENABLED = 5;
    public static final int DISABLED = 6;

    public static final int DROP_CASCADE = 0;
    public static final int DROP_RESTRICT = 1;
    public static final int DROP_DEFAULT = 2;

    public static final int RENAME_TABLE = 1;
    public static final int RENAME_COLUMN = 2;
    public static final int RENAME_INDEX = 3;

    public static final int RA_CASCADE = 0;
    public static final int RA_RESTRICT = 1;
    public static final int RA_NOACTION = 2;
    public static final int RA_SETNULL = 3;
    public static final int RA_SETDEFAULT = 4;

    public static final int SET_SCHEMA_USER = 1;
    public static final int SET_SCHEMA_DYNAMIC = 2;

    public static final int SET_ROLE_DYNAMIC = 1;
}
