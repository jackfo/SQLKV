package com.cfs.sqlkv.service.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This allows us to get to the byte array to go back and
 * edit contents or get the array without having a copy made.
 <P>
   Since a copy is not made, users must be careful that no more
   writes are made to the stream if the array reference is handed off.
 * <p>
 * Users of this must make the modifications *before* the
 * next write is done, and then release their hold on the
 * array.
   
 */
public class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {

	public AccessibleByteArrayOutputStream() {
		super();
	}

	public AccessibleByteArrayOutputStream(int size) {
		super(size);
	}

	/**
	 * The caller promises to set their variable to null
	 * before any other calls to write to this stream are made.
	   Or promises to throw away references to the stream before
	   passing the array reference out of its control.
	 */
	public byte[] getInternalByteArray() {
		return buf;
	}
    
    /**
     * Read the complete contents of the passed input stream
     * into this byte array.
     * @throws IOException 
     */
    public void readFrom(InputStream in) throws IOException
    {
       byte[] buffer = new byte[8192];
        
        for(;;)
        {
            int read = in.read(buffer, 0, buf.length);
            if (read == -1)
                break;
            write(buffer, 0, read);
        }
    }
    
    /**
     * Return an InputStream that wraps the valid byte array.
     * Note that no copy is made of the byte array from the
     * input stream, it is up to the caller to ensure the correct
     * co-ordination.
     */
    public InputStream getInputStream()
    {
        return new ByteArrayInputStream(buf, 0, count);
    }
    
    /**
     * Copy an InputStream into an array of bytes and return
     * an InputStream against those bytes. The input stream
     * is copied until EOF is returned. This is useful to provide
     * streams to applications in order to isolate them from
     * SQLKV's internals.
     * 
     * @param in InputStream to be copied
     * @param bufferSize Initial size of the byte array
     * 
     * @return InputStream against the raw data.
     * 
     * @throws IOException Error reading the stream
     */
    public static InputStream copyStream(InputStream in, int bufferSize)
         throws IOException
    {
        AccessibleByteArrayOutputStream raw =
            new AccessibleByteArrayOutputStream(bufferSize);
        raw.readFrom(in);
        return raw.getInputStream();
    }
}
