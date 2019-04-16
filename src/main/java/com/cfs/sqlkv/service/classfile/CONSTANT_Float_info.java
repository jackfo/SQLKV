package com.cfs.sqlkv.service.classfile;

import java.io.IOException;


/** Float Constant - page 96 */
final class CONSTANT_Float_info extends ConstantPoolEntry {
	private final float value;

	CONSTANT_Float_info(float value) {
		super(VMDescriptor.CONSTANT_Float);
		this.value = value;
	}

	public int hashCode() {
		return (int) value;
	}

	public boolean equals(Object other) {

		// check it is the right type
		if (other instanceof CONSTANT_Float_info) {
		
			return value == ((CONSTANT_Float_info) other).value;
		}

		return false;
	}

	int classFileSize() {
		// 1 (tag) + 4 (float length)
		return 1 + 4;
	}

	void put(ClassFormatOutput out) throws IOException {
		super.put(out);
		out.writeFloat(value);
	}
}

