package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.TypeDescriptor;
import com.cfs.sqlkv.common.Limits;

import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.sql.types.*;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.sql.Types;

/**
 * @author zhengxiaokang
 * @Description TypeId描述SQL类型的静态信息
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 16:47
 */
public final class TypeId {
    /**
     * Various fixed numbers related to datatypes.
     */
    public static final int LONGINT_PRECISION = 19;
    public static final int LONGINT_SCALE = 0;
    public static final int LONGINT_MAXWIDTH = 8;

    public static final int INT_PRECISION = 10;
    public static final int INT_SCALE = 0;
    public static final int INT_MAXWIDTH = 4;

    public static final int SMALLINT_PRECISION = 5;
    public static final int SMALLINT_SCALE = 0;
    public static final int SMALLINT_MAXWIDTH = 2;

    public static final int TINYINT_PRECISION = 3;
    public static final int TINYINT_SCALE = 0;
    public static final int TINYINT_MAXWIDTH = 1;

    // precision in number of bits
    public static final int DOUBLE_PRECISION = 52;
    // the ResultSetMetaData needs to have the precision for numeric data
    // in decimal digits, rather than number of bits, so need a separate constant.
    public static final int DOUBLE_PRECISION_IN_DIGITS = 15;
    public static final int DOUBLE_SCALE = 0;
    public static final int DOUBLE_MAXWIDTH = 8;

    // precision in number of bits
    public static final int REAL_PRECISION = 23;
    // the ResultSetMetaData needs to have the precision for numeric data
    // in decimal digits, rather than number of bits, so need a separate constant.
    public static final int REAL_PRECISION_IN_DIGITS = 7;
    public static final int REAL_SCALE = 0;
    public static final int REAL_MAXWIDTH = 4;

    public static final int DECIMAL_PRECISION = Limits.DB2_MAX_DECIMAL_PRECISION_SCALE;
    public static final int DECIMAL_SCALE = Limits.DB2_MAX_DECIMAL_PRECISION_SCALE;
    public static final int DECIMAL_MAXWIDTH = Limits.DB2_MAX_DECIMAL_PRECISION_SCALE;

    public static final int BOOLEAN_MAXWIDTH = 1;

    public static final int CHAR_MAXWIDTH = Limits.DB2_CHAR_MAXWIDTH;
    public static final int VARCHAR_MAXWIDTH = Limits.DB2_VARCHAR_MAXWIDTH;
    public static final int LONGVARCHAR_MAXWIDTH = Limits.DB2_LONGVARCHAR_MAXWIDTH;
    public static final int BIT_MAXWIDTH = Limits.DB2_CHAR_MAXWIDTH;
    public static final int VARBIT_MAXWIDTH = Limits.DB2_VARCHAR_MAXWIDTH;
    public static final int LONGVARBIT_MAXWIDTH = Limits.DB2_LONGVARCHAR_MAXWIDTH;

    // not supposed to be limited! 4096G should be ok(?), if Derby can handle...
    public static final int BLOB_MAXWIDTH = Integer.MAX_VALUE; // to change long
    public static final int CLOB_MAXWIDTH = Integer.MAX_VALUE; // to change long
    public static final int XML_MAXWIDTH = Integer.MAX_VALUE;

    // Max width for datetime values is the length of the
    // string returned from a call to "toString()" on the
    // java.sql.Date, java.sql.Time, and java.sql.Timestamp
    // classes (the result of toString() on those classes
    // is defined by the JDBC API).  This value is also
    // used as the "precision" for those types.
    public static final int DATE_MAXWIDTH = 10;    // yyyy-mm-dd
    public static final int TIME_MAXWIDTH = 8;    // hh:mm:ss


    // Scale DOES exist for time values.  For a TIMESTAMP value,
    // it's 9 ('fffffffff'); for a TIME value, it's 0 (because there
    // are no fractional seconds).  Note that date values do
    // not have a scale.
    public static final int TIME_SCALE = 0;
    public static final int TIMESTAMP_SCALE = 9;

    /* These define all the type names for SQL92 and JDBC
     * NOTE: boolean is SQL3
     */
    //public static final String      BIT_NAME = "BIT";
    //public static final String      VARBIT_NAME = "BIT VARYING";
    //public static final String      LONGVARBIT_NAME = "LONG BIT VARYING";

