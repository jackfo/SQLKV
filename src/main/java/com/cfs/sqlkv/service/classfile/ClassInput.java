package com.cfs.sqlkv.service.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;


/**	A wrapper around DataInputStream to provide input functions in terms
    of the types defined on pages 83.
 */

class ClassInput extends DataInputStream {

	ClassInput(InputStream in) {
		super(in);
	}

    int getU1() throws IOException {
        return readUnsignedByte();
    }

	int getU2() throws IOException {
		return readUnsignedShort();
	}
	int getU4() throws IOException {
		return readInt();
	}
	byte[] getU1Array(int count) throws IOException {
		byte[] b = new byte[count];
		readFully(b);
		return b;
	}
}
