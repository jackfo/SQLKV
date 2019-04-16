package com.cfs.sqlkv.util;

import com.cfs.sqlkv.catalog.DataDictionary;
import com.cfs.sqlkv.catalog.SchemaDescriptor;
import com.cfs.sqlkv.common.StatementType;
import com.cfs.sqlkv.compile.CompilerContext;
import com.cfs.sqlkv.context.LanguageConnectionContext;


/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-07 15:37
 */
public class StatementUtil {

    private StatementUtil(){};

    private static final String[] TypeNames = {"", "INSERT", "INSERT", "UPDATE", "DELETE", "ENABLED", "DISABLED"};

    public static String typeName(int typeNumber)
    {
        String retval;

        switch (typeNumber)
        {
            case StatementType.INSERT:
            case StatementType.BULK_INSERT_REPLACE:
            case StatementType.UPDATE:
            case StatementType.DELETE:
            case StatementType.ENABLED:
            case StatementType.DISABLED:
                retval = TypeNames[typeNumber];
                break;

            default:
                retval = "UNKNOWN";
                break;
        }

        return retval;
    }

    /**
     * 获取当前schemaName对应的描述
     * */
    public static SchemaDescriptor getSchemaDescriptor(String schemaName, boolean raiseError, DataDictionary dataDictionary, LanguageConnectionContext lcc, CompilerContext cc){
        SchemaDescriptor sd = lcc.getDefaultSchema();
        return sd;
    }

}
