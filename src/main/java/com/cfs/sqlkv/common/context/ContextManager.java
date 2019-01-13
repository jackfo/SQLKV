package com.cfs.sqlkv.common.context;

import com.cfs.sqlkv.context.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 19:10
 */
public class ContextManager {

    private final HashMap<String,CtxStack> ctxTable = new HashMap<String,CtxStack>();


    /**
     * 获取当前contextId所对应栈中的线程上下文
     * @param contextId 上下文id类型 如jdbc连接 statement
     * */
    public Context getContext(String contextId) {
        final CtxStack idStack = ctxTable.get(contextId);
        return (idStack==null?null:idStack.top());
    }


    public void pushContext(Context context){
        final String contextId = context.getIdName();
        CtxStack idStack = ctxTable.get(contextId);

        // if the stack is null, create a new one.
        if (idStack == null) {
            idStack = new CtxStack();
            ctxTable.put(contextId, idStack);
        }
        idStack.push(context);
    }

    /**
     * 上下文栈
     * */
    private static final class CtxStack {
        private final ArrayList<Context> stack_ = new ArrayList<Context>();
        private final List<Context> view_ = Collections.unmodifiableList(stack_);
        private Context top_ = null;
        void push(Context context) {
            stack_.add(context);
            top_ = context;
        }
        void pop() {
            stack_.remove(stack_.size()-1);
            top_ = stack_.isEmpty() ?
                    null : stack_.get(stack_.size()-1);
        }
        void remove(Context context) {
            if (context == top_) {
                pop();
                return;
            }
            stack_.remove(stack_.lastIndexOf(context));
        }
        Context top() {
            return top_;
        }
        boolean isEmpty() { return stack_.isEmpty(); }

        List<Context> getUnmodifiableList() {
            return view_;
        }
    }

}
