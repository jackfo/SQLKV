package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.sql.activation.Activation;

/**
 * 该接口描述在执行时总是为语句执行的操作。例如，DDL语句使用它来描述应该将哪些内容放入目录
 *
 * 满足此接口的对象被放入PreparedStatement并在执行时运行。因此，常量操作可以跨线程共享，并且不能在任何实例字段中存储特定于连接/线程的信息。
 */
public interface ConstantAction {

    public void	executeConstantAction(Activation activation ) throws StandardException;


}
