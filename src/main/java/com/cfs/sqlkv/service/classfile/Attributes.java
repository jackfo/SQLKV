package com.cfs.sqlkv.service.classfile;

import java.io.IOException;
import java.util.Vector;

class Attributes extends Vector<AttributeEntry> {
	private int classFileSize;

	Attributes(int count) {
		super(count);
	}

	void put(ClassFormatOutput out) throws IOException {
		int size = size();
		for (int i = 0; i < size; i++) {
			elementAt(i).put(out);
		}
	}

	int classFileSize() {
		return classFileSize;
	}

	/**
	*/

	void addEntry(AttributeEntry item) {
		addElement(item);
		classFileSize += item.classFileSize();
	}
}

