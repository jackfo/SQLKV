package com.cfs.sqlkv.catalog;

import com.cfs.sqlkv.catalog.types.ColumnDescriptor;
import com.cfs.sqlkv.column.ColumnDescriptorList;
import com.cfs.sqlkv.common.UUID;
import com.cfs.sqlkv.common.context.ContextService;
import com.cfs.sqlkv.context.Context;
import com.cfs.sqlkv.context.LanguageConnectionContext;

import com.cfs.sqlkv.factory.DataValueFactory;
import com.cfs.sqlkv.factory.DataValueFactoryImpl;
import com.cfs.sqlkv.factory.UUIDFactory;
import com.cfs.sqlkv.row.ExecIndexRow;
import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.service.cache.CacheManager;
import com.cfs.sqlkv.service.cache.Cacheable;
import com.cfs.sqlkv.service.cache.CacheableFactory;
import com.cfs.sqlkv.sql.dictionary.*;
import com.cfs.sqlkv.sql.types.SQLChar;
import com.cfs.sqlkv.sql.types.SQLVarchar;
import com.cfs.sqlkv.store.TransactionControl;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.conglomerate.ScanController;
import com.cfs.sqlkv.store.access.heap.TableRowLocation;
import com.cfs.sqlkv.temp.Temp;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.util.List;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-05 19:17
 */
public class DataDictionaryImpl implements DataDictionary, CacheableFactory {

    private static final int NUM_CORE = 4;

    volatile int readersInDDLMode;

    volatile int cacheMode = DataDictionary.COMPILE_ONLY_MODE;

    protected UUIDFactory uuidFactory = new UUIDFactory();

    private DataValueFactory dvf = new DataValueFactoryImpl();

    private CacheManager nameTdCache = new CacheManager(this, "TableDescriptorNameCache", 10, 100);

    /**
     * CONGLOME
     */
    private static final int SYSCONGLOMERATES_CORE_NUM = 0;
    private static final int SYSTABLES_CORE_NUM = 1;
    private static final int SYSCOLUMNS_CORE_NUM = 2;
    /**
     * 模式行
     */
    private static final int SYSSCHEMAS_CORE_NUM = 3;

    //TODO:暂时设计为前四个文件标识号
    public static int SYS_CONGLOMERATES_IDENTIFER = 16;
    public static int SYS_CONGLOMERATES_IDENTIFER_INDEX1 = 33;
    public static int SYS_CONGLOMERATES_IDENTIFER_INDEX2 = 49;
    public static int SYS_CONGLOMERATES_IDENTIFER_INDEX3 = 65;
    //这是因为上面一个存在三个索引
    public static int SYS_TABLE_IDENTIFER = 80;
    public static int SYS_TABLE_INDEX1 = 97;
    public static int SYS_TABLE_INDEX2 = 113;
    public static int SYS_COLUMNS_IDENTIFER = 128;
    public static int SYS_COLUMNS_IDENTIFER_INDEX1 = 145;
    public static int SYS_COLUMNS_IDENTIFER_INDEX2 = 161;
    public static int SYS_SCHEMAS_IDENTIFER = 176;
    public static int SYS_SCHEMAS_IDENTIFER_INDEX1 = 193;
    public static int SYS_SCHEMAS_IDENTIFER_INDEX2 = 209;

    public TransactionManager transactionController;

    public DataDictionaryImpl(TransactionManager transactionController) {
        this.transactionController = transactionController;
        dataDescriptorGenerator = new DataDescriptorGenerator(this);

    }


    public void initializeCoreInfo(boolean create) {
        coreInfo = new TabInfoImpl[NUM_CORE];
        UUIDFactory luuidFactory = uuidFactory;
        coreInfo[SYSCONGLOMERATES_CORE_NUM] = new TabInfoImpl(new SYSCONGLOMERATESRowFactory(luuidFactory, dvf));
        coreInfo[SYSTABLES_CORE_NUM] = new TabInfoImpl(new SYSTABLESRowFactory(luuidFactory, dvf));
        coreInfo[SYSCOLUMNS_CORE_NUM] = new TabInfoImpl(new SYSCOLUMNSRowFactory(this, luuidFactory, dvf));
        coreInfo[SYSSCHEMAS_CORE_NUM] = new TabInfoImpl(new SYSSCHEMASRowFactory(luuidFactory, dvf));
        if (!create) {
            setConglomerate();
        }
    }

