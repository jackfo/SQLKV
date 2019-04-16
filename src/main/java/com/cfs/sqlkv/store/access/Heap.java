package com.cfs.sqlkv.store.access;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.io.storage.Storable;
import com.cfs.sqlkv.service.io.ArrayInputStream;
import com.cfs.sqlkv.service.io.FormatIdUtil;
import com.cfs.sqlkv.service.io.FormatableBitSet;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.store.TransactionManager;
import com.cfs.sqlkv.store.access.conglomerate.*;
import com.cfs.sqlkv.store.access.heap.OpenTable;
import com.cfs.sqlkv.store.access.heap.TableController;
import com.cfs.sqlkv.store.access.heap.TableScan;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 13:43
 */
public class Heap extends GenericConglomerate {

    public Heap() {
    }

    private ContainerKey containerKey;

    protected int conglom_format_id;

    /**
     * 添加列
     * TODO:待实现
     */
    public void addColumn(TransactionManager xact_manager, int column_id, Storable template_column, int collation_id) {


    }

    public void drop(TransactionManager xact_manager) {

    }

    @Override
    public long getContainerid() {
        return id.getContainerId();
    }

    @Override
    public ContainerKey getId() {
        return id;
    }

    /**
     * 创建表并获取表的控制器
     */
    @Override
    public ConglomerateController open(TransactionManager xact_manager, Transaction rawtran) {
        OpenTable openTable = new OpenTable();
        if (openTable.init(this, null, rawtran) == null) {
            throw new RuntimeException(String.format("Container %d not found.", id.getContainerId()));
        }
        TableController tableController = new TableController();
        tableController.init(openTable);
        return tableController;
    }


    private ContainerKey id;

    /**
     * 表所有列的格式ID
     */
    int[] format_ids;

    public void create(Transaction rawtran, int segmentId, long input_containerid, DataValueDescriptor[] template, int conglom_format_id) {

        /**
         * 添加容器,这个时候容器中已经封装了锁、分配行为和页面行为等
         * */
        long containerid = rawtran.addContainer(segmentId, input_containerid);
        if (containerid < 0) {
            throw new RuntimeException("分配失败");
        }
        //创建当前段对应的容器对应的标识
        id = new ContainerKey(segmentId, containerid);
        BaseContainerHandle container = null;
        Page page = null;
        this.format_ids = ConglomerateUtil.createFormatIds(template);
        this.conglom_format_id = conglom_format_id;
        try {
            //打开容器
            container = rawtran.openContainer(id);
            DataValueDescriptor[] control_row = new DataValueDescriptor[1];
            control_row[0] = this;
            //获取容器第一页
            page = container.getPage(BaseContainerHandle.FIRST_PAGE_NUMBER);
            //插入第一个槽位
            page.insertAtSlot(Page.FIRST_SLOT_NUMBER, control_row, null, null, Page.INSERT_OVERFLOW, AccessFactoryGlobals.HEAP_OVERFLOW_THRESHOLD);
            page.unlatch();
            page = null;
            container.setEstimatedRowCount(0, 0);
        } finally {
            if (container != null) {
                container.close();
            }
            if (page != null) {
                page.unlatch();
            }
        }

    }


    /**
     * 是否是临时
     *
     * @return whether conglomerate is temporary or not.
     **/
    public boolean isTemporary() {
        return (id.getSegmentId() == BaseContainerHandle.TEMPORARY_SEGMENT);
    }

    /**
     * 如果有的列的字符规则不是COLLATION_TYPE_UCS_BASIC则返回真
     */
    public static boolean hasCollatedColumns(int[] collationIds) {
        for (int i = 0; i < collationIds.length; i++) {
            if (collationIds[i] != StringDataValue.COLLATION_TYPE_UCS_BASIC) {
                return true;
            }
        }
        return false;
    }

    public int getTypeFormatId() {
        return StoredFormatIds.ACCESS_HEAP_V3_ID;
    }

    @Override
    public ScanManager openScan(TransactionManager transactionManager, Transaction raw_transaction, DataValueDescriptor[] startKeyValue, int startSearchOperator, Qualifier qualifier[][], DataValueDescriptor[] stopKeyValue, int stopSearchOperator) {

        if (!RowUtil.isRowEmpty(startKeyValue) || !RowUtil.isRowEmpty(stopKeyValue)) {
            throw new RuntimeException("The feature is not implemented");
        }
        //构建打开表,并初始化相应的属性,获取容器句柄
        OpenTable open_table = new OpenTable();
        BaseContainerHandle baseContainerHandle = open_table.init(this, null, raw_transaction);

        if (baseContainerHandle == null) {
            throw new RuntimeException(String.format("Container s% not found", containerKey.getContainerId()));
        }

        TableScan tableScan = new TableScan();
        tableScan.init(open_table,qualifier);
        return tableScan;

    }

    /**
     * 读取conglom的格式和标识
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        conglom_format_id = FormatIdUtil.readFormatIdInteger(in);
        int segmentid = in.readInt();
        long containerid = in.readLong();
        id = new ContainerKey(segmentid, containerid);
        int num_columns = in.readInt();
        format_ids = ConglomerateUtil.readFormatIdArray(num_columns, in);
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        FormatIdUtil.writeFormatIdInteger(out, conglom_format_id);
        out.writeInt((int) id.getSegmentId());
        out.writeLong(id.getContainerId());
        out.writeInt(format_ids.length);
        ConglomerateUtil.writeFormatIdArray(format_ids, out);
    }

    @Override
    public boolean isNull() {
        return id == null;
    }

    @Override
    public Object getObject() {
        return null;
    }


    @Override
    public boolean getBoolean() {
        return false;
    }

    @Override
    public void restoreToNull() {
        id = null;
    }


    public OpenConglomerateScratchSpace getDynamicCompiledConglomInfo() {
        return new OpenConglomerateScratchSpace(format_ids);
    }

}
