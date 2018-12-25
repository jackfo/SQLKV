package com.cfs.sqlkv.jdbc;

import java.beans.Statement;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 16:14
 */
public class EmbedStatement extends Statement {
    /**
     * Creates a new {@link Statement} object
     * for the specified target object to invoke the method
     * specified by the name and by the array of arguments.
     * <p>
     * The {@code target} and the {@code methodName} values should not be {@code null}.
     * Otherwise an attempt to execute this {@code Expression}
     * will result in a {@code NullPointerException}.
     * If the {@code arguments} value is {@code null},
     * an empty array is used as the value of the {@code arguments} property.
     *
     * @param target     the target object of this statement
     * @param methodName the name of the method to invoke on the specified target
     * @param arguments  the array of arguments to invoke the specified method
     */
    public EmbedStatement(Object target, String methodName, Object[] arguments) {
        super(target, methodName, arguments);
    }
}
