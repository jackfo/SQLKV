package com.cfs.sqlkv.common;

import java.util.Arrays;
import java.util.Collection;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-12 10:58
 */
public class constant {


    /**SQL的一些关键字*/
    public static final Collection SQL_KEYWORDS;


    static {
        SQL_KEYWORDS = Arrays.asList("select", "from", "where", "and", "insert",
                "into", "values", "delete", "drop", "update", "set", "create", "table",
                "int", "double", "varchar", "view", "as", "index", "on",
                "long", "order", "by", "asc", "desc", "sum", "count", "avg",
                "min", "max", "distinct", "group", "add", "sub", "mul", "div",
                "explain", "using", "hash", "btree");
    }

}
