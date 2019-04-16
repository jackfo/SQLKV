package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.transaction.AccessManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 19:10
 */
public interface Database {

    /**
     *根据数据库获取数据字典
     * */
    public DataDictionary getDataDictionary();

    public LanguageConnectionContext setupConnection(ContextManager cm, String user, String drdaID, String dbname);

    //在连接之后加载数据库信息
    public void init(ContextManager contextManager,boolean create);

    public AccessManager getAccessManager();
}
