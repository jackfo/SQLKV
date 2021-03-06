package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.TypeDescriptor;
import com.cfs.sqlkv.service.io.Formatable;

import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Types;

/**
 * @author zhengxiaokang
 * @Description DataTypeDescriptor描述一个运行的SQL类型
 * It consists of a catalog type (TypeDescriptor) and runtime attributes. The list of runtime attributes is:
 * <p>
 * 当前类实现了Formatable。说明可以将自己写入到相应格式流或者从格式流中读取
 * 如果往当前类添加字段,确保你可以从writeExternal()/readExternal()方法中写或者读
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 17:51
 */
public class DataTypeDescriptor implements Formatable {

    public static final DataTypeDescriptor INTEGER = new DataTypeDescriptor(TypeId.INTEGER_ID, true);

    private TypeDescriptorImpl typeDescriptor;
    private TypeId typeId;

    private DataTypeDescriptor(TypeDescriptorImpl source, TypeId typeId) {
        typeDescriptor = source;
        this.typeId = typeId;
    }

    public DataTypeDescriptor(TypeId typeId, int precision, int scale,
                              boolean isNullable, int maximumWidth) {
        this.typeId = typeId;
        typeDescriptor = new TypeDescriptorImpl(typeId.getBaseTypeId(),
                precision,
                scale,
                isNullable,
                maximumWidth);
    }

    private DataTypeDescriptor(DataTypeDescriptor source, boolean isNullable) {
        this.typeId = source.typeId;
        typeDescriptor = new TypeDescriptorImpl(typeId.getBaseTypeId(),
                typeId.getMaximumPrecision(),
                typeId.getMaximumScale(),
                isNullable,
                typeId.getMaximumMaximumWidth());
    }


    public DataTypeDescriptor(TypeId typeId, boolean isNullable) {
        this.typeId = typeId;
        typeDescriptor = new TypeDescriptorImpl(typeId.getBaseTypeId(),
                typeId.getMaximumPrecision(),
                typeId.getMaximumScale(),
                isNullable,
                typeId.getMaximumMaximumWidth());
    }

    public DataTypeDescriptor(TypeId typeId, boolean isNullable, int maximumWidth) {
        this.typeId = typeId;
        typeDescriptor = new TypeDescriptorImpl(typeId.getBaseTypeId(),
                isNullable,
                maximumWidth);
    }

    private int collationDerivation = StringDataValue.COLLATION_DERIVATION_IMPLICIT;

    private DataTypeDescriptor(DataTypeDescriptor source,
                               int collationType,
                               int collationDerivation) {

        this.typeId = source.typeId;
        typeDescriptor = new TypeDescriptorImpl(source.typeDescriptor,
                source.getPrecision(),
                source.getScale(),
                source.isNullable(),
                source.getMaximumWidth(),
                collationType
        );
        this.collationDerivation = collationDerivation;
    }


    @Override
    public int getTypeFormatId() {
        return 0;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

    public static DataTypeDescriptor getBuiltInDataTypeDescriptor(String sqlTypeName) {
        return new DataTypeDescriptor(TypeId.getBuiltInTypeId(sqlTypeName), true);
    }

    public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType) {
        return DataTypeDescriptor.getBuiltInDataTypeDescriptor(jdbcType, true);
    }

    public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType, int length) {
        return DataTypeDescriptor.getBuiltInDataTypeDescriptor(jdbcType, true, length);
    }

    public DataTypeDescriptor getNullabilityType(boolean isNullable) {


        return new DataTypeDescriptor(this, isNullable);
    }

    public static final DataTypeDescriptor INTEGER_NOT_NULL = INTEGER.getNullabilityType(false);

    public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType, boolean isNullable) {
        switch (jdbcType) {
            case Types.INTEGER:
                return isNullable ? INTEGER : INTEGER_NOT_NULL;
            default:
                break;
        }


        TypeId typeId = TypeId.getBuiltInTypeId(jdbcType);
        if (typeId == null) {
            return null;
        }

        return new DataTypeDescriptor(typeId, isNullable);
    }

    public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType, boolean isNullable, int maxLength) {
        TypeId typeId = TypeId.getBuiltInTypeId(jdbcType);
        if (typeId == null) {
            return null;
        }
        return new DataTypeDescriptor(typeId, isNullable, maxLength);
    }

    public DataValueDescriptor getNull() {
        DataValueDescriptor returnDVD = typeId.getNull();
        return returnDVD;
    }

    public int getCollationType() {
        return typeDescriptor.getCollationType();
    }

    public TypeId getTypeId() {
        return typeId;
    }



    public DataTypeDescriptor getCollatedType(int collationType,
                                              int collationDerivation) {
//        if (!typeDescriptor.isStringType()){
//
//            return this;
//        }
//
//        if ((getCollationType() == collationType) &&
//                (getCollationDerivation() == collationDerivation))
//            return this;

        return new DataTypeDescriptor(this,
                collationType,
                collationDerivation);
    }

    public int getMaximumWidth() {
        return typeDescriptor.getMaximumWidth();
    }

    public boolean isNullable() {
        return typeDescriptor.isNullable();
    }

    public int getPrecision() {
        return typeDescriptor.getPrecision();
    }

    public int getScale() {
        return typeDescriptor.getScale();
    }

    public int getJDBCTypeId() {
        return typeDescriptor.getJDBCTypeId();
    }

    public TypeDescriptor getCatalogType() {
        return typeDescriptor;
    }

    /**
     * 根据目录类型获取类型描述
     */
    public static DataTypeDescriptor getType(TypeDescriptor catalogType) {
        TypeDescriptorImpl typeDescriptor = (TypeDescriptorImpl) catalogType;
        TypeId typeId = TypeId.getTypeId(catalogType);
        DataTypeDescriptor dtd = new DataTypeDescriptor(typeDescriptor, typeId);
        dtd.collationDerivation = StringDataValue.COLLATION_DERIVATION_IMPLICIT;
        return dtd;
    }

    public DataValueDescriptor normalize(DataValueDescriptor source, DataValueDescriptor cachedDest) {
        if (source.isNull()) {
            if (!isNullable())
                throw new RuntimeException("normalize");
            cachedDest.setToNull();
        } else {
            int jdbcId = getJDBCTypeId();
            cachedDest.normalize(this, source);
            if ((jdbcId == Types.LONGVARCHAR) || (jdbcId == Types.LONGVARBINARY)) {
                if (source.getClass() == cachedDest.getClass())
                    return source;
            }

        }
        return cachedDest;
    }



}
