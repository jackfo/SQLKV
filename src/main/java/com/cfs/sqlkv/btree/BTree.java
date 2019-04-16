package com.cfs.sqlkv.btree;

import com.cfs.sqlkv.btree.controlrow.LeafControlRow;
import com.cfs.sqlkv.column.ColumnOrdering;

import com.cfs.sqlkv.io.storage.Storable;
import com.cfs.sqlkv.service.io.FormatIdUtil;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.ConglomerateController;
import com.cfs.sqlkv.store.access.Qualifier;
import com.cfs.sqlkv.store.access.conglomerate.*;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-02 15:47
 */
public class BTree extends GenericConglomerate {
    static final int FORMAT_NUMBER = StoredFormatIds.ACCESS_B2I_V5_ID;

    public static final long ROOTPAGEID = BaseContainerHandle.FIRST_PAGE_NUMBER;


    private static final String PROPERTY_BASECONGLOMID = "baseConglomerateId";
    private static final String PROPERTY_ROWLOCCOLUMN = "rowLocationColumn";
    public static final String PROPERTY_NKEYFIELDS = "nKeyFields";
    public static final String PROPERTY_NUNIQUECOLUMNS = "nUniqueColumns";
    public static final String PROPERTY_UNIQUE_WITH_DUPLICATE_NULLS = "uniqueWithDuplicateNulls";


    public ContainerKey id;
    public int nKeyFields;
    public int nUniqueColumns;
    public static int maxRowsPerPage = Integer.MAX_VALUE;

    public int conglom_format_id;

    public int[] format_ids;

    public long baseConglomerateId;

    public boolean[] ascDescInfo;
    int rowLocationColumn;


    @Override
    public void addColumn(TransactionManager xact_manager, int column_id, Storable template_column, int collation_id)   {
        throw new RuntimeException("Unimplemented feature");
    }

    @Override
    public void drop(TransactionManager xact_manager)   {

    }

    @Override
    public long getContainerid() {
        return id.getContainerId();
    }

    @Override
    public ContainerKey getId() {
        return id;
    }

    @Override
    public ConglomerateController open(TransactionManager transactionManager, Transaction raw_transaction)   {
        BTreeController btreeController = new BTreeController();
        btreeController.init(transactionManager, raw_transaction, this);
        return btreeController;
    }

    /**
     * 打开BTree的扫描器
     */
    @Override
    public ScanManager openScan(TransactionManager transactionManager, Transaction raw_transaction, DataValueDescriptor[] startKeyValue, int startSearchOperator, Qualifier qualifier[][], DataValueDescriptor[] stopKeyValue, int stopSearchOperator)   {
        BTreeScan bTreeScan = new BTreeScan();
        bTreeScan.init(transactionManager, raw_transaction, null, startKeyValue, startSearchOperator, qualifier,stopKeyValue,stopSearchOperator, this);
        return bTreeScan;
    }

    @Override
    public OpenConglomerateScratchSpace getDynamicCompiledConglomInfo()   {
        return new OpenConglomerateScratchSpace(format_ids);
    }


    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.ACCESS_B2I_V5_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        FormatIdUtil.writeFormatIdInteger(out, conglom_format_id);

        out.writeLong(id.getContainerId());
        out.writeInt((int) id.getSegmentId());
        out.writeInt((nKeyFields));
        out.writeInt((nUniqueColumns));

        ConglomerateUtil.writeFormatIdArray(format_ids, out);
        out.writeLong(baseConglomerateId);
        out.writeInt(rowLocationColumn);
        FormatableBitSet ascDescBits = new FormatableBitSet(ascDescInfo.length);
        for (int i = 0; i < ascDescInfo.length; i++) {
            if (ascDescInfo[i]) {
                ascDescBits.set(i);
            }
        }
        ascDescBits.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        conglom_format_id = FormatIdUtil.readFormatIdInteger(in);
        long containerid = in.readLong();
        int segmentid = in.readInt();
        nKeyFields = in.readInt();
        nUniqueColumns = in.readInt();
        format_ids = ConglomerateUtil.readFormatIdArray(this.nKeyFields, in);
        id = new ContainerKey(segmentid, containerid);
        baseConglomerateId = in.readLong();
        rowLocationColumn = in.readInt();
        FormatableBitSet ascDescBits = new FormatableBitSet();
        ascDescBits.readExternal(in);
        ascDescInfo = new boolean[ascDescBits.getLength()];
        for (int i = 0; i < ascDescBits.getLength(); i++) {
            ascDescInfo[i] = ascDescBits.isSet(i);
        }

    }

    final public boolean isUnique() {
        return (nKeyFields != nUniqueColumns);
    }

    @Override
    public boolean isNull() {
        return id == null;
    }

    @Override
    public void restoreToNull() {
        id = null;
    }

    public void create(TransactionManager xact_manager, int segmentId, long input_conglomid, DataValueDescriptor[] template, ColumnOrdering[] columnOrder, Properties properties)   {
        String property_value;
        Transaction rawtran = xact_manager.getRawStoreFactoryTransaction();
        property_value = properties.getProperty(PROPERTY_BASECONGLOMID);
        baseConglomerateId = Long.parseLong(property_value);


        property_value = properties.getProperty(PROPERTY_ROWLOCCOLUMN);
        rowLocationColumn = Integer.parseInt(property_value);

        property_value = properties.getProperty(PROPERTY_NKEYFIELDS);
        nKeyFields = Integer.parseInt(property_value);

        property_value = properties.getProperty(PROPERTY_NUNIQUECOLUMNS);
        nUniqueColumns = Integer.parseInt(property_value);
        format_ids = ConglomerateUtil.createFormatIds(template);

        ascDescInfo = new boolean[template.length];
        for (int i = 0; i < ascDescInfo.length; i++) {
            if (columnOrder != null && i < columnOrder.length) {
                ascDescInfo[i] = columnOrder[i].getIsAscending();
            } else {
                ascDescInfo[i] = true;
            }
        }

        long containerid = rawtran.addContainer(segmentId, input_conglomid);

        id = new ContainerKey(segmentId, containerid);

        ConglomerateController base_cc = xact_manager.openConglomerate(baseConglomerateId, false);

        OpenBTree open_btree = new OpenBTree();


        open_btree.init(xact_manager, null, rawtran, this);

        // Open the newly created container, and insert the first control row.
        LeafControlRow.initEmptyBtree(open_btree);

        open_btree.close();

        base_cc.close();
    }


    final public DataValueDescriptor[] createTemplate(Transaction rawtran)   {
        return TemplateRow.newRow(rawtran, null, format_ids);
    }

    final DataValueDescriptor[] createBranchTemplate(Transaction rawtran, DataValueDescriptor page_ptr)   {
        return TemplateRow.newBranchRow(rawtran, format_ids, page_ptr);
    }


}
