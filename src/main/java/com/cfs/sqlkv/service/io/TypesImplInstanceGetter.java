package com.cfs.sqlkv.service.io;

import com.cfs.sqlkv.catalog.types.BaseTypeIdImpl;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-07 11:17
 */
public class TypesImplInstanceGetter extends FormatableInstanceGetter{
    public Object getNewInstance() {
        switch (fmtId) {
            case StoredFormatIds.BOOLEAN_TYPE_ID_IMPL:
            case StoredFormatIds.INT_TYPE_ID_IMPL:
            case StoredFormatIds.SMALLINT_TYPE_ID_IMPL:
            case StoredFormatIds.TINYINT_TYPE_ID_IMPL:
            case StoredFormatIds.BIGINT_TYPE_ID_IMPL:
            case StoredFormatIds.DOUBLE_TYPE_ID_IMPL:
            case StoredFormatIds.REAL_TYPE_ID_IMPL:
            case StoredFormatIds.REF_TYPE_ID_IMPL:
            case StoredFormatIds.CHAR_TYPE_ID_IMPL:
            case StoredFormatIds.VARCHAR_TYPE_ID_IMPL:
            case StoredFormatIds.LONGVARCHAR_TYPE_ID_IMPL:
            case StoredFormatIds.BIT_TYPE_ID_IMPL:
            case StoredFormatIds.VARBIT_TYPE_ID_IMPL:
            case StoredFormatIds.LONGVARBIT_TYPE_ID_IMPL:
            case StoredFormatIds.DATE_TYPE_ID_IMPL:
            case StoredFormatIds.TIME_TYPE_ID_IMPL:
            case StoredFormatIds.TIMESTAMP_TYPE_ID_IMPL:
            case StoredFormatIds.BLOB_TYPE_ID_IMPL:
            case StoredFormatIds.CLOB_TYPE_ID_IMPL:
            case StoredFormatIds.XML_TYPE_ID_IMPL:
                return new BaseTypeIdImpl(fmtId);
            default:
                return null;
        }
    }
}