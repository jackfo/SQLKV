package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.context.LanguageConnectionContext;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.sql.dictionary.DataDescriptorGenerator;
import com.cfs.sqlkv.store.TransactionController;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 19:17
 */
public class DataDictionaryImpl implements DataDictionary {

    private static final int NUM_CORE = 4;

    volatile int readersInDDLMode;

    volatile int cacheMode = DataDictionary.COMPILE_ONLY_MODE;

    protected UUIDFactory uuidFactory;

    public DataDictionaryImpl(){
        dataDescriptorGenerator = new DataDescriptorGenerator(this);
        coreInfo = new TabInfoImpl[NUM_CORE];
    }

    /**
     *
     * */
    @Override
    public int startReading(LanguageConnectionContext lcc) throws StandardException {

        int     bindCount = lcc.incrementBindCount();
        int     localCacheMode;
        boolean needRetry = false;
        do{
            synchronized(this){
                localCacheMode = getCacheMode();
                if (bindCount == 1){
                    readersInDDLMode++;
                }
            }

        }while (needRetry);
        return localCacheMode;
    }


    /**
     * 开始执行的时候减少绑定次数
     * */
    @Override
    public void doneReading(int mode, LanguageConnectionContext lcc) throws StandardException{
        int bindCount = lcc.decrementBindCount();
        synchronized(this){

        }
    }

    @Override
    public void startWriting(LanguageConnectionContext lcc) throws StandardException {
        boolean blocked = true;
    }

    @Override
    public SchemaDescriptor getSchemaDescriptor(UUID schemaId, TransactionController tc) throws StandardException {
        return getSchemaDescriptorBody(
                schemaId,
                TransactionController.ISOLATION_REPEATABLE_READ,
                tc);
    }

    @Override
    public SchemaDescriptor getSchemaDescriptor(String schemaName, TransactionController tc, boolean raiseError) throws StandardException {
        return null;
    }

    public	DataDescriptorGenerator dataDescriptorGenerator;
    @Override
    public DataDescriptorGenerator getDataDescriptorGenerator() {
        return dataDescriptorGenerator;
    }

    /**存放这表的核心消息*/
    private TabInfoImpl[] coreInfo;
    /**表的非核心消息*/
    private TabInfoImpl[] noncoreInfo;

    /**
     * 通过catalogNumber添加一个描述到系统目录标识
     * @param tuple 插入的描述
     * @param parent 父描述
     * @param catalogNumber
     * */
    @Override
    public void addDescriptor(TupleDescriptor tuple, TupleDescriptor parent, int catalogNumber, boolean allowsDuplicates, TransactionController tc) throws StandardException {
        TabInfoImpl ti =  (catalogNumber < NUM_CORE) ? coreInfo[catalogNumber] : getNonCoreTI(catalogNumber);
    }

    /**
     * 获取非核心表的信息
     * @param catalogNumber
     */
    private TabInfoImpl getNonCoreTI(int catalogNumber) throws StandardException {
        TabInfoImpl	ti = getNonCoreTIByNumber(catalogNumber);
        faultInTabInfo( ti );
        return ti;
    }

    /**
     * 返回非核心系统目录的表信息
     */
    protected TabInfoImpl getNonCoreTIByNumber(int catalogNumber) throws StandardException{

        int nonCoreNum = catalogNumber - NUM_CORE;

        TabInfoImpl retval = noncoreInfo[nonCoreNum];
        if (retval == null){
            UUIDFactory luuidFactory = uuidFactory;
            switch (catalogNumber){

            }

            noncoreInfo[nonCoreNum] = retval;
        }
        return retval;
    }

    private void faultInTabInfo(TabInfoImpl ti) throws StandardException {
        //数字索引
        int	numIndexes;


    }

    private SchemaDescriptor getSchemaDescriptorBody(UUID schemaId, int isolationLevel, TransactionController tc) throws StandardException {
        LanguageConnectionContext	lcc = getLCC();
        SchemaDescriptor sd = lcc.getDefaultSchema();
        return sd;
    }

    private static LanguageConnectionContext getLCC() {
        return (LanguageConnectionContext)
                getContextOrNull(LanguageConnectionContext.CONTEXT_ID);
    }

    private static Context getContextOrNull(final String contextID ) {
        return ContextService.getContextOrNull( contextID );
    }

    public int getCacheMode() {
        return cacheMode;
    }
}
