package com.cfs.sqlkv.service.reflect;

import com.cfs.sqlkv.service.loader.GeneratedMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-10 15:54
 */
public class ReflectMethod implements GeneratedMethod {
    private final Method realMethod;

    public ReflectMethod(Method m) {
        super();
        realMethod = m;
    }

    public Object invoke(Object ref){
        Throwable t;
        try {
            return realMethod.invoke(ref, null);
        } catch (IllegalAccessException iae) {
            t = iae;
        } catch (IllegalArgumentException iae2) {
            t = iae2;
        } catch (InvocationTargetException ite) {
            t = ite;
        }
        throw new RuntimeException(t.getMessage());
    }
}
