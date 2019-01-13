package com.cfs.sqlkv.store.access.conglomerate;

import com.cfs.sqlkv.io.Formatable;
import com.cfs.sqlkv.type.DataValueDescriptor;
import com.cfs.sqlkv.type.StringDataValue;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-12 20:17
 */
public class ConglomerateUtil {


    /**
     * 根据模板数据描述构建对应的格式ID数组
     * @param template 模板数据描述
     * @return 对应格式ID数组
     * */
    public static int[] createFormatIds(DataValueDescriptor[] template){
        int[] format_ids = new int[template.length];
        for (int i = 0; i < template.length; i++){
            format_ids[i] = ((Formatable) template[i]).getTypeFormatId();
        }
        return(format_ids);
    }

    /**
     *创建编码类型
     * 如果collationIds不为空则直接copy
     * 如果为空则设置默认编码COLLATION_TYPE_UCS_BASIC
     * */
    public static int[] createCollationIds(int sizeof_ids,int[] collationIds){
        int[] collation_ids = new int[sizeof_ids];
        if (collationIds != null){
            System.arraycopy(collationIds, 0, collation_ids, 0, collationIds.length);
        }else{
            for (int i = 0; i < collation_ids.length; i++) {
                collation_ids[i] = StringDataValue.COLLATION_TYPE_UCS_BASIC;
            }
        }
        return(collation_ids);
    }


}
