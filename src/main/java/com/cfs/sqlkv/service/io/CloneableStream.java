package com.cfs.sqlkv.service.io;

import java.io.InputStream;

/**
 * This is a simple interface that is used by streams that can clone themselves.
 * <p>
 * The purpose is for the implementation of BLOB/CLOB (and potentially other
 * types whose value is represented by a stream), for which their size makes it
 * impossible or very expensive to materialize the value.
 */
public interface CloneableStream {

    /**
     * Clone the stream.
     * <p>
     * To be used when a "deep" clone of a stream is required rather than
     * multiple references to the same stream.
     * <p>
     * The resulting clone should support reads, resets, closes which 
     * do not affect the original stream source of the clone.
     *
     * @return The cloned stream.
     */
    public InputStream cloneStream() ;
}
