package com.cfs.sqlkv.service.bytecode;

import com.cfs.sqlkv.service.classfile.*;
import com.cfs.sqlkv.service.classfile.*;

final class Type {

	static final Type LONG = new Type("long", VMDescriptor.LONG);
	static final Type INT = new Type("int", VMDescriptor.INT);
	static final Type SHORT = new Type("short", VMDescriptor.SHORT);
	static final Type BYTE = new Type("byte", VMDescriptor.BYTE);
	static final Type BOOLEAN = new Type("boolean", VMDescriptor.BOOLEAN);
	static final Type FLOAT = new Type("float", VMDescriptor.FLOAT);
	static final Type DOUBLE = new Type("double", VMDescriptor.DOUBLE);
	static final Type STRING = new Type("java.lang.String", "Ljava/lang/String;");

	private final String javaName; // e.g. java.lang.Object
	private final short vmType; // e.g. BCExpr.vm_reference
	private final String vmName; // e.g. Ljava/lang/Object;
	final String vmNameSimple; // e.g. java/lang/Object

	Type(String javaName, String vmName) {
		this.vmName = vmName;
		this.javaName = javaName;
		vmType = BCJava.vmTypeId(vmName);
		vmNameSimple = ClassHolder.convertToInternalClassName(javaName);
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(javaName);
        sb.append(", ");
        sb.append(vmType);
        sb.append(", ");
        sb.append(vmName);
        sb.append(", ");
        sb.append(vmNameSimple);
        sb.append("]");
        
        return sb.toString();
    }
	/*
	** Class specific methods.
	*/
	
	String javaName() {
		return javaName;
	}

	/**
	 * Get the VM Type name (java/lang/Object)
	 */
	String vmName() {
		return vmName;
	}
	/**
		Get the VM type (eg. VMDescriptor.INT)
	*/
	short vmType() {
		return vmType;
	}

	int width() {
		return Type.width(vmType);
	}

	static int width(short type) {
		switch (type) {
		case BCExpr.vm_void:
			return 0;
		case BCExpr.vm_long:
		case BCExpr.vm_double:
			return 2;
		default:
			return 1;
		}
	}
}
