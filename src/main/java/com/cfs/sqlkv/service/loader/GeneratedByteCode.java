package com.cfs.sqlkv.service.loader;


import com.cfs.sqlkv.context.Context;


/**
	Generated classes must implement this interface.

*/
public interface GeneratedByteCode {

	/**
		Initialize the generated class from a context.
		Called by the class manager just after
		creating the instance of the new class.
	*/
	public void initFromContext(Context context)
		 ;

	/**
		Set the Generated Class. Call by the class manager just after
		calling initFromContext.
	*/
	public void setGC(GeneratedClass gc);

	/**
		Called by the class manager just after calling setGC().
	*/
	public void postConstructor();

	/**
		Get the GeneratedClass object for this object.
	*/
	public GeneratedClass getGC();

	public GeneratedMethod getMethod(String methodName)  ;


	public Object e0()   ;
	public Object e1()   ;
	public Object e2()   ;
	public Object e3()   ;
	public Object e4()   ;
	public Object e5()   ;
	public Object e6()   ;
	public Object e7()   ;
	public Object e8()   ;
	public Object e9()   ;
}
