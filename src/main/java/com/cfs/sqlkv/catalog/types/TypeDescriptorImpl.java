package com.cfs.sqlkv.catalog.types;

import com.cfs.sqlkv.catalog.Formatable;
import com.cfs.sqlkv.catalog.TypeDescriptor;
import com.cfs.sqlkv.io.StoredFormatIds;
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
public class TypeDescriptorImpl implements TypeDescriptor,Formatable {

    private int	collationType = StringDataValue.COLLATION_TYPE_UCS_BASIC;

    public TypeDescriptorImpl(BaseTypeIdImpl typeId,boolean isNullable,int maximumWidth) {

    }

    private BaseTypeIdImpl		typeId;
    private int						precision;
    private int						scale;
    private boolean					isNullable;
    private int						maximumWidth;

    public TypeDescriptorImpl(
            BaseTypeIdImpl typeId,
            int precision,
            int scale,
            boolean isNullable,
            int maximumWidth)
    {
        this.typeId = typeId;
        this.precision = precision;
        this.scale = scale;
        this.isNullable = isNullable;
        this.maximumWidth = maximumWidth;
    }

    public TypeDescriptorImpl(BaseTypeIdImpl typeId,int precision,int scale,boolean isNullable,int maximumWidth, int collationType) {
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
    public int	getMaximumWidth() {
        return maximumWidth;
    }

    /**
     * Returns the number of decimal digits for the datatype, if applicable.
     *
     * @return	The number of decimal digits for the datatype.  Returns
     *		zero for non-numeric datatypes.
     */
    @Override
    public int	getPrecision() {
        return precision;
    }

    /**
     * Returns the number of digits to the right of the decimal for
     * the datatype, if applicable.
     *
     * @return	The number of digits to the right of the decimal for
     *		the datatype.  Returns zero for non-numeric datatypes.
     */
    @Override
    public int	getScale() {
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
     * @return	TRUE if the datatype can contain NULL, FALSE if not.
     */
    @Override
    public boolean	isNullable() {
        return isNullable;
    }

    public int	getCollationType() {
        return collationType;
    }

    public void	setCollationType(int collationTypeValue)
    {
        collationType = collationTypeValue;
    }





    /**
     * Get the type Id stored within this type descriptor.
     */
    public BaseTypeIdImpl getTypeId()
    {
        return typeId;
    }



    // Formatable methods

    /**
     * Read this object from a stream of stored objects.
     *
     * @param in read this.
     *
     * @exception IOException					thrown on error
     * @exception ClassNotFoundException		thrown on error
     */
    @Override
    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {

    }

    /**
     * Write this object to a stream of stored objects.
     *
     * @param out write bytes here.
     *
     * @exception IOException		thrown on error
     */
    @Override
    public void writeExternal( ObjectOutput out ) throws IOException {

    }

    /**
     * Get the formatID which corresponds to this class.
     *
     *	@return	the formatID of this class
     */
    @Override
    public	int	getTypeFormatId(){
        return StoredFormatIds.DATA_TYPE_IMPL_DESCRIPTOR_V01_ID;
    }




}
