package com.cfs.sqlkv.common.context;

import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.context.ContextImpl;

import java.util.HashSet;
import java.util.Stack;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 19:05
 */
public class ContextService {

    private static ContextService INSTANCE;

    static {
        INSTANCE = new ContextService();
    }

    /**
     * 维护当前线程创建的所有上下文。
     * 这些上下文是指存在于当前线程的线程thread local中的变量或者以下的情况:
     *     ContextManager-当前线程使用或者正在应用的context manager
     *     如果ContextManager.
     * */

    private ThreadLocal<Object> threadContextList = new ThreadLocal<Object>();


    /**每一个ContextService维持一个ContextManager集合*/
    private HashSet<ContextManager> allContexts;

    /**
     * 在创建ContextService实例的时候,会将当前对象交给factory,保证工厂始终是单例的
     * */
    public ContextService(){
        allContexts = new HashSet<ContextManager>();
    }

    /**
     * 获取ContextService单例
     * */
    public static ContextService getInstance(){
        return INSTANCE;
    }

    /**
     * @param contextId 上下文id
     * @return 返回相应的上下文
     * */
    public static Context getContext(String contextId){
        ContextManager cm = getInstance().getCurrentContextManager();
        if(cm==null){
            cm = getInstance().newContextManager();
        }
        return cm.getContext(contextId);
    }



    /**
     * @return 返回当前线程ContextManager
     * */
    public ContextManager getCurrentContextManager(){
        ThreadLocal<Object> tcl = threadContextList;
        if (tcl == null) {
            return null;
        }
        Object list = tcl.get();
        if (list instanceof ContextManager) {
            Thread me = Thread.currentThread();
            ContextManager cm = (ContextManager) list;
            if (cm.activeThread == me){
                return cm;
            }
            return null;
        }
        if (list == null){
            return null;
        }
        return ((Stack<ContextManager>)list).peek();
    }



    /**
     * 重置线程上下文管理器
     * */
    public void resetCurrentContextManager(ContextManager cm){
        ThreadLocal<Object> tcl = threadContextList;
        if (tcl == null) {
            return;
        }
        if (cm.activeCount != -1) {
            if (--cm.activeCount == 0) {
                cm.activeThread = null;
                if (cm.isEmpty()){
                    tcl.set(null);
                }
            }
            return;
        }
    }


    public void setCurrentContextManager(ContextManager cm) {
        Thread me = null;
        if (cm.activeThread == null) {
            cm.activeThread = (me = Thread.currentThread());
        }
        //将上下文管理器添加到当前线程
        if (addToThreadList(me, cm)){
            cm.activeCount++;
        }
    }

    /**
     * 添加上下文管理器到指定的线程
     * @return 如果线程局部变量里面是传入上下文管理器则返回true,其它情况都是返回false
     * */
    private boolean addToThreadList(Thread me, ContextManager associateCM) {
        ThreadLocal<Object> tcl = threadContextList;
        //表示线程上下文没有被使用
        if (tcl == null) {
            return false;
        }
        //如果当前线程的上下文管理和传入的上下文管理器相同则直接返回
        Object list = tcl.get();
        if (associateCM == list){
            return true;
        }
        //如果线程不存在上下文管理器,则直接设置传入的上下文管理器
        if (list == null) {
            tcl.set(associateCM);
            return true;
        }

        //线程上下文管理器不是传入的线程上下文管理器的时候需要维持到一个栈里面
        Stack<ContextManager> stack;
        if(list instanceof ContextManager){
            ContextManager threadsCM = (ContextManager) list;
            if (me == null){
                me = Thread.currentThread();
            }
            if (threadsCM.activeThread != me) {
                tcl.set(associateCM);
                return true;
            }
            stack = new Stack<>();
            tcl.set(stack);
            for (int i = 0; i < threadsCM.activeCount; i++) {
                stack.push(threadsCM);
            }
            threadsCM.activeCount = -1;
        }else{
            stack =(Stack<ContextManager>) list;
        }
        stack.push(associateCM);
        associateCM.activeCount = -1;
        //这里返回false的原因,表示当前线程里面的上下文管理器不是传入的线程上下文管理器,是栈结果则肯定不是传入的线程上下文管理器
        return false;
    }

    /**
     * 创建的上下文需要添加到系统中去,构造为当前线程上下文管理器
     * */
    public ContextManager newContextManager(){
        ContextManager cm = new ContextManager(this);
        new ContextImpl(cm,"systemContext");
        synchronized (this) {
            allContexts.add(cm);
        }
        return cm;
    }

    public static Context getContextOrNull(String contextId) {
        ContextService csf = INSTANCE;
        if (csf == null){
            return null;
        }
        ContextManager cm = csf.getCurrentContextManager();
        if (cm == null){
            return null;
        }
        return cm.getContext(contextId);
    }
}
