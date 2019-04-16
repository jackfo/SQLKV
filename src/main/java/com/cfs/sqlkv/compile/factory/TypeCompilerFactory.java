package com.cfs.sqlkv.compile.factory;

import com.cfs.sqlkv.catalog.types.TypeId;
import com.cfs.sqlkv.compile.BaseTypeCompiler;
import com.cfs.sqlkv.compile.TypeCompiler;
import com.cfs.sqlkv.engine.execute.RowUtil;

import java.sql.Types;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 16:31
 */
public class TypeCompilerFactory {
    private static final String PACKAGE_NAME = "com.cfs.sqlkv.compile.";
    public static TypeCompiler intTypeCompiler;
    public static TypeCompiler refTypeCompiler;
    static TypeCompiler realTypeCompiler ;

    public static TypeCompiler staticGetTypeCompiler(TypeId typeId) {
        switch (typeId.getJDBCTypeId()) {
            case Types.INTEGER:
                return intTypeCompiler =
                        getAnInstance(PACKAGE_NAME + "NumericTypeCompiler",
                                intTypeCompiler,
                                typeId);

            case Types.REAL:
                return realTypeCompiler =
                        getAnInstance(PACKAGE_NAME + "NumericTypeCompiler",
                                realTypeCompiler,
                                typeId);
            case Types.JAVA_OBJECT:
            case Types.OTHER:
                if (typeId.isRefTypeId()) {
                    return refTypeCompiler = getAnInstance(
                            PACKAGE_NAME + "RefTypeCompiler",
                            refTypeCompiler,
                            typeId);
                } else {
                    throw new RuntimeException("");
                }


        }
        return null;
    }

    private static TypeCompiler getAnInstance(String className, TypeCompiler anInstance, TypeId typeId) {
        if (anInstance == null) {
            Exception exc = null;
            Class<?> typeCompilerClass = null;

            try {
                typeCompilerClass = Class.forName(className);
                anInstance = (TypeCompiler)
                        typeCompilerClass.getConstructor().newInstance();
                ((BaseTypeCompiler) anInstance).setTypeId(typeId);
            } catch (ClassNotFoundException cnfe) {
                exc = cnfe;
            } catch (IllegalAccessException iae) {
                exc = iae;
            } catch (InstantiationException ie) {
                exc = ie;
            } catch (NoSuchMethodException nsme) {
                exc = nsme;
            } catch (java.lang.reflect.InvocationTargetException ite) {
                exc = ite;
            }
            if (exc != null) {
                throw new RuntimeException(exc.getMessage());
            }

        }

        return anInstance;
    }
}