    private void setConglomerate() {
        coreInfo[SYSTABLES_CORE_NUM].setTableConglomerate(SYS_TABLE_IDENTIFER);
        coreInfo[SYSTABLES_CORE_NUM].setIndexConglomerate(SYSTABLESRowFactory.SYSTABLES_INDEX1_ID, SYS_TABLE_INDEX1);
        coreInfo[SYSTABLES_CORE_NUM].setIndexConglomerate(SYSTABLESRowFactory.SYSTABLES_INDEX2_ID, SYS_TABLE_INDEX2);
        // SYSCOLUMNS
        coreInfo[SYSCOLUMNS_CORE_NUM].setTableConglomerate(SYS_COLUMNS_IDENTIFER);
        coreInfo[SYSCOLUMNS_CORE_NUM].setIndexConglomerate(SYSCOLUMNSRowFactory.SYSCOLUMNS_INDEX1_ID, SYS_COLUMNS_IDENTIFER_INDEX1);
        coreInfo[SYSCOLUMNS_CORE_NUM].setIndexConglomerate(SYSCOLUMNSRowFactory.SYSCOLUMNS_INDEX2_ID, SYS_COLUMNS_IDENTIFER_INDEX2);

        // SYSCONGLOMERATES
        coreInfo[SYSCONGLOMERATES_CORE_NUM].setTableConglomerate(SYS_CONGLOMERATES_IDENTIFER);
        coreInfo[SYSCONGLOMERATES_CORE_NUM].setIndexConglomerate(SYSCONGLOMERATESRowFactory.SYSCONGLOMERATES_INDEX1_ID, SYS_CONGLOMERATES_IDENTIFER_INDEX1);
        coreInfo[SYSCONGLOMERATES_CORE_NUM].setIndexConglomerate(SYSCONGLOMERATESRowFactory.SYSCONGLOMERATES_INDEX2_ID, SYS_CONGLOMERATES_IDENTIFER_INDEX2);
        coreInfo[SYSCONGLOMERATES_CORE_NUM].setIndexConglomerate(SYSCONGLOMERATESRowFactory.SYSCONGLOMERATES_INDEX3_ID, SYS_CONGLOMERATES_IDENTIFER_INDEX3);

        // SYSSCHEMAS
        coreInfo[SYSSCHEMAS_CORE_NUM].setTableConglomerate(SYS_SCHEMAS_IDENTIFER);
        coreInfo[SYSSCHEMAS_CORE_NUM].setIndexConglomerate(SYSSCHEMASRowFactory.SYSSCHEMAS_INDEX1_ID, SYS_SCHEMAS_IDENTIFER_INDEX1);
        coreInfo[SYSSCHEMAS_CORE_NUM].setIndexConglomerate(SYSSCHEMASRowFactory.SYSSCHEMAS_INDEX2_ID, SYS_SCHEMAS_IDENTIFER_INDEX2);
    }

    /**
     * 创建字典表,在这里是创建所有的系统表
     * 如:table属性相关表 模式属性相关表 列属性相关表
     */
    public void createDictionaryTables(TransactionManager transactionController)   {
        systemSchemaDesc = newSystemSchemaDesc(SchemaDescriptor.STD_SYSTEM_SCHEMA_NAME, SchemaDescriptor.SYSTEM_SCHEMA_UUID);
        for (int coreCtr = 0; coreCtr < NUM_CORE; coreCtr++) {
            Properties heapProperties = new Properties();
            TabInfoImpl ti = coreInfo[coreCtr];
            CatalogRowFactory catalogRowFactory = ti.getCatalogRowFactory();
            ExecRow templateRow = catalogRowFactory.makeEmptyRow();
            if (templateRow == null) {
                throw new RuntimeException(String.format("%s can't make empty row", catalogRowFactory));
            }
            long conglomId = createConglomerate(transactionController, templateRow, heapProperties);
            ti.setTableConglomerate(conglomId);
            if (coreInfo[coreCtr].getNumberOfIndexes() > 0) {
                bootStrapSystemIndexes(systemSchemaDesc, transactionController, dataDescriptorGenerator, ti);
            }
        }
    }

