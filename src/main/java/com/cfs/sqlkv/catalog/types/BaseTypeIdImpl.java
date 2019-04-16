package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.TypeDescriptor;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.util.IdUtil;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Types;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 16:21
 */
public class BaseTypeIdImpl implements Formatable {

    private int formatId;

    String unqualifiedName;

    /**
     * jdbc的id类型
     */
    transient int JDBCTypeId;

    public BaseTypeIdImpl() {
    }

    /**
     * 根据formatId创建相应的BaseTypeIdImpl实例
     */
    public BaseTypeIdImpl(int formatId) {
        this.formatId = formatId;
        setTypeIdSpecificInstanceVariables();
    }

    /**
     * 根据formatId设置unqualifiedName和JDBCTypeId
     */
    private void setTypeIdSpecificInstanceVariables() {
        switch (getTypeFormatId()) {
            case StoredFormatIds.BOOLEAN_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.BOOLEAN_NAME;
                JDBCTypeId = Types.BOOLEAN;
                break;

            case StoredFormatIds.INT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.INTEGER_NAME;
                JDBCTypeId = Types.INTEGER;
                break;

            case StoredFormatIds.SMALLINT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.SMALLINT_NAME;
                JDBCTypeId = Types.SMALLINT;
                break;

            case StoredFormatIds.TINYINT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.TINYINT_NAME;
                JDBCTypeId = Types.TINYINT;
                break;

            case StoredFormatIds.BIGINT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.BIGINT_NAME;
                JDBCTypeId = Types.BIGINT;
                break;

            case StoredFormatIds.DECIMAL_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.DECIMAL_NAME;
                JDBCTypeId = Types.DECIMAL;
                break;

            case StoredFormatIds.DOUBLE_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.DOUBLE_NAME;
                JDBCTypeId = Types.DOUBLE;
                break;

            case StoredFormatIds.REAL_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.REAL_NAME;
                JDBCTypeId = Types.REAL;
                break;

            case StoredFormatIds.REF_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.REF_NAME;
                JDBCTypeId = Types.OTHER;
                break;

            case StoredFormatIds.CHAR_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.CHAR_NAME;
                JDBCTypeId = Types.CHAR;
                break;

            case StoredFormatIds.VARCHAR_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.VARCHAR_NAME;
                JDBCTypeId = Types.VARCHAR;
                break;

            case StoredFormatIds.LONGVARCHAR_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.LONGVARCHAR_NAME;
                JDBCTypeId = Types.LONGVARCHAR;
                break;

            case StoredFormatIds.CLOB_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.CLOB_NAME;
                JDBCTypeId = Types.CLOB;
                break;

            case StoredFormatIds.BIT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.BIT_NAME;
                JDBCTypeId = Types.BINARY;
                break;

            case StoredFormatIds.VARBIT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.VARBIT_NAME;
                JDBCTypeId = Types.VARBINARY;
                break;

            case StoredFormatIds.LONGVARBIT_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.LONGVARBIT_NAME;
                JDBCTypeId = Types.LONGVARBINARY;
                break;

            case StoredFormatIds.BLOB_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.BLOB_NAME;
                JDBCTypeId = Types.BLOB;
                break;

            case StoredFormatIds.DATE_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.DATE_NAME;
                JDBCTypeId = Types.DATE;
                break;

            case StoredFormatIds.TIME_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.TIME_NAME;
                JDBCTypeId = Types.TIME;
                break;

            case StoredFormatIds.TIMESTAMP_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.TIMESTAMP_NAME;
                JDBCTypeId = Types.TIMESTAMP;
                break;

            case StoredFormatIds.XML_TYPE_ID_IMPL:
                schemaName = null;
                unqualifiedName = TypeId.XML_NAME;
                JDBCTypeId = Types.SQLXML;
                break;

            default:
                break;
        }
    }

    @Override
    public int getTypeFormatId() {
        if (formatId != 0) {
            return formatId;
        } else {
            return 0;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (schemaName == null) {
            out.writeUTF(unqualifiedName);
        } else {
            out.writeUTF(doubleQuote(schemaName));
            out.writeUTF(unqualifiedName);
        }
    }

    private String doubleQuote(String raw) {
        return '"' + raw + '"';
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        unqualifiedName = in.readUTF();
        if (unqualifiedName.charAt(0) == '"') {
            schemaName = stripQuotes(unqualifiedName);
            unqualifiedName = in.readUTF();
        }
    }

    protected String schemaName;

    private String stripQuotes(String quoted) {
        return quoted.substring(1, quoted.length() - 1);
    }

    public int getJDBCTypeId() {
        return JDBCTypeId;
    }

    public String getSQLTypeName() {
        if (schemaName == null) {
            return unqualifiedName;
        } else {
            return IdUtil.mkQualifiedName(schemaName, unqualifiedName);
        }
    }

    public String toParsableString(TypeDescriptor td) {
        String retval = getSQLTypeName();

        switch (getTypeFormatId()) {
            case StoredFormatIds.BIT_TYPE_ID_IMPL:
            case StoredFormatIds.VARBIT_TYPE_ID_IMPL:
                int rparen = retval.indexOf(')');
                String lead = retval.substring(0, rparen);
                retval = lead + td.getMaximumWidth() + retval.substring(rparen);
                break;

            case StoredFormatIds.CHAR_TYPE_ID_IMPL:
            case StoredFormatIds.VARCHAR_TYPE_ID_IMPL:
            case StoredFormatIds.BLOB_TYPE_ID_IMPL:
            case StoredFormatIds.CLOB_TYPE_ID_IMPL:
                retval += "(" + td.getMaximumWidth() + ")";
                break;
            case StoredFormatIds.DECIMAL_TYPE_ID_IMPL:
                retval += "(" + td.getPrecision() + "," + td.getScale() + ")";
                break;
        }
        return retval;
    }
}
