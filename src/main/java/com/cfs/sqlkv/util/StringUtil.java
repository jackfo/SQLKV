package com.cfs.sqlkv.util;

import java.util.Locale;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 15:48
 */
public class StringUtil {
    public static String SQLToUpperCase(String s) {
        return s.toUpperCase(Locale.ENGLISH);
    }

    public  static String quoteString(String source, char quote) {
        StringBuffer quoted = new StringBuffer(source.length() + 2);
        quoted.append(quote);
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == quote) quoted.append(quote);
            quoted.append(c);
        }
        quoted.append(quote);
        return quoted.toString();
    }

}