    /**
     * 创建对应的Conglomerate
     */
    private long createConglomerate(TransactionManager transactionController, ExecRow rowTemplate, Properties properties)   {
        DataValueDescriptor[] dataValueDescriptors = rowTemplate.getRowArray();
        long conglomId = transactionController.createConglomerate("table", dataValueDescriptors, null, properties);
        return conglomId;
    }

    /**
     *
     */
    @Override
    public int startReading(LanguageConnectionContext lcc)   {

        int bindCount = lcc.incrementBindCount();
        int localCacheMode;
        boolean needRetry = false;
        do {
            synchronized (this) {
                localCacheMode = getCacheMode();
                if (bindCount == 1) {
                    readersInDDLMode++;
                }
            }

        } while (needRetry);
        return localCacheMode;
    }


    /**
     * 开始执行的时候减少绑定次数
     */
    @Override
    public void doneReading(int mode, LanguageConnectionContext lcc)   {
    }

    @Override
    public void startWriting(LanguageConnectionContext lcc)   {
        boolean blocked = true;
    }

    @Override
    public SchemaDescriptor getSchemaDescriptor(UUID schemaId, TransactionManager tc)   {
        return getSchemaDescriptorBody(
                schemaId,
                TransactionManager.ISOLATION_REPEATABLE_READ,
                tc);
    }


    public DataDescriptorGenerator dataDescriptorGenerator;

    @Override
    public DataDescriptorGenerator getDataDescriptorGenerator() {
        return dataDescriptorGenerator;
    }

    /**
     * 存放这表的核心消息
     */
    private TabInfoImpl[] coreInfo;
    /**
     * 表的非核心消息
     */
    private TabInfoImpl[] noncoreInfo;

    /**
     * 通过catalogNumber添加一个描述到系统目录标识
     *
     * @param tuple         插入的描述
     * @param parent        父描述
     * @param catalogNumber
     */
    @Override
    public void addDescriptor(TupleDescriptor tuple, TupleDescriptor parent, int catalogNumber, boolean allowsDuplicates, TransactionManager tc)   {
        TabInfoImpl tabInfo = (catalogNumber < NUM_CORE) ? coreInfo[catalogNumber] : getNonCoreTI(catalogNumber);
        ExecRow row = tabInfo.getCatalogRowFactory().makeRow(tuple, parent);
        int insertRetCode = tabInfo.insertRow(row, tc);
    }

    @Override
    public TableDescriptor getTableDescriptor(String tableName, SchemaDescriptor schema, TransactionManager tc) {
        TableDescriptor returnValue = null;

        UUID schemaUUID = schema.getUUID();


        TableKey tableKey = new TableKey(schemaUUID, tableName);
        if (getCacheMode() == DataDictionary.COMPILE_ONLY_MODE) {
            TableDescriptorCacheable cacheEntry = (TableDescriptorCacheable) nameTdCache.find(tableKey);
            if (cacheEntry != null) {
                returnValue = cacheEntry.getTableDescriptor();
                nameTdCache.release(cacheEntry);
            }
            return returnValue;
        }
        return null;
    }

    @Override
    public void addDescriptorArray(TupleDescriptor[] tuple, TupleDescriptor parent, int catalogNumber, boolean allowsDuplicates, TransactionManager tc)   {
        TabInfoImpl ti = (catalogNumber < NUM_CORE) ? coreInfo[catalogNumber] : getNonCoreTI(catalogNumber);
        CatalogRowFactory crf = ti.getCatalogRowFactory();
        ExecRow[] rl = new ExecRow[tuple.length];
        for (int index = 0; index < tuple.length; index++) {
            ExecRow row = crf.makeRow(tuple[index], parent);
            rl[index] = row;
        }
        int insertRetCode = ti.insertRowList(rl, tc);
    }