    public static final String BIT_NAME = "CHAR () FOR BIT DATA";
    public static final String VARBIT_NAME = "VARCHAR () FOR BIT DATA";
    public static final String LONGVARBIT_NAME = "LONG VARCHAR FOR BIT DATA";
    public static final String TINYINT_NAME = "TINYINT";
    public static final String SMALLINT_NAME = "SMALLINT";
    public static final String INTEGER_NAME = "INTEGER";
    public static final String BIGINT_NAME = "BIGINT";
    public static final String FLOAT_NAME = "FLOAT";
    public static final String REAL_NAME = "REAL";
    public static final String DOUBLE_NAME = "DOUBLE";
    public static final String NUMERIC_NAME = "NUMERIC";
    public static final String DECIMAL_NAME = "DECIMAL";
    public static final String CHAR_NAME = "CHAR";
    public static final String VARCHAR_NAME = "VARCHAR";
    public static final String LONGVARCHAR_NAME = "LONG VARCHAR";
    public static final String DATE_NAME = "DATE";
    public static final String TIME_NAME = "TIME";
    public static final String TIMESTAMP_NAME = "TIMESTAMP";
    public static final String BINARY_NAME = "BINARY";
    public static final String VARBINARY_NAME = "VARBINARY";
    public static final String LONGVARBINARY_NAME = "LONGVARBINARY";
    public static final String BOOLEAN_NAME = "BOOLEAN";
    public static final String REF_NAME = "REF";
    public static final String REF_CURSOR = "REF CURSOR";
    public static final String NATIONAL_CHAR_NAME = "NATIONAL CHAR";
    public static final String NATIONAL_VARCHAR_NAME = "NATIONAL CHAR VARYING";
    public static final String NATIONAL_LONGVARCHAR_NAME = "LONG NVARCHAR";
    public static final String BLOB_NAME = "BLOB";
    public static final String CLOB_NAME = "CLOB";
    public static final String NCLOB_NAME = "NCLOB";

    // Following use of "XML" is per SQL/XML (2003) spec,
    // section "10.2 Type name determination".
    public static final String XML_NAME = "XML";

    // ARRAY and STRUCT are JDBC 2.0 data types that are not
    // supported by Derby.
    public static final String ARRAY_NAME = "ARRAY";
    public static final String STRUCT_NAME = "STRUCT";

    // DATALINK is a JDBC 3.0 data type. Not supported by Derby.
    public static final String DATALINK_NAME = "DATALINK";

    // ROWID and SQLXML are new types in JDBC 4.0. Not supported
    // by Derby.
    public static final String ROWID_NAME = "ROWID";
    public static final String SQLXML_NAME = "SQLXML";

    /**
     * The following constants define the type precedence hierarchy.
     */
    public static final int USER_PRECEDENCE = 1000;

    public static final int XML_PRECEDENCE = 180;
    public static final int BLOB_PRECEDENCE = 170;
    public static final int LONGVARBIT_PRECEDENCE = 160;
    public static final int VARBIT_PRECEDENCE = 150;
    public static final int BIT_PRECEDENCE = 140;
    public static final int BOOLEAN_PRECEDENCE = 130;
    public static final int TIME_PRECEDENCE = 120;
    public static final int TIMESTAMP_PRECEDENCE = 110;
    public static final int DATE_PRECEDENCE = 100;
    public static final int DOUBLE_PRECEDENCE = 90;
    public static final int REAL_PRECEDENCE = 80;
    public static final int DECIMAL_PRECEDENCE = 70;
    public static final int NUMERIC_PRECEDENCE = 69;
    public static final int LONGINT_PRECEDENCE = 60;
    public static final int INT_PRECEDENCE = 50;
    public static final int SMALLINT_PRECEDENCE = 40;
    public static final int TINYINT_PRECEDENCE = 30;
    public static final int REF_PRECEDENCE = 25;
    public static final int CLOB_PRECEDENCE = 14;
    public static final int LONGVARCHAR_PRECEDENCE = 12;
    public static final int VARCHAR_PRECEDENCE = 10;
    public static final int CHAR_PRECEDENCE = 0;

