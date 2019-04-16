package com.cfs.sqlkv.service.classfile;
import java.io.IOException;

/** Constant Pool class - pages 92-99 */
public abstract class ConstantPoolEntry /*implements PoolEntry*/ {
	
	protected int tag;
	protected boolean doubleSlot;

	protected int index;

	protected ConstantPoolEntry(int tag) {
		this.tag = tag;
	}

	int getIndex() {
		return index;
	}

	void setIndex(int index) {
		this.index = index;
	}

	boolean doubleSlot() {
		return doubleSlot;
	}

	/**
		Return the key used to key this object in a hashtable
	*/
	Object getKey() {
		return this;
	}

	/**
		Return an estimate of the size of the constant pool entry.
	*/
	abstract int classFileSize();

	void put(ClassFormatOutput out) throws IOException {
		out.putU1(tag);
	}

	/*
	** Public API methods
	*/

	/**
		Return the tag or type of the entry. Will be equal to one of the
		constants above, e.g. CONSTANT_Class.
	*/
	final int getTag() {
		return tag;
	}

	/**	
		Get the first index in a index type pool entry.
		This call is valid when getTag() returns one of
		<UL> 
		<LI> CONSTANT_Class
		<LI> CONSTANT_Fieldref
		<LI> CONSTANT_Methodref
		<LI> CONSTANT_InterfaceMethodref
		<LI> CONSTANT_String
		<LI> CONSTANT_NameAndType
		</UL>
	*/
	int getI1() { return 0; }

	/**	
		Get the second index in a index type pool entry.
		This call is valid when getTag() returns one of
		<UL> 
		<LI> CONSTANT_Fieldref
		<LI> CONSTANT_Methodref
		<LI> CONSTANT_InterfaceMethodref
		<LI> CONSTANT_NameAndType
		</UL>
	*/	
	int getI2() { return 0; };
}

