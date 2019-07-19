package com.cfs.sqlkv.service.io;

import java.io.DataInput;
import java.io.IOException;

/**
 * A util class for DataInput.
 */
public final class DataInputUtil {

    /**
     * Skips requested number of bytes,
     * throws EOFException if there is too few bytes in the DataInput.
     * @param in
     *      DataInput to be skipped.
     * @param skippedBytes
     *      number of bytes to skip. if skippedBytes &lt;= zero, do nothing.
     * @throws java.io.EOFException
     *      if EOF meets before requested number of bytes are skipped.
     * @throws IOException
     *      if IOException occurs. It doesn't contain EOFException.
     * @throws NullPointerException
     *      if the param 'in' equals null.
     */
    public static void skipFully(DataInput in, int skippedBytes)
    throws IOException {
        if (in == null) {
            throw new NullPointerException();
        }

        while (skippedBytes > 0) {
            int skipped = in.skipBytes(skippedBytes);
            if (skipped == 0) {
                in.readByte();
                skipped++;
            }
            skippedBytes -= skipped;
        }
    }
}
