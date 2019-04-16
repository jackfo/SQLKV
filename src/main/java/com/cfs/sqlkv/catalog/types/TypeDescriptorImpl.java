package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.catalog.TypeDescriptor;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Types;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 18:08
 */
public class TypeDescriptorImpl implements TypeDescriptor, Formatable {

    private int collationType = StringDataValue.COLLATION_TYPE_UCS_BASIC;


    public TypeDescriptorImpl() {
    }

    public TypeDescriptorImpl(BaseTypeIdImpl typeId, boolean isNullable, int maximumWidth) {

    }

    private BaseTypeIdImpl typeId;
    private int precision;
    private int scale;
    private boolean isNullable;
    private int maximumWidth;

    public TypeDescriptorImpl(
            BaseTypeIdImpl typeId,
            int precision,
            int scale,
            boolean isNullable,
            int maximumWidth) {
        this.typeId = typeId;
        this.precision = precision;
        this.scale = scale;
        this.isNullable = isNullable;
        this.maximumWidth = maximumWidth;
    }

    public TypeDescriptorImpl(BaseTypeIdImpl typeId, int precision, int scale, boolean isNullable, int maximumWidth, int collationType) {
        this.typeId = typeId;
        this.precision = precision;
        this.scale = scale;
        this.isNullable = isNullable;
        this.maximumWidth = maximumWidth;
        this.collationType = collationType;
    }

    public TypeDescriptorImpl(TypeDescriptorImpl source, int precision, int scale, boolean isNullable, int maximumWidth, int collationType) {
        this.typeId = source.typeId;
        this.precision = precision;
        this.scale = scale;
        this.isNullable = isNullable;
        this.maximumWidth = maximumWidth;
        this.collationType = collationType;
    }

    public int getMaximumWidth() {
        return maximumWidth;
    }

    /**
     * Returns the number of decimal digits for the datatype, if applicable.
     *
     * @return The number of decimal digits for the datatype.  Returns
     * zero for non-numeric datatypes.
     */
    @Override
    public int getPrecision() {
        return precision;
    }

    /**
     * Returns the number of digits to the right of the decimal for
     * the datatype, if applicable.
     *
     * @return The number of digits to the right of the decimal for
     * the datatype.  Returns zero for non-numeric datatypes.
     */
    @Override
    public int getScale() {
        return scale;
    }

    @Override
    public int getMaximumWidthInBytes() {
        return 0;
    }

    /**
     * Returns TRUE if the datatype can contain NULL, FALSE if not.
     * JDBC supports a return value meaning "nullability unknown" -
     * I assume we will never have columns where the nullability is unknown.
     *
     * @return TRUE if the datatype can contain NULL, FALSE if not.
     */
    @Override
    public boolean isNullable() {
        return isNullable;
    }

    public int getCollationType() {
        return collationType;
    }

    public void setCollationType(int collationTypeValue) {
        collationType = collationTypeValue;
    }


    /**
     * Get the type Id stored within this type descriptor.
     */
    public BaseTypeIdImpl getTypeId() {
        return typeId;
    }


    // Formatable methods

    /**
     * Read this object from a stream of stored objects.
     *
     * @param in read this.
     * @throws IOException            thrown on error
     * @throws ClassNotFoundException thrown on error
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        typeId = (BaseTypeIdImpl) in.readObject();
        precision = in.readInt();
        switch (typeId.getJDBCTypeId()) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                scale = 0;
                collationType = in.readInt();
                break;
            default:
                scale = in.readInt();
                collationType = 0;
                break;
        }

        isNullable = in.readBoolean();
        maximumWidth = in.readInt();
    }

    /**
     * Write this object to a stream of stored objects.
     *
     * @param out write bytes here.
     * @throws IOException thrown on error
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(typeId);
        out.writeInt(precision);
        switch (typeId.getJDBCTypeId()) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                out.writeInt(collationType);
                break;
            default:
                out.writeInt(scale);
                break;
        }
        out.writeBoolean(isNullable);
        out.writeInt(maximumWidth);
    }

    /**
     * Get the formatID which corresponds to this class.
     *
     * @return the formatID of this class
     */
    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.DATA_TYPE_IMPL_DESCRIPTOR_V01_ID;
    }

    public int getJDBCTypeId() {
        return typeId.getJDBCTypeId();
    }

    @Override
    public String toString() {
        String s = getSQLstring();
        if (!isNullable())
            return s + " NOT NULL";
        return s;
    }

    public String getSQLstring() {
        return typeId.toParsableString(this);
    }
}