    /*
     ** Static runtime fields for typeIds
     ** These are put here because the system needs them init time.
     */
    public static final TypeId BOOLEAN_ID = create(
            StoredFormatIds.BOOLEAN_TYPE_ID,
            StoredFormatIds.BOOLEAN_TYPE_ID_IMPL);

    public static final TypeId SMALLINT_ID = create(
            StoredFormatIds.SMALLINT_TYPE_ID,
            StoredFormatIds.SMALLINT_TYPE_ID_IMPL);

    public static final TypeId INTEGER_ID = create(
            StoredFormatIds.INT_TYPE_ID,
            StoredFormatIds.INT_TYPE_ID_IMPL);

    public static final TypeId CHAR_ID = create(
            StoredFormatIds.CHAR_TYPE_ID,
            StoredFormatIds.CHAR_TYPE_ID_IMPL);

    /*
     ** Others are created on demand by the getBuiltInTypeId(int),
     ** if they are built-in (i.e.? Part of JDBC .Types),
     ** or by getBuiltInTypeId(string) if they are REF_NAME type.
     */

    private static final TypeId TINYINT_ID = create(
            StoredFormatIds.TINYINT_TYPE_ID,
            StoredFormatIds.TINYINT_TYPE_ID_IMPL);

    public static final TypeId BIGINT_ID = create(
            StoredFormatIds.BIGINT_TYPE_ID,
            StoredFormatIds.BIGINT_TYPE_ID_IMPL);

    private static final TypeId REAL_ID = create(
            StoredFormatIds.REAL_TYPE_ID,
            StoredFormatIds.REAL_TYPE_ID_IMPL);

    public static final TypeId DOUBLE_ID = create(
            StoredFormatIds.DOUBLE_TYPE_ID,
            StoredFormatIds.DOUBLE_TYPE_ID_IMPL);


    private static final TypeId VARCHAR_ID = create(
            StoredFormatIds.VARCHAR_TYPE_ID,
            StoredFormatIds.VARCHAR_TYPE_ID_IMPL);

    private static final TypeId DATE_ID = create(
            StoredFormatIds.DATE_TYPE_ID,
            StoredFormatIds.DATE_TYPE_ID_IMPL);

    private static final TypeId TIME_ID = create(
            StoredFormatIds.TIME_TYPE_ID,
            StoredFormatIds.TIME_TYPE_ID_IMPL);

    private static final TypeId TIMESTAMP_ID = create(
            StoredFormatIds.TIMESTAMP_TYPE_ID,
            StoredFormatIds.TIMESTAMP_TYPE_ID_IMPL);

    private static final TypeId BIT_ID = create(
            StoredFormatIds.BIT_TYPE_ID,
            StoredFormatIds.BIT_TYPE_ID_IMPL);

    private static final TypeId VARBIT_ID = create(
            StoredFormatIds.VARBIT_TYPE_ID,
            StoredFormatIds.VARBIT_TYPE_ID_IMPL);

    private static final TypeId REF_ID = create(
            StoredFormatIds.REF_TYPE_ID,
            StoredFormatIds.REF_TYPE_ID_IMPL);

    private static final TypeId LONGVARCHAR_ID = create(
            StoredFormatIds.LONGVARCHAR_TYPE_ID,
            StoredFormatIds.LONGVARCHAR_TYPE_ID_IMPL);

    private static final TypeId LONGVARBIT_ID = create(
            StoredFormatIds.LONGVARBIT_TYPE_ID,
            StoredFormatIds.LONGVARBIT_TYPE_ID_IMPL);

    private static final TypeId BLOB_ID = create(
            StoredFormatIds.BLOB_TYPE_ID,
            StoredFormatIds.BLOB_TYPE_ID_IMPL);

    private static final TypeId CLOB_ID = create(
            StoredFormatIds.CLOB_TYPE_ID,
            StoredFormatIds.CLOB_TYPE_ID_IMPL);

    private static final TypeId XML_ID = create(
            StoredFormatIds.XML_TYPE_ID,
            StoredFormatIds.XML_TYPE_ID_IMPL);

