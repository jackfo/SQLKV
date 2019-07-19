package com.cfs.sqlkv.sql.types;


import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.io.ObjectOutput;



/**
 * Generates stream headers for Clob data values.
 * <p>
 * <em>THREAD SAFETY NOTE</em>: This class is considered thread safe, even
 * though it strictly speaking isn't. However, with the assumption that an
 * instance of this class cannot be shared across databases with different
 * versions, the only bad thing that can happen is that the mode is obtained
 * several times.
 */
//@ThreadSafe
public final class ClobStreamHeaderGenerator
        implements StreamHeaderGenerator {

    /**
     * Magic byte for the 10.5 stream header format.
     */
    private static final byte MAGIC_BYTE = (byte) 0xF0;

    /**
     * Bytes for a 10.5 unknown length header.
     */
    private static final byte[] UNKNOWN_LENGTH = new byte[]{
            0x00, 0x00, MAGIC_BYTE, 0x00, 0x00};

    /**
     * Header generator for the pre 10.5 header format. This format is used
     * for Clobs as well if the database version is pre 10.5.
     */
    private static final CharStreamHeaderGenerator CHARHDRGEN =
            new CharStreamHeaderGenerator();

    /**
     * Reference to "owning" DVD, used to update it with information about
     * which header format should be used. This is currently only determined by
     * consulting the data dictionary about the version.
     * <p>
     * This is an optimization to avoid having to consult the data dictionary
     * on every request to generate a header when a data value descriptor is
     * reused.
     */
    private final StringDataValue callbackDVD;
    /**
     * {@code true} if the database version is prior to 10.5, {@code false} if
     * the version is 10.5 or newer. If {@code null}, the version will be
     * determined by obtaining the database context through the context service.
     */
    private Boolean isPreSQLKVTenFive;

    /**
     * Creates a new generator that will use the context manager to determine
     * which header format to use based on the database version.
     *
     * @param dvd the owning data value descriptor
     */
    public ClobStreamHeaderGenerator(StringDataValue dvd) {
        if (dvd == null) {
            throw new IllegalStateException("dvd cannot be null");
        }
        this.callbackDVD = dvd;
    }


    public ClobStreamHeaderGenerator() {

        this.callbackDVD = null;

    }




    /**
     * Generates the header for the specified length and writes it into the
     * provided buffer, starting at the specified offset.
     *
     * @param buf         the buffer to write into
     * @param offset      starting offset in the buffer
     * @param valueLength the length to encode in the header
     * @return The number of bytes written into the buffer.
     */
    public int generateInto(byte[] buf, int offset, long valueLength) {
        int headerLength = 0;

        if (valueLength >= 0) {
            // Encode the character count in the header.
            buf[offset + headerLength++] = (byte) (valueLength >>> 24);
            buf[offset + headerLength++] = (byte) (valueLength >>> 16);
            buf[offset + headerLength++] = MAGIC_BYTE;
            buf[offset + headerLength++] = (byte) (valueLength >>> 8);
            buf[offset + headerLength++] = (byte) (valueLength >>> 0);
        } else {
            // Write an "unknown length" marker.
            headerLength = UNKNOWN_LENGTH.length;
            System.arraycopy(UNKNOWN_LENGTH, 0, buf, offset, headerLength);
        }

        return headerLength;
    }

    /**
     * Generates the header for the specified length.
     *
     * @param out         the destination stream
     * @param valueLength the length to encode in the header
     * @return The number of bytes written to the destination stream.
     * @throws IOException if writing to the destination stream fails
     */
    public int generateInto(ObjectOutput out, long valueLength) throws IOException {
        int headerLength = 0;
        headerLength = 5;
        // Assume the length specified is a char count.
        if (valueLength > 0) {
            // Encode the character count in the header.
            out.writeByte((byte) (valueLength >>> 24));
            out.writeByte((byte) (valueLength >>> 16));
            out.writeByte(MAGIC_BYTE);
            out.writeByte((byte) (valueLength >>> 8));
            out.writeByte((byte) (valueLength >>> 0));
        } else {
            // Write an "unknown length" marker.
            out.write(UNKNOWN_LENGTH);
        }

        return headerLength;
    }

    /**
     * Writes a SQLKV-specific end-of-stream marker to the buffer for a stream
     * of the specified character length, if required.
     *
     * @param buffer      the buffer to write into
     * @param offset      starting offset in the buffer
     * @param valueLength the length of the stream
     * @return Number of bytes written (zero or more).
     */
    public int writeEOF(byte[] buffer, int offset, long valueLength) {
        if (valueLength < 0) {
            return CharStreamHeaderGenerator.writeEOFMarker(buffer, offset);
        } else {
            return 0;
        }
    }

    /**
     * Writes a SQLKV-specific end-of-stream marker to the destination stream
     * for the specified character length, if required.
     *
     * @param out         the destination stream
     * @param valueLength the length of the stream
     * @return Number of bytes written (zero or more).
     */
    public int writeEOF(ObjectOutput out, long valueLength) throws IOException {
        if (valueLength < 0) {
            return CharStreamHeaderGenerator.writeEOFMarker(out);
        } else {
            return 0;
        }
    }

    /**
     * Returns the maximum header length.
     *
     * @return Maximum header length in bytes.
     */
    public int getMaxHeaderLength() {
        return 5;
    }

    /**
     * Determines which header format to use.
     * <p>
     * <em>Implementation note:</em> The header format is determined by
     * consulting the data dictionary throught the context service. If there is
     * no context, the operation will fail.
     *
     * @throws IllegalStateException if there is no context
     */
}
