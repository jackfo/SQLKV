package com.cfs.sqlkv.compile.sql;

import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.compile.parse.ParseException;
import com.cfs.sqlkv.context.LanguageConnectionContext;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 20:28
 */
public interface Statement {

    public PreparedStatement prepare(LanguageConnectionContext lcc) throws ParseException  , ParseException;

    /**
     * 生成一个执行计划不进行执行
     * */
    public PreparedStatement prepare(LanguageConnectionContext lcc, boolean allowInternalSyntax) throws ParseException  , ParseException;

    /**
     * 生成执行计划给出一组执行参数
     * */
    public PreparedStatement prepareStorable(LanguageConnectionContext lcc, PreparedStatement ps, Object[]			paramDefaults, SchemaDescriptor spsSchema, boolean	internalSQL)  ;

    /**
     *	返回这个statement的SQL字符串
     *	@return 当前statement的SQL字符串
     */
    String getSource();
}
