package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.sql.dictionary.TableDescriptor;
import com.cfs.sqlkv.store.TransactionControl;
import com.cfs.sqlkv.store.TransactionManager;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-27 15:33
 */
public interface DataDictionary {

    public static final int SYSCONGLOMERATES_CATALOG_NUM = 0;
    public static final int SYSTABLES_CATALOG_NUM = 1;
    public static final int SYSCOLUMNS_CATALOG_NUM = 2;
    public static final int SYSSCHEMAS_CATALOG_NUM = 3;

    public static final int COMPILE_ONLY_MODE = 0;

    public static final String module = DataDictionary.class.getName();

    public int startReading(LanguageConnectionContext lcc)  ;

    public void doneReading(int mode, LanguageConnectionContext lcc)  ;

    public void startWriting(LanguageConnectionContext lcc)  ;

    public SchemaDescriptor	getSchemaDescriptor(UUID schemaId, TransactionManager tc)  ;


    public SchemaDescriptor	getSchemaDescriptor(String schemaName, TransactionManager tc, boolean raiseError)  ;

    public DataDescriptorGenerator getDataDescriptorGenerator();

    public void addDescriptor(TupleDescriptor tuple, TupleDescriptor parent, int catalogNumber, boolean allowsDuplicates, TransactionManager tc)  ;

    public TableDescriptor getTableDescriptor(String tableName, SchemaDescriptor schema, TransactionManager tc);

    public void addDescriptorArray(TupleDescriptor[] tuple, TupleDescriptor parent, int catalogNumber, boolean allowsDuplicates, TransactionManager tc)  ;
}
