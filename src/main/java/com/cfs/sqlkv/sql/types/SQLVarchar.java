package com.cfs.sqlkv.sql.types;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;
import com.cfs.sqlkv.catalog.types.TypeId;

import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.DataType;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.text.RuleBasedCollator;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-08 10:52
 */
public class SQLVarchar extends SQLChar {

    public SQLVarchar() {
    }

    public SQLVarchar(String val) {
        super(val);
    }


    public int getTypeFormatId() {
        return StoredFormatIds.SQL_VARCHAR_ID;
    }


    public int typePrecedence() {
        return TypeId.VARCHAR_PRECEDENCE;
    }

    public void normalize(
            DataTypeDescriptor desiredType,
            DataValueDescriptor source) {
        normalize(desiredType, source.getString());
    }


    protected void normalize(DataTypeDescriptor desiredType, String sourceValue) {

        int desiredWidth = desiredType.getMaximumWidth();

        int sourceWidth = sourceValue.length();

        /*
         ** If the input is already the right length, no normalization is
         ** necessary.
         **
         ** It's OK for a Varchar value to be shorter than the desired width.
         ** This can happen, for example, if you insert a 3-character Varchar
         ** value into a 10-character Varchar column.  Just return the value
         ** in this case.
         */

        if (sourceWidth > desiredWidth) {
            hasNonBlankChars(sourceValue, desiredWidth, sourceWidth);
            sourceValue = sourceValue.substring(0, desiredWidth);
        }

        setValue(sourceValue);
    }

    public StringDataValue getValue(RuleBasedCollator collatorForComparison) {
        return this;
    }


    public DataValueDescriptor cloneValue(boolean forceMaterialization) {

        return new SQLVarchar(getString());

    }
}
