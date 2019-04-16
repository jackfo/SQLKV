package com.cfs.sqlkv.service.reflect;

import com.cfs.sqlkv.context.Context;

import com.cfs.sqlkv.service.loader.*;

import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 20:08
 */
public class ReflectGeneratedClass implements GeneratedClass {

    private final Hashtable<String, GeneratedMethod> methodCache;
    private static final GeneratedMethod[] directs;

    private final ClassInfo ci;
    private final int classLoaderVersion;

    /**
     * 构建10个自动生成方法
     * */
    static {
        directs = new GeneratedMethod[10];
        for (int i = 0; i < directs.length; i++) {
            directs[i] = new DirectCall(i);
        }
    }

    public ReflectGeneratedClass(ClassFactory cf, Class jvmClass) {
        ci = new ClassInfo(jvmClass);
        classLoaderVersion = cf.getClassLoaderVersion();
        methodCache = new Hashtable<String, GeneratedMethod>();
    }


    @Override
    public String getName() {
        return ci.getClassName();
    }

    @Override
    public Object newInstance(Context context) {
        Throwable t;
        try {
            GeneratedByteCode ni = (GeneratedByteCode) ci.getNewInstance();
            ni.initFromContext(context);
            ni.setGC(this);
            ni.postConstructor();
            return ni;
        } catch (InstantiationException ie) {
            t = ie;
        } catch (IllegalAccessException iae) {
            t = iae;
        } catch (java.lang.reflect.InvocationTargetException ite) {
            t = ite;
        } catch (NoSuchMethodException le) {
            t = le;
        } catch (LinkageError le) {
            t = le;
        }
        throw new RuntimeException(t.getMessage());
    }

    @Override
    public GeneratedMethod getMethod(String simpleName) {
        GeneratedMethod rm = methodCache.get(simpleName);
        if (rm != null) {
            return rm;
        }
        if ((simpleName.length() == 2) && simpleName.startsWith("e")) {
            int id = ((int) simpleName.charAt(1)) - '0';
            rm = directs[id];
        } else {
            Method m = null;
            try {
                m = getJVMClass().getMethod(simpleName, (Class[]) null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            rm = new ReflectMethod(m);
        }
        methodCache.put(simpleName, rm);
        return rm;
    }

    protected Class<?> getJVMClass() {
        return ci.getClassObject();
    }

    @Override
    public int getClassLoaderVersion() {
        return classLoaderVersion;
    }
}

class DirectCall implements GeneratedMethod {

    private final int which;

    DirectCall(int which) {

        this.which = which;
    }

    public Object invoke(Object ref) {


        GeneratedByteCode gref = (GeneratedByteCode) ref;
        switch (which) {
            case 0:
                return gref.e0();
            case 1:
                return gref.e1();
            case 2:
                return gref.e2();
            case 3:
                return gref.e3();
            case 4:
                return gref.e4();
            case 5:
                return gref.e5();
            case 6:
                return gref.e6();
            case 7:
                return gref.e7();
            case 8:
                return gref.e8();
            case 9:
                return gref.e9();
        }
        return null;


    }
}
