package com.cfs.sqlkv.service.bytecode;


import com.cfs.sqlkv.util.ByteArray;
import com.cfs.sqlkv.service.compiler.ClassBuilder;
import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.service.loader.GeneratedClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * This is a common superclass for the various impls.
 * Saving class files is a common thing to do.
 *
 */
public abstract class GClass implements ClassBuilder {

	protected ByteArray bytecode;
	protected final ClassFactory cf;
	protected final String qualifiedName;



	public GClass(ClassFactory cf, String qualifiedName) {
		this.cf = cf;
		this.qualifiedName = qualifiedName;
	}
	public String getFullName() {
		return qualifiedName;
	}
	public GeneratedClass getGeneratedClass()   {
		return cf.loadGeneratedClass(qualifiedName, getClassBytecode());
	}

	protected void writeClassFile(String dir, boolean logMessage, Throwable t)
		  {


	}

}
