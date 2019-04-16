package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.service.compiler.MethodBuilder;

/**
 * @author zhengxiaokang
 * @Description CursorNode表示可以返回给客户端的结果集。
 *              游标可以是由DECLARE CURSOR语句创建的命名游标，
 *              或者它可以是与SELECT语句关联的未命名游标（更多
 *              确切地说，是一个将表返回给客户端的表表达式。 在里面
 *              后一种情况，光标没有名称
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-02 19:39
 */
public class CursorNode extends DMLStatementNode {

    public final static int READ_ONLY = 1;

    public CursorNode(String statementType, ResultSetNode resultSet, ContextManager contextManager) {
        super(resultSet, contextManager);
    }

    @Override
    public int activationKind() {
        return NEED_CURSOR_ACTIVATION;
    }

    @Override
    public void generate(ActivationClassBuilder acb, MethodBuilder mb) {
        //generateParameterValueSet(acb);
        resultSet.markStatementResultSet();
        resultSet.generate(acb, mb);

    }

    @Override
    public void bindStatement() {
        //获取数据字典
        DataDictionary dataDictionary = getDataDictionary();
        FromList fromList = new FromList(getContextManager());
        //主要是对结果集的绑定
        super.bind(dataDictionary);
        resultSet.bindResultColumns(fromList);
        resultSet.renameGeneratedResultNames();
    }


    @Override
    public void optimizeStatement() {
        super.optimizeStatement();
    }


}
