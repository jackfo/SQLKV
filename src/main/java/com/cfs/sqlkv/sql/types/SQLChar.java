package com.cfs.sqlkv.sql.types;

import com.cfs.sqlkv.catalog.types.TypeId;

import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.FormatIdInputStream;
import com.cfs.sqlkv.service.io.InputStreamUtil;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:50
 */
public class SQLChar extends DataType implements StringDataValue {

    private String value;

    protected final static int RETURN_SPACE_THRESHOLD = 4096;

    /**
     * 数据在磁盘是一种流格式
     */
    InputStream stream;
    /**
     * 数据是一个大数据类型
     */
    protected Clob _clobValue;

    public SQLChar() {
    }

    public SQLChar(String val) {
        value = val;
    }

    private char[] rawData;
    private int rawLength = -1;

    @Override
    public boolean isNull() {
        return ((value == null) && (rawLength == -1) && (stream == null) && (_clobValue == null));
    }

    @Override
    public void restoreToNull() {
        value = null;
        _clobValue = null;
        stream = null;
        rawLength = -1;
    }

    @Override
    public String getString()   {
        if (value == null) {
            int len = rawLength;
            if (len != -1) {
                value = new String(rawData, 0, len);
                if (len > RETURN_SPACE_THRESHOLD) {
                    // free up this char[] array to reduce memory usage
                    rawData = null;
                    rawLength = -1;
                }
            } else if (_clobValue != null) {
                try {
                    value = _clobValue.getSubString(1L, getClobLength());
                    _clobValue = null;
                } catch (SQLException se) {

                }

            } else if (stream != null) {
                try {
                    if (stream instanceof FormatIdInputStream) {
                        readExternal((FormatIdInputStream) stream);
                    } else {
                        readExternal(new FormatIdInputStream(stream));
                    }
                    stream = null;
                    return getString();
                } catch (IOException ioe) {
                }
            }
        }
        return value;
    }

    @Override
    public DataValueDescriptor getNewNull() {
        return new SQLChar();
    }


    @Override
    public Object getObject()   {
        return getString();
    }

    @Override
    public int compare(DataValueDescriptor other)   {
        if (typePrecedence() < other.typePrecedence()) {
            return -(other.compare(this));
        }
        return stringCompare(this, (SQLChar) other);
    }

    protected int stringCompare(SQLChar char1, SQLChar char2)
              {
        return stringCompare(char1.getCharArray(), char1.getLength(),
                char2.getCharArray(), char2.getLength());
    }

    public char[] getCharArray()   {
        if (isNull()) {
            return null;
        } else if (rawLength != -1) {
            return rawData;
        } else {
            getString();
            rawData = value.toCharArray();
            rawLength = rawData.length;
            return rawData;
        }
    }

