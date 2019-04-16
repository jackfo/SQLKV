package com.cfs.sqlkv.service.classfile;

import java.io.IOException;

/** Integer Constant - page 96 */
class CONSTANT_Integer_info extends ConstantPoolEntry {
	private final int value;

	CONSTANT_Integer_info(int value) {
		super(VMDescriptor.CONSTANT_Integer);
		this.value = value;
	}

	public int hashCode() {
		return value;
	}

	void put(ClassFormatOutput out) throws IOException {
		super.put(out);
		out.putU4(value);
	}

	public boolean equals(Object other) {

		// check it is the right type
		if (other instanceof CONSTANT_Integer_info) {
		
			return value == ((CONSTANT_Integer_info) other).value;
		}

		return false;
	}

	int classFileSize() {
		// 1 (tag) + 4 (int length)
		return 1 + 4;
	}
}

