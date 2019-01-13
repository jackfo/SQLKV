package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.node.QueryTreeNodeVector;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 23:33
 */
public class ResultColumnList extends QueryTreeNodeVector<ResultColumn> {

    public ResultColumnList(ContextManager contextManager){
        this(null,contextManager);
    }

    public ResultColumnList(Class<ResultColumn> eltClass, ContextManager contextManager) {
        super(eltClass, contextManager);
    }

    /**
     * 将表中所有的结果列添加到结果集
     * @param resultColumn
     * @return 无返回值,主要是设置resultColumn实例相关句柄属性
     *
     * */
    public void addResultColumn(ResultColumn resultColumn) {
        resultColumn.setVirtualColumnId(size() + 1);
        addElement(resultColumn);
    }





}
