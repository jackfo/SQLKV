package com.cfs.sqlkv.engine.execute;

import com.cfs.sqlkv.catalog.types.DataTypeDescriptor;

import com.cfs.sqlkv.row.ExecRow;
import com.cfs.sqlkv.row.IndexRow;
import com.cfs.sqlkv.row.ValueRow;
import com.cfs.sqlkv.service.io.ArrayUtil;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.type.DataValueDescriptor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-02-28 12:20
 */
public class ExecRowBuilder implements Formatable {

    /**
     * 判断是否是索引
     */
    private boolean indexable;

    /**
     * 构建空值的模板
     */
    private Object[] template;


    private int[] columns;

    /**
     * 行列的数量
     */
    private int count;

    /**
     * 当前行最高的行号
     */
    private int maxColumnNumber;

    public ExecRowBuilder(int size, boolean indexable) {
        this.template = new Object[size];
        this.columns = new int[size];
        this.indexable = indexable;
    }

    public ExecRowBuilder() {
    }

    @Override
    public int getTypeFormatId() {
        return StoredFormatIds.EXEC_ROW_BUILDER_ID;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(indexable);
        ArrayUtil.writeArray(out, template);
        out.writeObject(columns);
        out.writeInt(count);
        out.writeInt(maxColumnNumber);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        indexable = in.readBoolean();
        template = ArrayUtil.readObjectArray(in);
        columns = (int[]) in.readObject();
        count = in.readInt();
        maxColumnNumber = in.readInt();
    }

    /**
     * 设置模板列,并针对每一列设置对应空值
     */
    public ExecRow build() {
        ExecRow row = indexable ?
                new IndexRow(maxColumnNumber) : new ValueRow(maxColumnNumber);

        for (int i = 0; i < count; i++) {
            Object o = template[i];
            DataValueDescriptor dvd = (o instanceof DataValueDescriptor) ?
                    ((DataValueDescriptor) o).getNewNull() :
                    ((DataTypeDescriptor) o).getNull();
            row.setColumn(columns[i], dvd);
        }

        return row;
    }

    public void setColumn(int column, Object columnTemplate) {
        template[count] = columnTemplate;
        columns[count] = column;
        count++;
        maxColumnNumber = Math.max(maxColumnNumber, column);
    }
}
