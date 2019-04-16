package com.cfs.sqlkv.service.bytecode;


import com.cfs.sqlkv.service.classfile.ClassHolder;
import com.cfs.sqlkv.service.classfile.ClassMember;
import com.cfs.sqlkv.service.compiler.LocalField;
import com.cfs.sqlkv.service.compiler.MethodBuilder;
import com.cfs.sqlkv.service.loader.ClassFactory;
import com.cfs.sqlkv.util.ByteArray;

import java.io.IOException;
import java.security.AccessController;

/**
 * ClassBuilder is used to construct a java class's byte array
 * representation.
 *
 * Limitations:
 *   No checking for language use violations such as invalid modifiers
 *	or duplicate field names.
 *   All classes must have a superclass; java.lang.Object must be
 *      supplied if there is no superclass.
 *
 * <p>
 * When a class is first created, it has:
 * <ul>
 * <li> a superclass
 * <li> modifiers
 * <li> a name
 * <li> a package
 * <li> no superinterfaces, methods, fields, or constructors
 * <li> an empty static initializer
 * <li> an empty initializer
 * </ul>
 * <p>
 * MethodBuilder implementations are required to supply a way for
 * Generators to give them code.  Most typically, they may have
 * a stream to which the Generator writes the code that is of
 * the type to satisfy what the Generator is writing.
 * <p>
 * BCClass is a ClassBuilder implementation for generating java bytecode
 * directly.
 *
 */
public class BCClass extends GClass {
	
	/**
	 * Simple text indicating any limits execeeded while generating
	 * the class file.
	 */
	String limitMsg;
	

    /**
	 * 给类添加相应的字段,注在这里字段不会进行初始化
	 * @param javaType 字段类型
	 * @param name     字段名
	 * @param modifiers 字段修饰符
	 * */
	public LocalField addField(String javaType, String name, int modifiers) {
        //根据Java类型获取VM类型
		Type type = factory.type(javaType);
		// put it into the class holder right away.
		ClassMember field = classHold.addMember(name, type.vmName(), modifiers);
		int cpi = classHold.addFieldReference(field);
		return new BCLocalField(type, cpi);
	}

	/**
	 * At the time the class is completed and bytecode
	 * generated, if there are no constructors then
	 * the default no-arg constructor will be defined.
	 */
	public ByteArray getClassBytecode()   {

		// return if already done
		if (bytecode != null) return bytecode;
		
		try {
			// the class is now complete, get its bytecode.
			bytecode = classHold.getFileFormat();
			
		} catch (IOException ioe) {

		}

		// release resources, we have the code now.
		// name is not released, it may still be accessed.
		classHold = null;




		


		return bytecode;
	}


	/**
	 * the class's unqualified name
	 */
	public String getName() {
		return name;
	}
 
	/**
	 * a method. Once it is created, thrown
	 * exceptions, statements, and local variable declarations
	 * must be added to it. It is put into its defining class
	 * when it is created.
	 * <verbatim>
	   Java: #modifiers #returnType #methodName() {}
	  		// modifiers is the | of the JVM constants for
	  		// the modifiers such as static, public, etc.
	   </verbatim>
	 * <p>
	 * This is used to start a constructor as well; pass in
	 * null for the returnType when used in that manner.
	 *
	 * See java.lang.reflect.Modifiers
	 * @param modifiers the | of the Modifiers
	 *	constants representing the visibility and control of this
	 *	method.
	 * @param returnType the return type of the method as its
	 *	Java language type name.
	 * @param methodName the name of the method.
	 *
	 * @return the method builder.
	 */
	public MethodBuilder newMethodBuilder(int modifiers, String returnType,
		String methodName) {

		return newMethodBuilder(modifiers, returnType,
			methodName, (String[]) null);

	}


