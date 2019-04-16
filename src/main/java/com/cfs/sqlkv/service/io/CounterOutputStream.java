package com.cfs.sqlkv.service.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

/**
	An OutputStream that simply provides methods to count the number
	of bytes written to an underlying stream.
*/

public class CounterOutputStream extends OutputStream implements Limit {

	protected OutputStream out;
	private int count;
	private int limit;

	/**
		Create a CounterOutputStream that will discard any bytes
		written but still coutn them and call its reset method
		so that the count is intially zero.
	*/
	public CounterOutputStream() {
        limit = -1;
	}

	public void setOutputStream(OutputStream out) {
		this.out = out;
		setLimit(-1);
	}

	/**
		Get count of bytes written to the stream since the last
		reset() call.
	*/
	public int getCount() {
		return count;
	}

	/**
		Set a limit at which an exception will be thrown. This allows callers
		to count the number of bytes up to some point, without having to complete
		the count. E.g. a caller may only want to see if some object will write out
		over 4096 bytes, without waiting for all 200,000 bytes of the object to be written.
		<BR>
		If the passed in limit is 0 or negative then the stream will count bytes without
		throwing an exception.

		@see EOFException
	*/
	public void setLimit(int limit) {

		count = 0;

		this.limit = limit;

		return;
	}

	public int clearLimit() {

		int unused = limit - count;
		limit = 0;

		return unused;
	}

	/*
	** Methods of OutputStream
	*/

	/**
		Add 1 to the count.

		@see OutputStream#write
	*/
	public  void write(int b) throws IOException {
		
		if ((limit >= 0) && ((count + 1) > limit)) {
			throw new EOFException();
		}

		if (out != null) out.write(b);
		count++;
	}

	/**
		Add len to the count, discard the data.

		@see OutputStream#write
	*/
	public void write(byte b[], int off, int len) throws IOException {

		if ((limit >= 0) && ((count + len) > limit)) {
			throw new EOFException();
		}

		if (out != null) out.write(b, off, len);
		count += len;
	}
}
