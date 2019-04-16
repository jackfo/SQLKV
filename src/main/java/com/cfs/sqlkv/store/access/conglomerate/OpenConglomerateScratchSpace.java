package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.engine.execute.RowUtil;

import com.cfs.sqlkv.transaction.Transaction;
import com.cfs.sqlkv.type.DataValueDescriptor;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-31 20:18
 */
public class OpenConglomerateScratchSpace {

    private RowPosition scratch_row_position;

    private DataValueDescriptor[] scratch_row;

    /**
     * 返回行对应的模板
     * */
    private DataValueDescriptor[] row_for_export_template;


    public OpenConglomerateScratchSpace(int[] format_ids){
        this.format_ids = format_ids;
    }

    /**
     * 获取行位置
     * */
    public RowPosition get_scratch_row_position(){
        if (scratch_row_position == null) {
            scratch_row_position = new RowPosition();
        }
        return scratch_row_position;
    }

    public DataValueDescriptor[] get_scratch_row(Transaction raw_transaction){
        if (scratch_row == null) {
            scratch_row = get_row_for_export(raw_transaction);
        }
        return scratch_row;
    }

    private final int[] format_ids;

    public DataValueDescriptor[] get_row_for_export(Transaction raw_transaction){
        if (row_for_export_template == null){
            row_for_export_template = RowUtil.newTemplate(raw_transaction.getDataValueFactory(), null, format_ids);
        }
        return RowUtil.newRowFromTemplate(row_for_export_template);
    }

    private DataValueDescriptor[]   scratch_template;
    public DataValueDescriptor[] get_template(Transaction rawtran)   {
        if (scratch_template == null) {
            scratch_template = TemplateRow.newRow(rawtran, null, format_ids);
        }
        return scratch_template;
    }
}
