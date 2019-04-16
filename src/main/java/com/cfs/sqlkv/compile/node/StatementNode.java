package com.cfs.sqlkv.compile.node;

import com.cfs.sqlkv.common.context.ContextManager;
import com.cfs.sqlkv.compile.ActivationClassBuilder;
import com.cfs.sqlkv.compile.result.ResultDescription;
import com.cfs.sqlkv.compile.result.ResultSet;
import com.cfs.sqlkv.engine.execute.ConstantActionActivation;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.sql.activation.BaseActivation;
import com.cfs.sqlkv.sql.activation.CursorActivation;
import com.cfs.sqlkv.util.ByteArray;

import java.lang.reflect.Modifier;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 19:43
 */
public abstract class StatementNode extends QueryTreeNode {

    public StatementNode(ContextManager contextManager) {
        super(contextManager);
    }

    /**
     * 绑定Statement
     */
    public void bindStatement() {

    }

    public ResultDescription makeResultDescription() {
        return null;
    }

    static final int NEED_DDL_ACTIVATION = 5;
    static final int NEED_CURSOR_ACTIVATION = 4;
    static final int NEED_PARAM_ACTIVATION = 2;
    static final int NEED_ROW_ACTIVATION = 1;
    static final int NEED_NOTHING_ACTIVATION = 0;

    /**
     * 自动生成激活器
     */
    public GeneratedClass generate(ByteArray byteCode) {
        //获取节点激活种类
        int nodeChoice = activationKind();

        String superClass;
        switch (nodeChoice) {
            case NEED_CURSOR_ACTIVATION:
                superClass = CursorActivation.module;
                break;
            case NEED_NOTHING_ACTIVATION:
            case NEED_ROW_ACTIVATION:
            case NEED_PARAM_ACTIVATION:
                superClass = BaseActivation.className;
                break;

            case NEED_DDL_ACTIVATION:
                return getClassFactory().loadGeneratedClass(ConstantActionActivation.class.getName(), null);
            default:
                throw new RuntimeException("not a available Activation");
        }
        ActivationClassBuilder generatingClass = new ActivationClassBuilder(superClass, null, getCompilerContext());


        //构建createResultSet方法
        MethodBuilder mbWorker = generatingClass.getClassBuilder().newMethodBuilder(Modifier.PUBLIC, ResultSet.class.getName(),
                "createResultSet");
        //构建其具体执行过程
        generate(generatingClass, mbWorker);
        //添加返回类型
        mbWorker.methodReturn();
        mbWorker.complete();
        generatingClass.finishExecuteMethod();
        //技术构造器的生成
        generatingClass.finishConstructor();
        GeneratedClass activationClass = generatingClass.getGeneratedClass(byteCode);
        return activationClass;
    }

    public abstract int activationKind();

    public void optimizeStatement() {

    }

    abstract String statementToString();

}
