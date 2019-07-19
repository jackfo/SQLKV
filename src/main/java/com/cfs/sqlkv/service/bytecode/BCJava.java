package com.cfs.sqlkv.service.bytecode;

import com.cfs.sqlkv.common.sanity.SanityManager;

import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.cache.CacheableFactory;
import com.cfs.sqlkv.service.classfile.ClassHolder;
import com.cfs.sqlkv.service.classfile.VMDescriptor;
import com.cfs.sqlkv.service.compiler.ClassBuilder;
import com.cfs.sqlkv.service.compiler.JavaFactory;
import com.cfs.sqlkv.service.loader.ClassFactory;
/**
	<p>
	<b>Debugging problems with generated classes</b>
	<p>
	When the code has been generated incorrectly, all sorts of
	odd things can go wrong.  This is one recommended approach to
	finding the problem.
	<p>
	First, turn on ByteCodeGenInstr and DumpClassFile. Look
	for missing files (right now they are consecutively numbered
	by the activation class builder; later on they won't be, but
	BytCodeGenInstr dumps messages about the classes it has).
	Look at the log to make sure that all "GEN starting class/method"
	messages are paired with a "GEN ending class/method" message.
	If a file is missing or the pairing is missing, then something
	went wrong when the system tried to generate the bytecodes.
	Resort to your favorite debugging tool to step through
	the faulty statement.
	<p>
	If you get class files but the system crashes on you (I had
	an OS segmentation fault once) or you get funny messages like
	JDBC Excpetion: ac5 where ac5 is just the name of a generated
	class, then one of the following is likely:
	<ul>
	<li> you are calling INVOKEVIRTUAL when
	     you are supposed to call INVOKEINTERFACE
	<li> you have an inexact match on a method argument or
	     return type.
	<li> you are trying to get to a superclass's field using
	     a subclass.
	</ul>
	The best way to locate the problem here is to do this (replace
	ac5.class with the name of your class file):
	<ol>
	<li> javap -c -v ac5 &gt; ac5.gp<br>
		 if javap reports "Class not found", and the file ac5.class does
		 exist in the current directory, then the .class file is probably
		 corrupt.  Try running mocha on it to see if that works. The
		 problem will be in the code that generates the entries for
		 the class file -- most likely the ConstantPool is bad, an
		 attribute got created incorrectly, or
		 perhaps the instruction streams are goofed up.
	<li> java mocha.Decompiler ac5.class<br>
	     if mocha cannot create good java source, then you really
	     need to go back and examine the calls creating the java 
	     constructs; a parameter might have been null when it should
	     have, a call to turn an expression into a statement may be
	     missing, or something else may be wrong.
	<li> mv ac5.mocha ac5.java
	<li> vi ac5.java ; you will have to fix any new SQLBoolean(1, ...)
	     calls to be new SQLBoolean(true, ...).  Also mocha 
	     occasionally messes up other stuff too.  Just iterate on it
	     until it builds or you figure out what is wrong with
	     the generated code.
	<li> javac ac5.java
	<li> javap -v -c ac5 &gt; ac5.jp
	<li> sed '1,$s/#[0-9]* &lt;/# &lt;/' ac5.gp &gt; ac5.gn
	<li> sed '1,$s/#[0-9]* &lt;/# &lt;/' ac5.jp &gt; ac5.jn<br>
	     These seds are to get rid of constant pool entry numbers,
	     which will be wildly different on the two files.
	<li> vdiff32 ac5.gn ac5.jn<br>
	     this tool shows you side-by-side diffs.  If you change
	     to the window that interleaves the diffs, you can see the 
	     length of the line.  Look for places where there are
	     invokevirtual vs. invokeinterface differences, differences
	     in the class name of a field, differences in the class name
	     of a method parameter or return type.  The generated code
	     *will* have some unavoidable differences from the
	     compiled code, such as:
	     <ul>
	     <li> it will have goto's at the end of try blocks
		  rather than return's.
	     <li> it will do a getstatic on a static final field
		  rather than inlining the static final field's value
	     <li> it will have more checkcast's in it, since it
		  doesn't see if the checkcast will always succeed
		  and thus remove it.
	     </ul>
	     Once you find a diff, you need to track down where
	     the call was generated and modify it appropriately:
	     change newMethodCall to newInterfaceMethodCall;
	     add newCastExpression to get a argument into the right
	     type for the parameter; ensure the return type given for
	     the method is its declared return type.
	</ol>

 */
public class BCJava implements CacheableFactory, JavaFactory {


	/**缓存JAVA类名与虚拟机类型名*/
	private CacheManager vmTypeIdCache =new CacheManager(this,"VMTypeIdCache",64,256);

	public BCJava() { }

	public void stop() { }

	public ClassBuilder newClassBuilder(ClassFactory cf, String packageName, int modifiers, String className, String superClass) {
		
		return new BCClass(cf, packageName, modifiers, className, superClass, this);
	}


	public Cacheable newCacheable(CacheManager cm) {
		return new VMTypeIdCacheable();
	}

	public Type type(String javaType) {

		Type retval;

		try {

			VMTypeIdCacheable vtic = (VMTypeIdCacheable) vmTypeIdCache.find(javaType);

			retval = (Type) vtic.descriptor();

			vmTypeIdCache.release(vtic);

			return retval;

		} catch (Exception se) {
			if (SanityManager.DEBUG) {
				SanityManager.THROWASSERT("Unexpected exception", se);
			}

			/*
			** If we're running a sane server, let's act as if the
			** exception didn't happen, and just get the vmTypeId the
			** slow way, without caching.
			*/
			retval = new Type(javaType, ClassHolder.convertToInternalDescriptor(javaType));
		}

		return retval;
	}

	String vmType(BCMethodDescriptor md) {
		String retval;

		try {

			VMTypeIdCacheable vtic = (VMTypeIdCacheable) vmTypeIdCache.find(md);

			retval = vtic.descriptor().toString();

			vmTypeIdCache.release(vtic);

		} catch (Exception se) {
			if (SanityManager.DEBUG) {
				SanityManager.THROWASSERT("Unexpected exception", se);
			}

			/*
			** If we're running a sane server, let's act as if the
			** exception didn't happen, and just get the vmTypeId the
			** slow way, without caching.
			*/
			retval = md.buildMethodDescriptor();
		}

		return retval;
	}
	/**
	 * Map vm types as strings to vm types as the VM
	 * handles, with int ids. Used in mapping opcodes
	 * based on type of operand/stack entry available.
	 */
	static short vmTypeId(String vmTypeS) {
		char vmTypeC = vmTypeS.charAt(0);
		switch(vmTypeC) {
			case VMDescriptor.C_CLASS : return BCExpr.vm_reference;
			case VMDescriptor.C_BYTE : return BCExpr.vm_byte;
			case VMDescriptor.C_CHAR : return BCExpr.vm_char;
			case VMDescriptor.C_DOUBLE : return BCExpr.vm_double;
			case VMDescriptor.C_FLOAT : return BCExpr.vm_float;
			case VMDescriptor.C_INT : return BCExpr.vm_int;
			case VMDescriptor.C_LONG : return BCExpr.vm_long;
			case VMDescriptor.C_SHORT : return BCExpr.vm_short;
			case VMDescriptor.C_BOOLEAN : return BCExpr.vm_int;
			case VMDescriptor.C_ARRAY : return BCExpr.vm_reference;
			case VMDescriptor.C_VOID : return BCExpr.vm_void;
			default: 
				if (SanityManager.DEBUG)
				SanityManager.THROWASSERT("No type match for "+vmTypeS);
		}
		return BCExpr.vm_void;
	}

    


}
