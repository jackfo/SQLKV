package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.result.ResultColumnList;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 18:49
 */
public class SelectNode extends ResultSetNode{

    FromList  fromList;
    FromTable targetTable;
    public SelectNode(ContextManager contextManager) {
        super(contextManager);
    }

    public SelectNode(ResultColumnList selectList, FromList fromList,ContextManager contextManager){
        super(contextManager);
        this.fromList = fromList;
    }

}