    private static final TypeId[] ALL_BUILTIN_TYPE_IDS =
            {
                    BOOLEAN_ID,
                    SMALLINT_ID,
                    INTEGER_ID,
                    CHAR_ID,
                    TINYINT_ID,
                    BIGINT_ID,
                    REAL_ID,
                    DOUBLE_ID,
                    VARCHAR_ID,
                    DATE_ID,
                    TIME_ID,
                    TIMESTAMP_ID,
                    BIT_ID,
                    VARBIT_ID,
                    REF_ID,
                    LONGVARCHAR_ID,
                    LONGVARBIT_ID,
                    BLOB_ID,
                    CLOB_ID,
                    XML_ID,
            };

    /*
     ** Static methods to obtain TypeIds
     */

    /**
     * Create a TypeId for the given format identifiers using
     * a BaseTypeIdImpl. Used to create the static final variables
     * of this class.
     */
    private static TypeId create(int typeFormatId, int implTypeFormatId) {
        return new TypeId(typeFormatId, new BaseTypeIdImpl(implTypeFormatId));
    }

    /**
     * Return all of the builtin type ids.
     */
    public static TypeId[] getAllBuiltinTypeIds() {
        int count = ALL_BUILTIN_TYPE_IDS.length;

        TypeId[] retval = new TypeId[count];

        for (int i = 0; i < count; i++) {
            retval[i] = ALL_BUILTIN_TYPE_IDS[i];
        }

        return retval;
    }


    /**
     * Get a TypeId of the given JDBC type.  This factory method is
     * intended to be used for built-in types.  For user-defined types,
     * we will need a factory method that takes a Java type name.
     *
     * @param JDBCTypeId The JDBC Id of the type, as listed in
     *                   java.sql.Types
     * @return The appropriate TypeId, or null if there is no such
     * TypeId.
     */

    public static TypeId getBuiltInTypeId(int JDBCTypeId) {

        switch (JDBCTypeId) {
            case Types.TINYINT:
                return TINYINT_ID;

            case Types.SMALLINT:
                return SMALLINT_ID;

            case Types.INTEGER:
                return INTEGER_ID;

            case Types.BIGINT:
                return BIGINT_ID;

            case Types.REAL:
                return REAL_ID;

            case Types.FLOAT:
            case Types.DOUBLE:
                return DOUBLE_ID;

            case Types.CHAR:
                return CHAR_ID;

            case Types.VARCHAR:
                return VARCHAR_ID;

            case Types.DATE:
                return DATE_ID;
            case Types.TIME:
                return TIME_ID;

            case Types.TIMESTAMP:
                return TIMESTAMP_ID;

            case Types.BIT:
            case Types.BOOLEAN:
                return BOOLEAN_ID;

            case Types.BINARY:
                return BIT_ID;

            case Types.VARBINARY:
                return VARBIT_ID;

            case Types.LONGVARBINARY:
                return LONGVARBIT_ID;

            case Types.LONGVARCHAR:
                return LONGVARCHAR_ID;


            case Types.BLOB:
                return BLOB_ID;

            case Types.CLOB:
                return CLOB_ID;

            case Types.SQLXML:
                return XML_ID;

            default:
                return null;
        }
    }

