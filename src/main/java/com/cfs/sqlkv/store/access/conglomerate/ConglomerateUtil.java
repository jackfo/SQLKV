package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.service.io.FormatIdUtil;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 20:17
 */
public class ConglomerateUtil {


    /**
     * 根据模板数据描述构建对应的格式ID数组
     *
     * @param template 模板数据描述
     * @return 对应格式ID数组
     */
    public static int[] createFormatIds(DataValueDescriptor[] template) {
        int[] format_ids = new int[template.length];
        for (int i = 0; i < template.length; i++) {
            format_ids[i] = template[i].getTypeFormatId();
        }
        return (format_ids);
    }

    /**
     * 创建编码类型
     * 如果collationIds不为空则直接copy
     * 如果为空则设置默认编码COLLATION_TYPE_UCS_BASIC
     */
    public static int[] createCollationIds(int sizeof_ids, int[] collationIds) {
        int[] collation_ids = new int[sizeof_ids];
        if (collationIds != null) {
            System.arraycopy(collationIds, 0, collation_ids, 0, collationIds.length);
        } else {
            for (int i = 0; i < collation_ids.length; i++) {
                collation_ids[i] = StringDataValue.COLLATION_TYPE_UCS_BASIC;
            }
        }
        return (collation_ids);
    }

    public static int[] readFormatIdArray(
            int num,
            ObjectInput in)
            throws IOException {
        // read in the array of format id's

        int[] format_ids = new int[num];
        for (int i = 0; i < num; i++) {
            format_ids[i] = FormatIdUtil.readFormatIdInteger(in);
        }

        return (format_ids);
    }

    public static void writeFormatIdArray(int[] format_id_array, ObjectOutput out) throws IOException {
        for (int i = 0; i < format_id_array.length; i++) {
            FormatIdUtil.writeFormatIdInteger(out, format_id_array[i]);
        }
    }




}
