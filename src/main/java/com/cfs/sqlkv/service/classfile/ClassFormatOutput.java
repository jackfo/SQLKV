package com.cfs.sqlkv.service.classfile;

import com.cfs.sqlkv.service.io.AccessibleByteArrayOutputStream;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/** A wrapper around DataOutputStream to provide input functions in terms
    of the types defined on pages 83 of the Java Virtual Machine spec.

	For this types use these methods of DataOutputStream
	<UL>
	<LI>float - writeFloat
	<LI>long - writeLong
	<LI>double - writeDouble
	<LI>UTF/String - writeUTF
	<LI>U1Array - write(byte[])
	</UL>
 */

public final class ClassFormatOutput extends DataOutputStream {

	public ClassFormatOutput() {
		this(512);
	}

	public ClassFormatOutput(int size) {
		this(new AccessibleByteArrayOutputStream(size));
	}
	public ClassFormatOutput(OutputStream stream) {
		super(stream);
	}
	public void putU1(int i) throws IOException {
		// ensure the format of the class file is not
		// corrupted by writing an incorrect, truncated value.
		if (i > 255)
			ClassFormatOutput.limit("U1", 255, i);
		write(i);
	}
	public void putU2(int i) throws IOException {
		putU2("U2", i);

	}
	public void putU2(String limit, int i) throws IOException {
		
		// ensure the format of the class file is not
		// corrupted by writing an incorrect, truncated value.
		if (i > 65535)
			ClassFormatOutput.limit(limit, 65535, i);
		write(i >> 8);
		write(i);
	}
	public void putU4(int i) throws IOException {
		writeInt(i);
	}

	public void writeTo(OutputStream outTo) throws IOException {
		((AccessibleByteArrayOutputStream) out).writeTo(outTo);
	}

	/**
		Get a reference to the data array the class data is being built
		in. No copy is made.
	*/
	public byte[] getData() {
		return ((AccessibleByteArrayOutputStream) out).getInternalByteArray();
	}

	/**
	 * Throw an ClassFormatError if a limit of the Java class file format is reached.
	 * @param name Terse limit description from JVM spec.
	 * @param limit What the limit is.
	 * @param value What the value for the current class is
	 * @throws IOException Thrown when limit is exceeded.
	 */
	static void limit(String name, int limit, int value)
		throws IOException
	{
		throw new IOException(name + "(" + value + " > " + limit + ")");
	}
}