    public static TypeId getSQLTypeForJavaType(String javaTypeName) {
        if (javaTypeName.equals("java.lang.Boolean") ||
                javaTypeName.equals("boolean")) {
            return BOOLEAN_ID;
        } else if (javaTypeName.equals("byte[]")) {
            return VARBIT_ID;
        } else if (javaTypeName.equals("java.lang.String")) {
            return VARCHAR_ID;
        } else if (javaTypeName.equals("java.lang.Integer") ||
                javaTypeName.equals("int")) {
            return INTEGER_ID;
        } else if (javaTypeName.equals("byte")) {
            return TINYINT_ID;
        } else if (javaTypeName.equals("short")) {
            return SMALLINT_ID;
        } else if (javaTypeName.equals("java.lang.Long") ||
                javaTypeName.equals("long")) {
            return BIGINT_ID;
        } else if (javaTypeName.equals("java.lang.Float") ||
                javaTypeName.equals("float")) {
            return REAL_ID;
        } else if (javaTypeName.equals("java.lang.Double") ||
                javaTypeName.equals("double")) {
            return DOUBLE_ID;
        } else if (javaTypeName.equals("java.sql.Date")) {
            return DATE_ID;
        } else if (javaTypeName.equals("java.sql.Time")) {
            return TIME_ID;
        } else if (javaTypeName.equals("java.sql.Timestamp")) {
            return TIMESTAMP_ID;
        } else if (javaTypeName.equals("java.sql.Blob")) {
            return BLOB_ID;
        } else if (javaTypeName.equals("java.sql.Clob")) {
            return CLOB_ID;

        } else if (javaTypeName.equals("org.apache.derby.iapi.types.XML")) {
            return XML_ID;
        } else {
            /*
             ** If it's a Java primitive type, return null to indicate that
             ** there is no corresponding SQL type (all the Java primitive
             ** types that have corresponding SQL types are handled above).
             **
             ** There is only one primitive type not mentioned above, char.
             */
            if (javaTypeName.equals("char")) {
                return null;
            }

            /*
             ** It's a non-primitive type (a class) that does not correspond
             ** to a SQL built-in type, so treat it as a user-defined type.
             */
            return TypeId.getUserDefinedTypeId(javaTypeName);
        }
    }

    public static TypeId getUserDefinedTypeId(String className) {
        return new TypeId(StoredFormatIds.USERDEFINED_TYPE_ID_V3,
                null);
    }

    public static TypeId getUserDefinedTypeId(String schemaName, String unqualifiedName, String className) {
        return new TypeId
                (
                        StoredFormatIds.USERDEFINED_TYPE_ID_V3,
                        null
                );
    }

    /**
     * Given a SQL type name return the corresponding TypeId.
     *
     * @param SQLTypeName Name of SQL type
     * @return TypeId or null if there is no corresponding SQL type.
     */
    public static TypeId getBuiltInTypeId(String SQLTypeName) {

        if (SQLTypeName.equals(BOOLEAN_NAME)) {
            return BOOLEAN_ID;
        }
        if (SQLTypeName.equals(CHAR_NAME)) {
            return CHAR_ID;
        }
        if (SQLTypeName.equals(DATE_NAME)) {
            return DATE_ID;
        }
        if (SQLTypeName.equals(DOUBLE_NAME)) {
            return DOUBLE_ID;
        }
        if (SQLTypeName.equals(FLOAT_NAME)) {
            return DOUBLE_ID;
        }
        if (SQLTypeName.equals(INTEGER_NAME)) {
            return INTEGER_ID;
        }
        if (SQLTypeName.equals(BIGINT_NAME)) {
            return BIGINT_ID;
        }
        if (SQLTypeName.equals(REAL_NAME)) {
            return REAL_ID;
        }
        if (SQLTypeName.equals(SMALLINT_NAME)) {
            return SMALLINT_ID;
        }
        if (SQLTypeName.equals(TIME_NAME)) {
            return TIME_ID;
        }
        if (SQLTypeName.equals(TIMESTAMP_NAME)) {
            return TIMESTAMP_ID;
        }
        if (SQLTypeName.equals(VARCHAR_NAME)) {
            return VARCHAR_ID;
        }
        if (SQLTypeName.equals(BIT_NAME)) {
            return BIT_ID;
        }
        if (SQLTypeName.equals(VARBIT_NAME)) {
            return VARBIT_ID;
        }
        if (SQLTypeName.equals(TINYINT_NAME)) {
            return TINYINT_ID;
        }
        if (SQLTypeName.equals(LONGVARCHAR_NAME)) {
            return LONGVARCHAR_ID;
        }
        if (SQLTypeName.equals(LONGVARBIT_NAME)) {
            return LONGVARBIT_ID;
        }
        if (SQLTypeName.equals(BLOB_NAME)) {
            return BLOB_ID;
        }
        if (SQLTypeName.equals(CLOB_NAME)) {
            return CLOB_ID;
        }
        if (SQLTypeName.equals(XML_NAME)) {
            return XML_ID;
        }

        // Types defined below here are SQL types and non-JDBC types that are
        // supported by Derby
        if (SQLTypeName.equals(REF_NAME)) {
            return REF_ID;
        }
        return null;
    }

