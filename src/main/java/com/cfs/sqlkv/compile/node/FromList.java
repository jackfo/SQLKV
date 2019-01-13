package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.name.TableName;
import com.cfs.sqlkv.exception.StandardException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-03 18:48
 */
public class FromList extends QueryTreeNodeVector<ResultSetNode> {

    public FromList(ContextManager contextManager){
       super(null,contextManager);
    }

    public final void addFromTable(FromTable fromTable) throws StandardException{
        TableName leftTable;
        TableName rightTable;
    }
}
