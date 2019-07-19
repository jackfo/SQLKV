package com.cfs.sqlkv.service.bytecode;


import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.classfile.*;
import com.cfs.sqlkv.common.sanity.SanityManager;

/**
 * This class implements a Cacheable for a Byte code generator cache of
 * VMTypeIds.  It maps a Java class or type name to a VM type ID.
 */
class VMTypeIdCacheable implements Cacheable {
	/* The VM name of the Java class name */
	// either a Type (java type) or a String (method descriptor)
	private Object descriptor;

	/* This is the identity */
	private Object key;

	/* Cacheable interface */

	public void clearIdentity() {
	}

	public Object getIdentity() {
		return key;
	}

	@Override
	public Cacheable createIdentity(Object key,Object createParameters)   {
		return this;
	}

	/** @see Cacheable#setIdentity */
	public Cacheable setIdentity(Object key) {

		this.key = key;
		if (key instanceof String) {
			/* The identity is the Java class name */
			String javaName = (String) key;

			/* Get the VM type name associated with the Java class name */
			String vmName = ClassHolder.convertToInternalDescriptor(javaName);
			descriptor = new Type(javaName, vmName);
		}
		else
		{
			descriptor = ((BCMethodDescriptor) key).buildMethodDescriptor();
		}

		return this;
	}

	/** @see Cacheable#clean */
	public void clean(boolean remove) {
		/* No such thing as a dirty cache entry */
		return;
	}

	/** @see Cacheable#isDirty */
	public boolean isDirty() {
		/* No such thing as a dirty cache entry */
		return false;
	}

	/*
	** Class specific methods.
	*/

	/**
	 * Get the VM Type name (java/lang/Object) that is associated with this Cacheable
	 */

	Object descriptor() {
		return descriptor;
	}
}
