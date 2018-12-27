package com.cfs.sqlkv.catalog.types;

/**
 * @author zhengxiaokang
 * @Description TypeId描述SQL类型的静态信息
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-26 16:47
 */
public class TypeId {

    public static final String  VARCHAR_NAME = "VARCHAR";

    private BaseTypeIdImpl baseTypeId;
    private int  formatId;



    /**java类型名*/
    private String javaTypeName;

    public TypeId(int formatId, BaseTypeIdImpl baseTypeId) {
        this.formatId = formatId;
        this.baseTypeId = baseTypeId;
        setTypeIdSpecificInstanceVariables();
    }

    private void setTypeIdSpecificInstanceVariables(){
        switch (formatId){

        }
    }
    /**
     * @param typeFormatId
     * @param implTypeFormatId
     * @return
     * */
    private static TypeId create(int typeFormatId, int implTypeFormatId) {
        return new TypeId(typeFormatId, new BaseTypeIdImpl(implTypeFormatId));
    }


    public static final TypeId INTEGER_ID = create(0,0);
}