    public String getVTIClass(TableDescriptor td, boolean asTableFunction)   {
        if (SchemaDescriptor.STD_SYSTEM_DIAG_SCHEMA_NAME.equals(td.getSchemaName())) {
            return getBuiltinVTIClass(td, asTableFunction);
        } else {

        }
        return null;
    }

    public String getBuiltinVTIClass(TableDescriptor td, boolean asTableFunction) {
        if (SchemaDescriptor.STD_SYSTEM_DIAG_SCHEMA_NAME.equals(td.getSchemaName())) {

        }
        return null;
    }

    private SchemaDescriptor systemSchemaDesc;

    public SchemaDescriptor getSystemSchemaDescriptor()   {
        return systemSchemaDesc;
    }

    /**
     * 获取非核心表的信息
     *
     * @param catalogNumber
     */
    private TabInfoImpl getNonCoreTI(int catalogNumber)   {
        TabInfoImpl ti = getNonCoreTIByNumber(catalogNumber);
        faultInTabInfo(ti);
        return ti;
    }

    /**
     * 返回非核心系统目录的表信息
     */
    protected TabInfoImpl getNonCoreTIByNumber(int catalogNumber)   {

        int nonCoreNum = catalogNumber - NUM_CORE;

        TabInfoImpl retval = noncoreInfo[nonCoreNum];
        if (retval == null) {
            UUIDFactory luuidFactory = uuidFactory;
            switch (catalogNumber) {

            }

            noncoreInfo[nonCoreNum] = retval;
        }
        return retval;
    }

    private void faultInTabInfo(TabInfoImpl ti)   {
        //数字索引
        int numIndexes;


    }

    private SchemaDescriptor getSchemaDescriptorBody(UUID schemaId, int isolationLevel, TransactionManager tc)   {
        LanguageConnectionContext lcc = getLCC();
        SchemaDescriptor sd = lcc.getDefaultSchema();
        return sd;
    }

    private static LanguageConnectionContext getLCC() {
        return (LanguageConnectionContext) getContextOrNull(LanguageConnectionContext.CONTEXT_ID);
    }

    private static Context getContextOrNull(final String contextID) {
        return ContextService.getContextOrNull(contextID);
    }

    public int getCacheMode() {
        return cacheMode;
    }

    /**
     * 根据模式获取描述
     */
    @Override
    public SchemaDescriptor getSchemaDescriptor(String schemaName, TransactionManager tc, boolean raiseError)   {
        SchemaDescriptor sd = locateSchemaRow(schemaName, tc);
        return sd;
    }

    /**
     * 根据模式名获取对应的模式描述
     */
    private SchemaDescriptor locateSchemaRow(String schemaName, TransactionManager tc)   {
        DataValueDescriptor schemaNameOrderable;
        TabInfoImpl ti = coreInfo[SYSSCHEMAS_CORE_NUM];
        schemaNameOrderable = new SQLVarchar(schemaName);
        ExecIndexRow keyRow = new IndexRow(1);
        keyRow.setColumn(1, schemaNameOrderable);
        return getDescriptorViaIndex(SYSSCHEMASRowFactory.SYSSCHEMAS_INDEX1_ID,
                keyRow, ti, null, null, SchemaDescriptor.class, false);
    }


    @Override
    public Cacheable newCacheable(CacheManager cm) {
        return new TableDescriptorCacheable(this);
    }

    private TableDescriptor getTableDescriptorIndex1Scan(String tableName, String schemaUUID)   {
        DataValueDescriptor schemaIDOrderable;
        DataValueDescriptor tableNameOrderable;
        TableDescriptor td;
        TabInfoImpl ti = coreInfo[SYSTABLES_CORE_NUM];
        tableNameOrderable = new SQLVarchar(tableName);
        schemaIDOrderable = new SQLChar(schemaUUID);
        ExecIndexRow keyRow = new IndexRow(2);
        keyRow.setColumn(1, tableNameOrderable);
        keyRow.setColumn(2, schemaIDOrderable);
        //根据索引列来获取对应的表描述
        td = getDescriptorViaIndex(SYSTABLESRowFactory.SYSTABLES_INDEX1_ID, keyRow, ti, null, null, TableDescriptor.class, false);
        return finishTableDescriptor(td);
    }


