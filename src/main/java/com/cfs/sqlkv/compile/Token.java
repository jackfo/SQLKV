package com.cfs.sqlkv.compile;

/**
 * @author zhengxiaokang
 * @Description 描述输入的Token流
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 17:35
 */
public class Token {


    /**
     * 描述共有多少种Token
     *
     * */
    public int kind;

    public int beginLine, beginColumn, endLine, endColumn;

    public Token next;

    public Token specialToken;

    public static Token newToken(){
        return new Token();
    }


}
