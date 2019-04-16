package com.cfs.sqlkv.service.classfile;

import java.io.IOException;

/** Double Constant - page 97 - Section 4.4.5 */
final class CONSTANT_Double_info extends ConstantPoolEntry {
	private final double value;

	CONSTANT_Double_info(double value) {
		super(VMDescriptor.CONSTANT_Double);
		doubleSlot = true; //See page 98.
		this.value = value;
	}

	public int hashCode() {
		return (int) value;
	}

	int classFileSize() {
		// 1 (tag) + 8 (double length)
		return 1 + 8;
	}
	void put(ClassFormatOutput out) throws IOException {
		super.put(out);
		out.writeDouble(value);
	}

	public boolean equals(Object other) {

		// check it is the right type
		if (other instanceof CONSTANT_Double_info) {
		
			return value == ((CONSTANT_Double_info) other).value;
		}

		return false;
	}
}