    /**
     * 通过索引来获取描述
     */
    private <T extends TupleDescriptor> T getDescriptorViaIndex(int indexId,
                                                                ExecIndexRow keyRow, TabInfoImpl ti, TupleDescriptor parentTupleDescriptor,
                                                                List<? super T> list, Class<T> returnType, boolean forUpdate)   {
        TransactionManager tc = getTransactionCompile();
        return getDescriptorViaIndexMinion(indexId, keyRow, ti, parentTupleDescriptor, list, returnType, forUpdate, tc);
    }

    /**
     * 根据索引id获取对应的元组子类的描述
     */
    private <T extends TupleDescriptor> T getDescriptorViaIndexMinion(int indexId,
                                                                      ExecIndexRow keyRow, TabInfoImpl ti, TupleDescriptor parentTupleDescriptor, List<? super T> list, Class<T> returnType, boolean forUpdate, TransactionManager tc)   {
        CatalogRowFactory catalogRowFactory = ti.getCatalogRowFactory();
        ConglomerateController table;
        ExecIndexRow indexRow1;
        ExecRow outRow;
        TableRowLocation baseRowLocation;
        ScanController scanController;
        T td = null;
        outRow = catalogRowFactory.makeEmptyRow();
        table = tc.openConglomerate(ti.getHeapConglomerate(), false);
        //根据索引id找到其对应的ConglomerateNumber
        long indexConglomerateNumber = ti.getIndexConglomerate(indexId);
        int open_mode;
        if (forUpdate) {
            open_mode = TransactionManager.OPENMODE_FORUPDATE;
        } else {
            open_mode = 0;
        }
        // 打开扫描控制器
        scanController = tc.openScan(indexConglomerateNumber, false, open_mode, TransactionManager.MODE_RECORD, 0, null, keyRow.getRowArray(), ScanController.GE, null, keyRow.getRowArray(), ScanController.GT);

        while (true){
            //创建一个索引列的模板
            TableRowLocation tableRowLocation = new TableRowLocation();
            indexRow1 = getIndexRowFromHeapRow(ti.getIndexRowGenerator(indexId), tableRowLocation, outRow);
            boolean isFetch = scanController.fetchNext(indexRow1.getRowArray());
            if(!isFetch){
                break;
            }
            //根据索引获取行位置
            baseRowLocation = (TableRowLocation) indexRow1.getColumn(indexRow1.nColumns());
            table.fetch(baseRowLocation, outRow.getRowArray(), null);
            TupleDescriptor tupleDescriptor = catalogRowFactory.buildDescriptor(outRow, parentTupleDescriptor, this);
            td = returnType.cast(tupleDescriptor);
            if (list != null && td != null) {
                list.add(td);
            }
        }
        return td;
    }

    public static ExecIndexRow getIndexRowFromHeapRow(IndexRowGenerator irg, TableRowLocation rl, ExecRow heapRow)   {
        IndexDescriptor indexDescriptor = irg.getIndexDescriptor();
        int indexLength = indexDescriptor.baseColumnPositions().length + 1;
        ExecIndexRow indexRow = new IndexRow(indexLength);
        irg.getIndexRow(heapRow, rl, indexRow, null);
        return indexRow;
    }

    public TransactionManager getTransactionCompile()   {
        return transactionController;
    }

    private TableDescriptor finishTableDescriptor(TableDescriptor td)   {
        if (td != null) {
            synchronized (td) {
                getColumnDescriptorsScan(td);
                getConglomerateDescriptorsScan(td);
            }
        }
        return td;
    }

    private void getColumnDescriptorsScan(TableDescriptor td)   {
        getColumnDescriptorsScan(td.getUUID(), td.getColumnDescriptorList(), td);
    }


