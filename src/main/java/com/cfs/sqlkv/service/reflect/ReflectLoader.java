package com.cfs.sqlkv.service.reflect;

import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.util.ByteArray;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 20:16
 */
public class ReflectLoader extends ClassLoader {

    private final ReflectClasses reflectClasses;

    public ReflectLoader(ClassLoader parent, ReflectClasses reflectClasses){
        super(parent);
        this.reflectClasses = reflectClasses;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        return reflectClasses.loadApplicationClass(name);
    }

    /**
     * 根据JDK的类加载器加载出对应的字节码文件,之后将其封装到反射自动生成类
     * */
    public ReflectGeneratedClass loadGeneratedClass(String name, ByteArray classData) {
        Class jvmClass = defineClass(name, classData.getArray(), classData.getOffset(), classData.getLength());
        resolveClass(jvmClass);
        return new ReflectGeneratedClass(reflectClasses, jvmClass);
    }


}
