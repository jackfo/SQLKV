package com.cfs.sqlkv.util;


import com.cfs.sqlkv.catalog.types.TypeDescriptorImpl;
import com.cfs.sqlkv.service.io.FormatableInstanceGetter;
import com.cfs.sqlkv.service.io.TypesImplInstanceGetter;
import com.cfs.sqlkv.service.loader.ClassInfo;
import com.cfs.sqlkv.service.loader.InstanceGetter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-07 10:25
 */
public class InstanceUtil {

    private static final String[] classArray = new String[256];

    static {
        classArray[14] = TypeDescriptorImpl.class.getName();
        classArray[15] = TypesImplInstanceGetter.class.getName();
        classArray[16] = TypesImplInstanceGetter.class.getName();
        classArray[17] = TypesImplInstanceGetter.class.getName();
        classArray[18] = TypesImplInstanceGetter.class.getName();
        classArray[19] = TypesImplInstanceGetter.class.getName();
        classArray[20] = TypesImplInstanceGetter.class.getName();
        classArray[21] = TypesImplInstanceGetter.class.getName();
        classArray[22] = TypesImplInstanceGetter.class.getName();
        classArray[23] = TypesImplInstanceGetter.class.getName();
        classArray[25] = TypesImplInstanceGetter.class.getName();
        classArray[28] = TypesImplInstanceGetter.class.getName();
        classArray[30] = TypesImplInstanceGetter.class.getName();
        classArray[32] = TypesImplInstanceGetter.class.getName();
        classArray[33] = TypesImplInstanceGetter.class.getName();
        classArray[34] = TypesImplInstanceGetter.class.getName();
    }





    public static Object newInstanceFromIdentifier(int identifier) {
        InstanceGetter ci = classFromIdentifier(identifier);
        Throwable t;

        Object result = null;
        try {
            result = ci.getNewInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;

    }

    public static InstanceGetter classFromIdentifier(int identifier) {
        String className = classArray[identifier];

        if (className != null) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                final Constructor<?> constructor = clazz.getDeclaredConstructor();

                if (FormatableInstanceGetter.class.isAssignableFrom(clazz)) {
                    FormatableInstanceGetter tfig = null;
                    try {
                        tfig = (FormatableInstanceGetter) constructor.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    tfig.setFormatId(identifier);
                    return tfig;
                }
                return new ClassInfo(clazz);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("");
    }

}