    /**
     * Get the TypeId (fundemental type information)
     * for a catalog type.
     *
     * @param catalogType
     * @return TypeId that represents the base type, null if not applicable.
     */
    public static TypeId getTypeId(TypeDescriptor catalogType) {
        TypeDescriptorImpl tdi = (TypeDescriptorImpl) catalogType;
        final int jdbcType = catalogType.getJDBCTypeId();
        TypeId typeId = TypeId.getBuiltInTypeId(jdbcType);
        if (typeId != null)
            return typeId;

        if (jdbcType == Types.JAVA_OBJECT) {
            return new TypeId(StoredFormatIds.USERDEFINED_TYPE_ID_V3, tdi.getTypeId());
        }


        return null;
    }

    /*
     * * Instance fields and methods
     */

    private BaseTypeIdImpl baseTypeId;
    private int formatId;

    /* Set in setTypeIdSpecificInstanceVariables() as needed */
    private boolean isBitTypeId;
    private boolean isLOBTypeId;
    private boolean isBooleanTypeId;
    private boolean isConcatableTypeId;
    private boolean isDecimalTypeId;
    private boolean isLongConcatableTypeId;
    private boolean isNumericTypeId;
    private boolean isRefTypeId;
    private boolean isStringTypeId;
    private boolean isFloatingPointTypeId;
    private boolean isRealTypeId;
    private boolean isDateTimeTimeStampTypeId;
    private boolean isUserDefinedTypeId;
    private int maxPrecision;
    private int maxScale;
    private int typePrecedence;
    private String javaTypeName;
    private int maxMaxWidth;

    /**
     * Constructor for a TypeId
     *
     * @param formatId   Format id of specific type id.
     * @param baseTypeId The Base type id
     */
    public TypeId(int formatId, BaseTypeIdImpl baseTypeId) {
        this.formatId = formatId;
        this.baseTypeId = baseTypeId;
        setTypeIdSpecificInstanceVariables();
    }


