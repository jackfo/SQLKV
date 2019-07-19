package com.cfs.sqlkv.sql.types;

import java.io.IOException;
import java.io.ObjectOutput;

/**
 * Generates stream headers encoding the length of the stream.
 */
public interface StreamHeaderGenerator {



    /**
     * Generates the header for the specified length and writes it into the
     * provided buffer, starting at the specified offset.
     *
     * @param buf the buffer to write into
     * @param offset starting offset in the buffer
     * @param valueLength the length of the stream, can be in either bytes or
     *      characters depending on the header format
     * @return The number of bytes written into the buffer.
     */
    int generateInto(byte[] buf, int offset, long valueLength);

    /**
     * Generates the header for the specified length and writes it into the
     * destination stream.
     *
     * @param out the destination stream
     * @param valueLength the length of the stream, can be in either bytes or
     *      characters depending on the header format
     * @return The number of bytes written to the destination stream.
     * @throws IOException if writing to the destination stream fails
     */
    int generateInto(ObjectOutput out, long valueLength) throws IOException;

    /**
     * Writes a SQLKV-specific end-of-stream marker to the buffer for a stream
     * of the specified length, if required.
     *
     * @param buffer the buffer to write into
     * @param offset starting offset in the buffer
     * @param valueLength the length of the stream, can be in either bytes or
     *      characters depending on the header format
     * @return Number of bytes written (zero or more).
     */
    int writeEOF(byte[] buffer, int offset, long valueLength);

    /**
     * Writes a SQLKV-specific end-of-stream marker to the destination stream
     * for the specified length, if required.
     *
     * @param out the destination stream
     * @param valueLength the length of the stream, can be in either bytes or
     *      characters depending on the header format
     * @return Number of bytes written (zero or more).
     * @throws IOException if writing to the destination stream fails
     */
    int writeEOF(ObjectOutput out, long valueLength) throws IOException;

    /**
     * Returns the maximum length of the header.
     *
     * @return Max header length in bytes.
     */
    int getMaxHeaderLength();
}
