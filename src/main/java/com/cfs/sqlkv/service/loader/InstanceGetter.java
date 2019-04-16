package com.cfs.sqlkv.service.loader;

import java.lang.reflect.InvocationTargetException;

public interface InstanceGetter {

	/**
	  *创建一个类的实例
	  */
	public Object getNewInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException;
}