    private void setTypeIdSpecificInstanceVariables() {
        switch (formatId) {
            case StoredFormatIds.BIT_TYPE_ID:
                typePrecedence = BIT_PRECEDENCE;
                javaTypeName = "byte[]";
                maxMaxWidth = TypeId.BIT_MAXWIDTH;
                isBitTypeId = true;
                isConcatableTypeId = true;
                break;

            case StoredFormatIds.BOOLEAN_TYPE_ID:
                maxPrecision = TypeId.BOOLEAN_MAXWIDTH;
                typePrecedence = BOOLEAN_PRECEDENCE;
                javaTypeName = "java.lang.Boolean";
                maxMaxWidth = TypeId.BOOLEAN_MAXWIDTH;
                isBooleanTypeId = true;
                break;

            case StoredFormatIds.CHAR_TYPE_ID:
                typePrecedence = CHAR_PRECEDENCE;
                javaTypeName = "java.lang.String";
                maxMaxWidth = TypeId.CHAR_MAXWIDTH;
                isStringTypeId = true;
                isConcatableTypeId = true;
                break;

            case StoredFormatIds.DATE_TYPE_ID:
                typePrecedence = DATE_PRECEDENCE;
                javaTypeName = "java.sql.Date";
                maxMaxWidth = TypeId.DATE_MAXWIDTH;
                maxPrecision = TypeId.DATE_MAXWIDTH;
                isDateTimeTimeStampTypeId = true;
                break;

            case StoredFormatIds.DECIMAL_TYPE_ID:
                maxPrecision = TypeId.DECIMAL_PRECISION;
                maxScale = TypeId.DECIMAL_SCALE;
                typePrecedence = DECIMAL_PRECEDENCE;
                javaTypeName = "java.math.BigDecimal";
                maxMaxWidth = TypeId.DECIMAL_MAXWIDTH;
                isDecimalTypeId = true;
                isNumericTypeId = true;
                break;

            case StoredFormatIds.DOUBLE_TYPE_ID:
                maxPrecision = TypeId.DOUBLE_PRECISION;
                maxScale = TypeId.DOUBLE_SCALE;
                typePrecedence = DOUBLE_PRECEDENCE;
                javaTypeName = "java.lang.Double";
                maxMaxWidth = TypeId.DOUBLE_MAXWIDTH;
                isNumericTypeId = true;
                isFloatingPointTypeId = true;
                break;

            case StoredFormatIds.INT_TYPE_ID:
                maxPrecision = TypeId.INT_PRECISION;
                maxScale = TypeId.INT_SCALE;
                typePrecedence = INT_PRECEDENCE;
                javaTypeName = "java.lang.Integer";
                maxMaxWidth = TypeId.INT_MAXWIDTH;
                isNumericTypeId = true;
                break;

            case StoredFormatIds.BIGINT_TYPE_ID:
                maxPrecision = TypeId.LONGINT_PRECISION;
                maxScale = TypeId.LONGINT_SCALE;
                typePrecedence = LONGINT_PRECEDENCE;
                javaTypeName = "java.lang.Long";
                maxMaxWidth = TypeId.LONGINT_MAXWIDTH;
                isNumericTypeId = true;
                break;

            case StoredFormatIds.LONGVARBIT_TYPE_ID:
                typePrecedence = LONGVARBIT_PRECEDENCE;
                javaTypeName = "byte[]";
                maxMaxWidth = TypeId.LONGVARBIT_MAXWIDTH;
                isBitTypeId = true;
                isConcatableTypeId = true;
                isLongConcatableTypeId = true;
                break;

            case StoredFormatIds.LONGVARCHAR_TYPE_ID:
                typePrecedence = LONGVARCHAR_PRECEDENCE;
                javaTypeName = "java.lang.String";
                maxMaxWidth = TypeId.LONGVARCHAR_MAXWIDTH;
                isStringTypeId = true;
                isConcatableTypeId = true;
                isLongConcatableTypeId = true;
                break;

            case StoredFormatIds.REAL_TYPE_ID:
                maxPrecision = TypeId.REAL_PRECISION;
                maxScale = TypeId.REAL_SCALE;
                typePrecedence = REAL_PRECEDENCE;
                javaTypeName = "java.lang.Float";
                maxMaxWidth = TypeId.REAL_MAXWIDTH;
                isNumericTypeId = true;
                isRealTypeId = true;
                isFloatingPointTypeId = true;
                break;

            case StoredFormatIds.REF_TYPE_ID:
                typePrecedence = REF_PRECEDENCE;
                javaTypeName = "java.sql.Ref";
                isRefTypeId = true;
                break;

            case StoredFormatIds.SMALLINT_TYPE_ID:
                maxPrecision = TypeId.SMALLINT_PRECISION;
                maxScale = TypeId.SMALLINT_SCALE;
                typePrecedence = SMALLINT_PRECEDENCE;
                javaTypeName = "java.lang.Integer";
                maxMaxWidth = TypeId.SMALLINT_MAXWIDTH;
                isNumericTypeId = true;
                break;

            case StoredFormatIds.TIME_TYPE_ID:
                typePrecedence = TIME_PRECEDENCE;
                javaTypeName = "java.sql.Time";
                maxScale = TypeId.TIME_SCALE;
                maxMaxWidth = TypeId.TIME_MAXWIDTH;
                maxPrecision = TypeId.TIME_MAXWIDTH;
                isDateTimeTimeStampTypeId = true;
                break;


            case StoredFormatIds.TINYINT_TYPE_ID:
                maxPrecision = TypeId.TINYINT_PRECISION;
                maxScale = TypeId.TINYINT_SCALE;
                typePrecedence = TINYINT_PRECEDENCE;
                javaTypeName = "java.lang.Integer";
                maxMaxWidth = TypeId.TINYINT_MAXWIDTH;
                isNumericTypeId = true;
                break;

            case StoredFormatIds.VARBIT_TYPE_ID:
                typePrecedence = VARBIT_PRECEDENCE;
                javaTypeName = "byte[]";
                maxMaxWidth = TypeId.VARBIT_MAXWIDTH;
                isBitTypeId = true;
                isConcatableTypeId = true;
                break;

            case StoredFormatIds.BLOB_TYPE_ID:
                typePrecedence = BLOB_PRECEDENCE;
                javaTypeName = "java.sql.Blob";
                maxMaxWidth = TypeId.BLOB_MAXWIDTH;
                isBitTypeId = true;
                isConcatableTypeId = true;
                isLongConcatableTypeId = true; // ??
                isLOBTypeId = true;
                break;

            case StoredFormatIds.VARCHAR_TYPE_ID:
                typePrecedence = VARCHAR_PRECEDENCE;
                javaTypeName = "java.lang.String";
                maxMaxWidth = TypeId.VARCHAR_MAXWIDTH;
                isStringTypeId = true;
                isConcatableTypeId = true;
                break;

            case StoredFormatIds.CLOB_TYPE_ID:
                typePrecedence = CLOB_PRECEDENCE;
                javaTypeName = "java.sql.Clob";
                maxMaxWidth = TypeId.CLOB_MAXWIDTH;
                isStringTypeId = true;
                isConcatableTypeId = true;
                isLongConcatableTypeId = true; // ??
                isLOBTypeId = true;
                break;

            case StoredFormatIds.XML_TYPE_ID:

                typePrecedence = XML_PRECEDENCE;
                javaTypeName = "org.apache.derby.iapi.types.XML";
                maxMaxWidth = TypeId.XML_MAXWIDTH;

                // We set this to true in order to disallow use
                // of the XML datatype for procedure/function args.
                isLongConcatableTypeId = true;
                break;

        }

    }

