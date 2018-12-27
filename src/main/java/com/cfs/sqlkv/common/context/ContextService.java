package com.cfs.sqlkv.common.context;

import java.util.HashSet;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 19:05
 */
public class ContextService {

    private static ContextService INSTANCE;

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
        ContextService.INSTANCE = this;
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

    }

    /**
     * @return 返回当前线程ContextManager
     * */
    public ContextManager getCurrentContextManager(){

    }

    public void resetCurrentContextManager(ContextManager cm){

    }


}
