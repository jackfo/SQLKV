package com.cfs.sqlkv.service.io;

import java.io.OutputStream;

/**
	An OutputStream that simply discards all data written to it.
*/

public final class NullOutputStream extends OutputStream {

	/*
	** Methods of OutputStream
	*/

	/**
		Discard the data.

		@see OutputStream#write
	*/
	public  void write(int b)  {
	}

	/**
		Discard the data.

		@see OutputStream#write
	*/
	public void write(byte b[]) {
	}

	/**
		Discard the data.

		@see OutputStream#write
	*/
	public void write(byte b[], int off, int len)  {
	}
}