    /**
     * 主要是修改列描述的位置
     */
    private void getColumnDescriptorsScan(UUID uuid, ColumnDescriptorList columnDescriptorsParam, TupleDescriptor td)   {
        ColumnDescriptor cd;
        ColumnDescriptorList cdlCopy = new ColumnDescriptorList();
        DataValueDescriptor refIDOrderable = getIDValueAsCHAR(uuid);
        TabInfoImpl ti = coreInfo[SYSCOLUMNS_CORE_NUM];
        ExecIndexRow keyRow = new IndexRow(1);
        keyRow.setColumn(1, refIDOrderable);
        getDescriptorViaIndex(SYSCOLUMNSRowFactory.SYSCOLUMNS_INDEX1_ID,
                keyRow, ti, td, columnDescriptorsParam, ColumnDescriptor.class, false);

        /**
         * 将列描述里面的数据按照其位置重新添加到集合
         * */
        int cdlSize = columnDescriptorsParam.size();
        for (int index = 0; index < cdlSize; index++) {
            cdlCopy.add(columnDescriptorsParam.get(index));
        }
        for (int index = 0; index < cdlSize; index++) {
            cd = cdlCopy.elementAt(index);
            columnDescriptorsParam.set(cd.getPosition() - 1, cd);
        }
    }

    private void getConglomerateDescriptorsScan(TableDescriptor td)   {
        ConglomerateDescriptorList cdl = td.getConglomerateDescriptorList();
        ExecIndexRow keyRow3 = null;
        DataValueDescriptor tableIDOrderable;
        TabInfoImpl ti = coreInfo[SYSCONGLOMERATES_CORE_NUM];

        /* Use tableIDOrderable in both start and stop positions for scan */
        tableIDOrderable = getIDValueAsCHAR(td.getUUID());

        /* Set up the start/stop position for the scan */
        keyRow3 = new IndexRow(1);
        keyRow3.setColumn(1, tableIDOrderable);

        getDescriptorViaIndex(
                SYSCONGLOMERATESRowFactory.SYSCONGLOMERATES_INDEX3_ID,
                keyRow3,
                ti,
                null,
                cdl,
                ConglomerateDescriptor.class,
                false);
    }

    private static SQLChar getIDValueAsCHAR(UUID uuid) {
        String uuidString = uuid.toString();
        return new SQLChar(uuidString);
    }

    public TableDescriptor getUncachedTableDescriptor(TableKey tableKey)   {
        String tableName = tableKey.getTableName();
        UUID schemaId = tableKey.getSchemaId();
        String schemaIdString = Temp.schemaIdString;
        if (schemaId != null) {
            schemaIdString = schemaId.toString();
        }
        return getTableDescriptorIndex1Scan(tableName, schemaIdString);
    }


    /**
     * 所有的索引描述最终添加到CONGLOMERATE表中去
     */
    private void bootStrapSystemIndexes(SchemaDescriptor sd, TransactionManager tc, DataDescriptorGenerator ddg, TabInfoImpl ti)   {
        ConglomerateDescriptor[] cgd = new ConglomerateDescriptor[ti.getNumberOfIndexes()];
        for (int indexCtr = 0; indexCtr < ti.getNumberOfIndexes(); indexCtr++) {
            cgd[indexCtr] = bootstrapOneIndex(sd, tc, ddg, ti, indexCtr, ti.getHeapConglomerate());
        }
        for (int indexCtr = 0; indexCtr < ti.getNumberOfIndexes(); indexCtr++) {
            addDescriptor(cgd[indexCtr], sd, SYSCONGLOMERATES_CATALOG_NUM, false, tc);
        }
    }

