package com.cfs.sqlkv.service.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A stream that will throw an exception if its methods are invoked after it
 * has been closed.
 */
public class CloseFilterInputStream
        extends FilterInputStream {

    /** Message, modeled after CloseFilterInputStream in the client. */
    private static final String MESSAGE ="The object is already closed.";

    
    /** Tells if this stream has been closed. */
    private boolean closed;

    public CloseFilterInputStream(InputStream in) {
        super(in);
    }

    public void close() throws IOException {
        closed = true;        
        super.close();
    }

    public int available() throws IOException {
        checkIfClosed();
        return super.available();
    }

    public int read() throws IOException {
        checkIfClosed();
        return super.read();
    }

    public int read(byte[] b) throws IOException {
        checkIfClosed();
        return super.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        checkIfClosed();
        return super.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        checkIfClosed();
        return super.skip(n);
    }
    
    /** Throws exception if this stream has been closed. */
    private void checkIfClosed() throws IOException {
        if (closed) {
            throw new IOException(MESSAGE);
        }
    }
}
