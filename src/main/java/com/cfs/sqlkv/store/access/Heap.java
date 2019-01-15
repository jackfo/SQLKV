package com.cfs.sqlkv.store.access;

import com.cfs.sqlkv.column.ColumnOrdering;
import com.cfs.sqlkv.exception.StandardException;
import com.cfs.sqlkv.io.FormatableBitSet;
import com.cfs.sqlkv.io.Storable;
import com.cfs.sqlkv.store.access.conglomerate.ConglomerateUtil;
import com.cfs.sqlkv.store.access.conglomerate.GenericConglomerate;
import com.cfs.sqlkv.store.access.conglomerate.LogicalUndo;
import com.cfs.sqlkv.store.access.conglomerate.TransactionManager;
import com.cfs.sqlkv.store.access.raw.ContainerKey;
import com.cfs.sqlkv.store.access.raw.LockingPolicy;
import com.cfs.sqlkv.store.access.raw.data.BaseContainerHandle;
import com.cfs.sqlkv.store.access.raw.data.Page;
import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-09 13:43
 */
public class Heap extends GenericConglomerate {

    public Heap(){}

    protected int conglom_format_id;
    @Override
    public void addColumn(TransactionManager xact_manager, int column_id, Storable template_column, int collation_id) throws StandardException {

    }

    @Override
    public void drop(TransactionManager xact_manager) throws StandardException {

    }

    @Override
    public long getContainerid() {
        return 0;
    }

    private ContainerKey id;

    /**表所有列的格式ID*/
    int[] format_ids;

    /**表所有列的编码ID*/
    protected int[] collation_ids;

    private boolean hasCollatedTypes;

    /**
     *
     * Create a container for the heap table with default minimumRecordSize to be at least
     * 		// MINIMUM_RECORD_SIZE_DEFAULT (12),
     * 		// to guarantee there is enough room for updates
     * 		// of the row.
     * 		// Here we only take care of the case that
     * 		// that the properties are set with the create
     * 		// statement.  For the case when properties are
     * 		// not set with the create statement, it is taken
     * 		// care of in fileContainer.java: createInfoFromProp().
     * */
    public void create(
            Transaction             rawtran,
            int                     segmentId,
            long                    input_containerid,
            DataValueDescriptor[]   template,
            ColumnOrdering[]        columnOrder,
            int[]                   collationIds,
            Properties properties,
            int                     conglom_format_id,
            int                     tmpFlag) throws StandardException {

        if(properties!=null){

        }

        /**
         * 添加容器,这个时候容器中已经封装了锁、分配行为和页面行为等
         * 页面已经分配完毕
         * */
        long containerid = rawtran.addContainer(segmentId, input_containerid, BaseContainerHandle.MODE_DEFAULT, properties, tmpFlag);

        if (containerid < 0) {
            throw new RuntimeException("分配失败");
        }
        //创建当前段对应的容器对应的标识
        id = new ContainerKey(segmentId, containerid);


        this.format_ids = ConglomerateUtil.createFormatIds(template);


        this.conglom_format_id = conglom_format_id;

        collation_ids = ConglomerateUtil.createCollationIds(format_ids.length, collationIds);

        hasCollatedTypes = hasCollatedColumns(collation_ids);


        BaseContainerHandle container = null;
        Page page = null;

        try{
            //打开容器
            container = rawtran.openContainer(id,(LockingPolicy)null,BaseContainerHandle.MODE_FORUPDATE |(isTemporary() ? BaseContainerHandle.MODE_TEMP_IS_KEPT : 0) );
            DataValueDescriptor[] control_row = new DataValueDescriptor[1];
            control_row[0] = this;
            //获取容器第一页
            page = container.getPage(BaseContainerHandle.FIRST_PAGE_NUMBER);
            //插入第一个槽位
            page.insertAtSlot(Page.FIRST_SLOT_NUMBER, control_row, (FormatableBitSet) null, (LogicalUndo) null, Page.INSERT_OVERFLOW, AccessFactoryGlobals.HEAP_OVERFLOW_THRESHOLD);
            page.unlatch();
            page = null;
            container.setEstimatedRowCount(0,0);
        }finally{
            if (container != null){
                container.close();
            }
            if (page !=null){
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
        return(id.getSegmentId() == BaseContainerHandle.TEMPORARY_SEGMENT);
    }

    /**
     * 如果有的列的字符规则不是COLLATION_TYPE_UCS_BASIC则返回真
     * */
    public static boolean hasCollatedColumns(int[] collationIds) {
        for (int i=0; i < collationIds.length; i++) {
            if (collationIds[i] != StringDataValue.COLLATION_TYPE_UCS_BASIC) {
                return true;
            }
        }
        return false;
    }
}