    public int getLength()   {
        if (_clobValue != null) {
            return getClobLength();
        }
        if (rawLength != -1)
            return rawLength;
        String tmpString = getString();
        if (tmpString == null) {
            return 0;
        } else {
            int clobLength = tmpString.length();
            return clobLength;
        }
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.SQL_CHAR_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (_clobValue != null) {
            throw new RuntimeException("暂未实现大数据段");
        }
        String lvalue = null;
        char[] data = null;
        int strlen = rawLength;
        boolean isRaw;
        if (strlen < 0) {
            lvalue = value;
            strlen = lvalue.length();
            isRaw = false;
        } else {
            data = rawData;
            isRaw = true;
        }
        int utflen = strlen;
        for (int i = 0; (i < strlen) && (utflen <= 65535); i++) {
            int c = isRaw ? data[i] : lvalue.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                // 1 byte for character
            } else if (c > 0x07FF) {
                utflen += 2; // 3 bytes for character
            } else {
                utflen += 1; // 2 bytes for character
            }
        }
        StreamHeaderGenerator header = getStreamHeaderGenerator();
        // Generate the header, write it to the destination stream, write the
        // user data and finally write an EOF-marker is required.
        header.generateInto(out, utflen);
        writeUTF(out, strlen, isRaw, null);
        header.writeEOF(out, utflen);

    }

    private final void writeUTF(ObjectOutput out, int strLen, final boolean isRaw, Reader characterReader) throws IOException {
        final char[] data = isRaw ? rawData : null;
        final String lvalue = isRaw ? null : value;
        for (int i = 0; i < strLen; i++) {
            int c;
            if (characterReader != null) {
                c = characterReader.read();
            } else {
                c = isRaw ? data[i] : lvalue.charAt(i);
            }

            writeUTF(out, c);
        }
    }

    private static void writeUTF(ObjectOutput out, int c) throws IOException {
        if ((c >= 0x0001) && (c <= 0x007F)) {
            out.write(c);
        } else if (c > 0x07FF) {
            out.write(0xE0 | ((c >> 12) & 0x0F));
            out.write(0x80 | ((c >> 6) & 0x3F));
            out.write(0x80 | ((c >> 0) & 0x3F));
        } else {
            out.write(0xC0 | ((c >> 6) & 0x1F));
            out.write(0x80 | ((c >> 0) & 0x3F));
        }
    }


    protected static final StreamHeaderGenerator CHAR_HEADER_GENERATOR = new CharStreamHeaderGenerator();

    public StreamHeaderGenerator getStreamHeaderGenerator() {
        return CHAR_HEADER_GENERATOR;
    }

    private final static int GROWBY_FOR_CHAR = 64;

    protected int growBy() {
        return GROWBY_FOR_CHAR;  //seems reasonable for a char
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int utflen = in.readUnsignedShort();
        readExternal(in, utflen, 0);
    }

    protected void readExternal(ObjectInput in, int utflen,
                                final int knownStrLen)
            throws IOException {
        int requiredLength;
        // minimum amount that is reasonable to grow the array
        // when we know the array needs to growby at least one
        // byte but we dont want to grow by one byte as that
        // is not performant
        int minGrowBy = growBy();
        if (utflen != 0) {
            // the object was not stored as a streaming column
            // we know exactly how long it is
            requiredLength = utflen;
        } else {
            // the object was stored as a streaming column
            // and we have a clue how much we can read unblocked
            // OR
            // The original string was a 0 length string.
            requiredLength = in.available();
            if (requiredLength < minGrowBy)
                requiredLength = minGrowBy;
        }

        char str[];
        if ((rawData == null) || (requiredLength > rawData.length)) {

            str = new char[requiredLength];
        } else {
            str = rawData;
        }
        int arrayLength = str.length;

        // Set these to null to allow GC of the array if required.
        rawData = null;
        resetForMaterialization();
        int count = 0;
        int strlen = 0;

        readingLoop:
        while (((strlen < knownStrLen) || (knownStrLen == 0)) &&
                ((count < utflen) || (utflen == 0))) {
            int c;

            try {

                c = in.readUnsignedByte();
            } catch (EOFException eof) {
                if (utflen != 0)
                    throw new EOFException();

                break readingLoop;
            }


            if (strlen >= arrayLength) // the char array needs to be grown
            {
                int growby = in.available();
                if (growby < minGrowBy)
                    growby = minGrowBy;

                int newstrlength = arrayLength + growby;
                char oldstr[] = str;
                str = new char[newstrlength];
                System.arraycopy(oldstr, 0, str, 0, arrayLength);
                arrayLength = newstrlength;
            }

            int char2, char3;
            char actualChar;
            if ((c & 0x80) == 0x00) {
                // one byte character
                count++;
                actualChar = (char) c;
            } else if ((c & 0x60) == 0x40) // we know the top bit is set here
            {
                // two byte character
                count += 2;
                if (utflen != 0 && count > utflen)
                    throw new UTFDataFormatException();
                char2 = in.readUnsignedByte();
                if ((char2 & 0xC0) != 0x80)
                    throw new UTFDataFormatException();
                actualChar = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
            } else if ((c & 0x70) == 0x60) // we know the top bit is set here
            {
                // three byte character
                count += 3;
                if (utflen != 0 && count > utflen)
                    throw new UTFDataFormatException();
                char2 = in.readUnsignedByte();
                char3 = in.readUnsignedByte();
                if ((c == 0xE0) && (char2 == 0) && (char3 == 0)
                        && (utflen == 0)) {
                    // we reached the end of a long string,
                    // that was terminated with
                    // (11100000, 00000000, 00000000)
                    break readingLoop;
                }

                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                    throw new UTFDataFormatException();


                actualChar = (char) (((c & 0x0F) << 12) |
                        ((char2 & 0x3F) << 6) |
                        ((char3 & 0x3F) << 0));
            } else {
                throw new UTFDataFormatException(
                        "Invalid code point: " + Integer.toHexString(c));
            }

            str[strlen++] = actualChar;
        }


        rawData = str;
        rawLength = strlen;
    }

    @Override
    public void readExternalFromArray(ArrayInputStream in) throws IOException {
        resetForMaterialization();
        int utfLen = (((in.read() & 0xFF) << 8) | (in.read() & 0xFF));
        if (rawData == null || rawData.length < utfLen) {
            rawData = new char[utfLen];
        }
        arg_passer[0] = rawData;

        rawLength = in.readSQLKVUTF(arg_passer, utfLen);
        rawData = arg_passer[0];
    }

    char[][] arg_passer = new char[1][];

    private int getClobLength()   {
        try {
            return rawGetClobLength();
        } catch (SQLException se) {
            throw new RuntimeException(se.getMessage());
        }
    }


    private void resetForMaterialization() {
        value = null;
        stream = null;
    }

    private int rawGetClobLength() throws SQLException {
        long length = _clobValue.length();
        if (length > Integer.MAX_VALUE) {
            throw new SQLException("length > Integer.MAX_VALUE");
        }
        return (int) length;
    }

    public int typePrecedence() {
        return TypeId.CHAR_PRECEDENCE;
    }

    protected static int stringCompare(char[] op1, int leftlen, char[] op2, int rightlen) {
        int posn;
        char leftchar;
        char rightchar;
        int retvalIfLTSpace;
        char[] remainingString;
        int remainingLen;
        if (op1 == null || op2 == null) {
            if (op1 != null)    // op2 == null
                return -1;
            if (op2 != null)    // op1 == null
                return 1;
            return 0;           // both null
        }
        int shorterLen = leftlen < rightlen ? leftlen : rightlen;
        for (posn = 0; posn < shorterLen; posn++) {
            leftchar = op1[posn];
            rightchar = op2[posn];
            if (leftchar != rightchar) {
                if (leftchar < rightchar)
                    return -1;
                else
                    return 1;
            }
        }
        if (leftlen == rightlen)
            return 0;
        if (leftlen > rightlen) {
            retvalIfLTSpace = -1;
            remainingString = op1;
            posn = rightlen;
            remainingLen = leftlen;
        } else {
            retvalIfLTSpace = 1;
            remainingString = op2;
            posn = leftlen;
            remainingLen = rightlen;
        }
        for (; posn < remainingLen; posn++) {
            char remainingChar;
            remainingChar = remainingString[posn];
            if (remainingChar < ' ')
                return retvalIfLTSpace;
            else if (remainingChar > ' ')
                return -retvalIfLTSpace;
        }

        return 0;
    }

    protected final void hasNonBlankChars(String source, int start, int end) {
        for (int posn = start; posn < end; posn++) {
            if (source.charAt(posn) != ' ') {
                runtimeException("..");
            }
        }
    }

    public void setValue(String theValue) {
        stream = null;
        rawLength = -1;
        _clobValue = null;

        value = theValue;
    }

    protected void setFrom(DataValueDescriptor theValue)   {
        if (theValue instanceof SQLChar) {
            SQLChar that = (SQLChar) theValue;

            if (that._clobValue != null) {
                setValue(that._clobValue);
                return;
            }
        }
        setValue(theValue.getString());
    }

    public void setValue(Clob theValue) {
        stream = null;
        rawLength = -1;
        value = null;
        _clobValue = theValue;
    }



}