    private ConglomerateDescriptor bootstrapOneIndex(SchemaDescriptor sd, TransactionManager tc,
                                                     DataDescriptorGenerator ddg, TabInfoImpl indexTableInfo, int indexNumber, long heapConglomerateNumber)   {
        boolean isUnique;
        ConglomerateController conglomerateController;
        ExecRow baseRow;
        ExecIndexRow indexableRow;
        int numColumns;
        long conglomId;
        TableRowLocation tableRowLocation;
        CatalogRowFactory catalogRowFactory = indexTableInfo.getCatalogRowFactory();
        IndexRowGenerator irg;
        ConglomerateDescriptor conglomerateDescriptor;
        //初始化表中当前索引的信息 如增长器
        initSystemIndexVariables(indexTableInfo, indexNumber);
        //获取索引行对应的增长器
        irg = indexTableInfo.getIndexRowGenerator(indexNumber);
        //获取当前索引的列数
        numColumns = indexTableInfo.getIndexColumnCount(indexNumber);
        isUnique = indexTableInfo.isIndexUnique(indexNumber);

        indexableRow = irg.getIndexRowTemplate();


        baseRow = catalogRowFactory.makeEmptyRow();
        conglomerateController = tc.openConglomerate(heapConglomerateNumber, false);
        tableRowLocation = conglomerateController.newRowLocationTemplate();
        conglomerateController.close();
        irg.getIndexRow(baseRow, tableRowLocation, indexableRow, null);


        Properties indexProperties = new Properties();
        indexProperties.put("baseConglomerateId", Long.toString(heapConglomerateNumber));
        indexProperties.put("nUniqueColumns", Integer.toString(isUnique ? numColumns : numColumns + 1));
        indexProperties.put("rowLocationColumn", Integer.toString(numColumns));
        indexProperties.put("nKeyFields", Integer.toString(numColumns + 1));

        //创建索引对应的Conglomerate
        conglomId = tc.createConglomerate("BTREE", indexableRow.getRowArray(), null, indexProperties);

        String indexName = catalogRowFactory.getIndexName(indexNumber);
        UUID canonicalTableUUID = catalogRowFactory.getCanonicalTableUUID();
        UUID canonicalIndexUUID = catalogRowFactory.getCanonicalIndexUUID(indexNumber);
        UUID schemaId = sd.getUUID();
        conglomerateDescriptor = ddg.newConglomerateDescriptor(conglomId,
                indexName, true, irg, false,
                canonicalIndexUUID, canonicalTableUUID, schemaId);
        indexTableInfo.setIndexConglomerate(conglomerateDescriptor);
        return conglomerateDescriptor;
    }

    /**
     * 获取当前索引 可能是联合索引
     * 创建索引增长器 将其注入到索引对应的位置
     */
    private void initSystemIndexVariables(TabInfoImpl ti, int indexNumber)   {
        //获取当前索引号对应的索引列,因为可能是联合索引
        int numCols = ti.getIndexColumnCount(indexNumber);
        int[] baseColumnPositions = new int[numCols];
        /**获取每一个索引的列位置*/
        for (int colCtr = 0; colCtr < numCols; colCtr++) {
            baseColumnPositions[colCtr] = ti.getBaseColumnPosition(indexNumber, colCtr);
        }
        boolean[] isAscending = new boolean[baseColumnPositions.length];
        for (int i = 0; i < baseColumnPositions.length; i++) {
            isAscending[i] = true;
        }

        IndexRowGenerator irg = null;
        //创建索引增长器
        irg = new IndexRowGenerator(
                "BTREE", ti.isIndexUnique(indexNumber),
                false,
                false,
                false,
                baseColumnPositions,
                isAscending,
                baseColumnPositions.length);

        // For now, assume that all index columns are ordered columns
        ti.setIndexRowGenerator(indexNumber, irg);
    }


    private SchemaDescriptor newDeclaredGlobalTemporaryTablesSchemaDesc(String name) {
        return new SchemaDescriptor(this, name, Temp.sessionId, null, false);
    }

    private SchemaDescriptor newSystemSchemaDesc(String name, String uuid) {
        return new SchemaDescriptor(this, name, Temp.sessionId, uuidFactory.recreateUUID(uuid), true);
    }

    public void loadDictionaryTables(TransactionManager tc)   {
        loadCatalogs(coreInfo);
    }

    private void loadCatalogs(TabInfoImpl[] catalogArray)   {
        int ictr;
        int numIndexes;
        int indexCtr;
        TabInfoImpl catalog;
        int catalogCount = catalogArray.length;
        for (ictr = 0; ictr < catalogCount; ictr++) {
            catalog = catalogArray[ictr];
            numIndexes = catalog.getNumberOfIndexes();
            for (indexCtr = 0; indexCtr < numIndexes; indexCtr++) {
                initSystemIndexVariables(catalog, indexCtr);
            }
        }
    }


    public void finished() {
        nameTdCache.cleanAll();


    }
}
