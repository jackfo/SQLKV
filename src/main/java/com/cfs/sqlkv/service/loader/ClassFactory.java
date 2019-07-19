package com.cfs.sqlkv.service.loader;


import com.cfs.sqlkv.util.ByteArray;

import java.io.ObjectStreamClass;

public interface ClassFactory {

	/**
	 *	Add a generated class to the class manager's class repository.
	 */
	public GeneratedClass loadGeneratedClass(String fullyQualifiedName, ByteArray classDump)  ;

	/**返回一个类检查对象*/
	public ClassInspector	getClassInspector();

    /**根据类名加载应用类*/
	public Class loadApplicationClass(String className) throws ClassNotFoundException;

	/**
		Load an application class, or a class that is potentially an application class.

		@exception ClassNotFoundException Class cannot be found, or
		a SecurityException or LinkageException was thrown loading the class.
	*/
	public Class loadApplicationClass(ObjectStreamClass classDescriptor) throws ClassNotFoundException;

	/**
		Was the passed in class loaded by a ClassManager.

		@return true if the class was loaded by a SQLKV class manager,
		false it is was loaded by the system class loader, or another class loader.
	*/
	public boolean isApplicationClass(Class theClass);

	/**
		Notify the class manager that a jar file has been modified.
		@param reload Restart any attached class loader

		@exception StandardException thrown on error
	*/
	public void notifyModifyJar(boolean reload)   ;

	/**
		Notify the class manager that the classpath has been modified.

		@exception StandardException thrown on error
	*/
	public void notifyModifyClasspath(String classpath)   ;

	/**
		Return the in-memory "version" of the class manager. The version
		is bumped everytime the classes are re-loaded.
	*/
	public int getClassLoaderVersion();
}
