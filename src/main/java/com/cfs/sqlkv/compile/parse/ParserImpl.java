package com.cfs.sqlkv.compile.parse;

import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.compile.Parser;
import com.cfs.sqlkv.compile.Visitable;


import java.io.StringReader;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 18:31
 */
public class ParserImpl {

    public static final int LARGE_TOKEN_SIZE = 128;
    private SQLParser cachedParser;
    private SQLParserTokenManager cachedTokenManager;
    protected String SQLtext;
    private final CompilerContext compilerContext;
    private CharStream charStream;

    public ParserImpl(CompilerContext compilerContext) {
        this.compilerContext = compilerContext;
    }

    /**
     * 获取相应的SQL解析器
     */
    public SQLParser getParse() {
        SQLParserTokenManager sqlParserTokenManager = getTokenManger();
        SQLParser sqlParser = cachedParser;
        if (sqlParser == null) {
            sqlParser = new SQLParser(sqlParserTokenManager);
            sqlParser.setCompilerContext(compilerContext);
        } else {
            sqlParser.ReInit(sqlParserTokenManager);
        }
        return sqlParser;
    }

    /**
     * 获取相应的解析管理器,如果存在则重新利用
     */
    public SQLParserTokenManager getTokenManger() {
        SQLParserTokenManager sqlParserTokenManager = cachedTokenManager;
        if (sqlParserTokenManager == null) {
            sqlParserTokenManager = new SQLParserTokenManager(charStream);
            cachedTokenManager = sqlParserTokenManager;
        } else {
            sqlParserTokenManager.ReInit(charStream);
        }
        return sqlParserTokenManager;
    }


    public Visitable parseStatement(String sql) throws ParseException {
        return parseStatement(sql, null);
    }

    /**
     * @param statementSQLText 解析的SQL文本
     * @param paramDefaults    默认的参数
     */
    public Visitable parseStatement(String statementSQLText, Object[] paramDefaults) throws ParseException {
        return parseStatement(statementSQLText, paramDefaults, true);
    }

    private Visitable parseStatement(String sql, Object[] paramDefaults, boolean isStatement) throws ParseException {
        StringReader sqlReader = new StringReader(sql);
        if (charStream == null) {
            charStream = new UCode_CharStream(sqlReader, 1, 1, LARGE_TOKEN_SIZE);
        } else {
            charStream.ReInit(sqlReader, 1, 1, LARGE_TOKEN_SIZE);
        }
        SQLtext = sql;
        //获取相应的SQL解析器
        SQLParser sqlParser = getParse();
        //根据isStatement来判定是否带条件
        return isStatement ? sqlParser.statement(sql, paramDefaults) : sqlParser.searchCondition(sql);
    }

    /**
     * @return 返回相应的SQL语句
     */
    public String getSQLtext() {
        return SQLtext;
    }


}
