package com.cfs.sqlkv.service.bytecode;

import com.cfs.sqlkv.service.classfile.*;
import com.cfs.sqlkv.common.sanity.SanityManager;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Objects;

/**
 * Validate BC calls.
 *
 */
class d_BCValidate
{

	private static final String[] csPackages = {
		"java",
		"org.apache.derby.exe.",
		"org.apache.derby.iapi.",
		"org.apache.derby.jdbc.",
		"org.apache.derby.iapi.",
		"org.apache.derby.impl.",
		"org.apache.derby.authentication.",
		"org.apache.derby.catalog.",
		"org.apache.derby.iapi.db.",
		"org.apache.derby.iapi.types.",
		"org.apache.derby.iapi.types.",
		"org.apache.derby.catalog.types.",
		};


	private static final Class[] NO_PARAMS = new Class[0];

	static void checkMethod(short opcode, Type dt, String methodName, String[] debugParameterTypes, Type rt) {


	}

	private static Hashtable<String,Class<?>> primitives;

	static {
		if (SanityManager.DEBUG) {
			primitives = new Hashtable<String,Class<?>>();
			primitives.put("boolean", Boolean.TYPE);
			primitives.put("byte", Byte.TYPE);
			primitives.put("char", Character.TYPE);
			primitives.put("double", Double.TYPE);
			primitives.put("float", Float.TYPE);
			primitives.put("int", Integer.TYPE);
			primitives.put("long", Long.TYPE);
			primitives.put("short", Short.TYPE);
			primitives.put("void", Void.TYPE);
		}

	}
	

	private static Class loadClass(String name) throws ClassNotFoundException {

		if (SanityManager.DEBUG) {

			Class c = (Class) primitives.get(name);
			if (c != null)
				return c;

			if (name.endsWith("[]")) {
				Class baseClass = loadClass(name.substring(0, name.length() - 2));
				return Array.newInstance(baseClass, 0).getClass();
			}
			
			return Class.forName(name);
		}

		return null;
	}
}