    public BaseTypeIdImpl getBaseTypeId() {
        return baseTypeId;
    }

    public DataValueDescriptor getNull() {
        switch (formatId) {
            case StoredFormatIds.BIT_TYPE_ID:
                return new SQLBit();

            case StoredFormatIds.BOOLEAN_TYPE_ID:
                return new SQLBoolean();

            case StoredFormatIds.CHAR_TYPE_ID:
                return new SQLChar();

            case StoredFormatIds.DECIMAL_TYPE_ID:
                return new SQLDecimal();

            case StoredFormatIds.DOUBLE_TYPE_ID:
                return new SQLDouble();

            case StoredFormatIds.INT_TYPE_ID:
                return new SQLInteger();

            case StoredFormatIds.BIGINT_TYPE_ID:
                return new SQLLongint();

            case StoredFormatIds.LONGVARBIT_TYPE_ID:
                return new SQLLongVarbit();

            case StoredFormatIds.BLOB_TYPE_ID:
                return new SQLBlob();

            case StoredFormatIds.CLOB_TYPE_ID:
                return new SQLClob();

            case StoredFormatIds.LONGVARCHAR_TYPE_ID:
                return new SQLLongvarchar();

            case StoredFormatIds.REAL_TYPE_ID:
                return new SQLReal();

            case StoredFormatIds.REF_TYPE_ID:
                return new SQLRef();

            case StoredFormatIds.SMALLINT_TYPE_ID:
                return new SQLSmallint();

            case StoredFormatIds.TINYINT_TYPE_ID:
                return new SQLTinyint();

            case StoredFormatIds.DATE_TYPE_ID:
                return new SQLDate();

            case StoredFormatIds.TIME_TYPE_ID:
                return new SQLTime();

            case StoredFormatIds.TIMESTAMP_TYPE_ID:
                return new SQLTimestamp();

            case StoredFormatIds.USERDEFINED_TYPE_ID_V3:
                return new UserType();

            case StoredFormatIds.VARBIT_TYPE_ID:
                return new SQLVarbit();

            case StoredFormatIds.VARCHAR_TYPE_ID:
                return new SQLVarchar();

            case StoredFormatIds.XML_TYPE_ID:
                return new XML();

            default:
                return null;
        }
    }

    public boolean isStringTypeId() {
        return isStringTypeId;
    }

    /**
     * 返回数字最大精度
     */
    public int getMaximumPrecision() {
        return maxPrecision;
    }

    /**
     * 返回当前类型最大可读
     */
    public int getMaximumScale() {
        return maxScale;
    }

    /**
     * 返回当前类型最大宽度
     */
    public int getMaximumMaximumWidth() {
        return maxMaxWidth;
    }

    public final int getJDBCTypeId() {
        return baseTypeId.getJDBCTypeId();
    }

    public int getTypeFormatId() {
        return formatId;
    }

    public boolean isBooleanTypeId() {
        return isBooleanTypeId;
    }

    public boolean isRefTypeId() {
        return isRefTypeId;
    }
}