	/**
	 * a method with parameters. Once it is created, thrown
	 * exceptions, statements, and local variable declarations
	 * must be added to it. It is put into its defining class
	 * when it is created.
	 * <verbatim>
	   Java: #modifiers #returnType #methodName() {}
	  		// modifiers is the | of the JVM constants for
	  		// the modifiers such as static, public, etc.
	   </verbatim>
	 * <p>
	 * This is used to start a constructor as well; pass in
	 * null for the returnType when used in that manner.
	 *
	 * See java.lang.reflect.Modifiers
	 * @param modifiers the | of the Modifiers
	 *	constants representing the visibility and control of this
	 *	method.
	 * @param returnType the return type of the method as its
	 *	Java language type name.
	 * @param methodName the name of the method.
	 * @param parms an array of ParameterDeclarations representing the
	 *				method's parameters
	 *
	 * @return the method builder.
	 */
	public MethodBuilder newMethodBuilder(int modifiers, String returnType,
		String methodName, String[] parms) {


		BCMethod m = new BCMethod(this,
									returnType,
									methodName,
									modifiers,
									parms,
									factory);

		return m;
		
	}


	/**
	 * a constructor. Once it is created, thrown
	 * exceptions, statements, and local variable declarations
	 * must be added to it. It is put into its defining class
	 * when it is created.
	 * <verbatim>
	   Java: #modifiers #className() {}
	  		// modifiers is the | of the JVM constants for
	  		// the modifiers such as static, public, etc.
	  		// className is taken from definingClass.getName()
	   </verbatim>
	 * <p>
	 * This is used to start a constructor as well; pass in
	 * null for the returnType when used in that manner.
     * <p>
	 *
	 * See Modifiers
	 * @param modifiers the | of the Modifiers
	 *	constants representing the visibility and control of this
	 *	method.
	 *
	 * @return the method builder for the constructor.
	 */
	public MethodBuilder newConstructorBuilder(int modifiers) {
        return new BCMethod(this, "void", "<init>", modifiers, null, factory);
	}
  	//
	// class interface
	//

	String getSuperClassName() {
		return superClassName;
	}

	/**
	 * Let those that need to get to the
	 * classModify tool to alter the class definition.
	 */
	ClassHolder modify() {
		return classHold;
	}

	/*
	** Method descriptor caching
	*/

	public BCClass(ClassFactory cf, String packageName, int classModifiers,
            String className, String superClassName,
            BCJava factory) {

		super(cf, packageName.concat(className));


		// by the time the constructor is done, we have:
		//
		// package #packageName;
		// #classModifiers class #className extends #superClassName
		// { }
		//

		name = className;
		if (superClassName == null)
			superClassName = "java.lang.Object";
		this.superClassName = superClassName;

		classType = factory.type(getFullName());

		classHold = new ClassHolder(qualifiedName, factory.type(superClassName).vmNameSimple, classModifiers);

		this.factory = factory;
	}

	protected ClassHolder classHold;

	protected String superClassName;
	protected String name;

	BCJava factory;
	final Type classType;

	ClassFactory getClassFactory() {
		return cf;
	}

	/**
	 * Add the fact that some class limit was exceeded while generating
	 * the class. We create a set of them and report at the end, this
	 * allows the generated class file to still be dumped.
	 * @param mb
	 * @param limitName
	 * @param limit
	 * @param value
	 */
	void addLimitExceeded(BCMethod mb, String limitName, int limit, int value)
	{
		StringBuffer sb = new StringBuffer();
		if (limitMsg != null)
		{
			sb.append(limitMsg);
			sb.append(", ");
		}
		
		sb.append("method:");
		sb.append(mb.getName());
		sb.append(" ");
		sb.append(limitName);
		sb.append(" (");
		sb.append(value);
		sb.append(" > ");
		sb.append(limit);
		sb.append(")");
		
		limitMsg = sb.toString();
	}
    
    /**
     * Add the fact that some class limit was exceeded while generating
     * the class. Text is the simple string passed in.
     * @param rawText Text to be reported.
     * 
     * @see BCClass#addLimitExceeded(BCMethod, String, int, int)
     */
    void addLimitExceeded(String rawText)
    {
        if (limitMsg != null)
        {
            limitMsg = limitMsg + ", " + rawText;
       }
        else
        {
            limitMsg = rawText;
        }
    }

}
