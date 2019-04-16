package com.cfs.sqlkv.service.reflect;


import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.service.loader.ClassInspector;
import com.cfs.sqlkv.service.loader.GeneratedClass;
import com.cfs.sqlkv.util.ByteArray;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-03-03 20:07
 */
public class ReflectClasses implements ClassFactory {

    private Map<String,ReflectGeneratedClass> preCompiled = new HashMap<String,ReflectGeneratedClass>();

    private int action = -1;

    synchronized ReflectGeneratedClass loadGeneratedClassFromData(String fullyQualifiedName, ByteArray classDump) {
        if (classDump == null || classDump.getArray() == null) {
            ReflectGeneratedClass gc = preCompiled.get(fullyQualifiedName);
            if(gc!=null){
                return gc;
            }

            try {
                Class jvmClass = Class.forName(fullyQualifiedName);
                gc = new ReflectGeneratedClass(this, jvmClass);
                preCompiled.put(fullyQualifiedName, gc);
                return gc;
            } catch (ClassNotFoundException cnfe) {
                throw new NoClassDefFoundError(cnfe.toString());
            }
        }

        ReflectLoader reflectLoader = new ReflectLoader(getClass().getClassLoader(), this);
        return reflectLoader.loadGeneratedClass(fullyQualifiedName, classDump);
    }

    /**
     * 根据类名或者字节码流加载对应的类
     * @param fullyQualifiedName 全限定类名
     * @param classDump 类的二进制文件
     * */
    @Override
    public GeneratedClass loadGeneratedClass(String fullyQualifiedName, ByteArray classDump)   {
        return loadGeneratedClassFromData(fullyQualifiedName, classDump);
    }

    @Override
    public ClassInspector getClassInspector() {
        return null;
    }

    @Override
    public Class loadApplicationClass(String className) throws ClassNotFoundException {
        return null;
    }

    @Override
    public Class loadApplicationClass(ObjectStreamClass classDescriptor) throws ClassNotFoundException {
        return null;
    }

    @Override
    public boolean isApplicationClass(Class theClass) {
        return false;
    }

    @Override
    public void notifyModifyJar(boolean reload)   {

    }

    @Override
    public void notifyModifyClasspath(String classpath)   {

    }

    @Override
    public int getClassLoaderVersion() {
        return 0;
    }
}
