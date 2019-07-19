package com.cfs.sqlkv.service.loader;

import com.cfs.sqlkv.context.Context;


/**
	A meta-class that represents a generated class.
	(Similar to java.lang.Class).
*/

public interface GeneratedClass {

	/**
		Return the name of the generated class.
	*/
	public String getName();

	/**
		Return a new object that is an instance of the represented
		class. The object will have been initialised by the no-arg
		constructor of the represneted class.
		(Similar to java.lang.Class.newInstance).

		@exception 	StandardException	Standard SQLKV error policy

	*/
	public Object newInstance(Context context)
		 ;

	/**
		Obtain a handle to the method with the given name
		that takes no arguments.


	*/
	public GeneratedMethod getMethod(String simpleName)
		 ;

	/**
		Return the class reload version that this class was built at.
	*/
	public int getClassLoaderVersion();
}

