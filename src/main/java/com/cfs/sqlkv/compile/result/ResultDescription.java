package com.cfs.sqlkv.compile.result;

import com.cfs.sqlkv.io.ArrayUtil;
import com.cfs.sqlkv.service.io.Formatable;
import com.cfs.sqlkv.service.io.StoredFormatIds;
import com.cfs.sqlkv.util.StringUtil;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-29 18:52
 */
public class ResultDescription implements Formatable{

    private ResultColumnDescriptor[] columns;
    private String statementType;

    private transient ResultSetMetaData metaData;

    /**key为列名 value为列是第几个字段*/
    private Map<String,Integer> columnNameMap;

    public ResultDescription(){}

    public ResultDescription(ResultColumnDescriptor[] columns, String statementType){
        this.columns = ArrayUtil.copy(columns);
        this.statementType = statementType;
    }

    public ResultDescription(ResultDescription rd, int[] theCols){
        this.columns = new ResultColumnDescriptor[theCols.length];
        for (int i = 0; i < theCols.length; i++) {
            columns[i] = rd.getColumnDescriptor(theCols[i]);
        }
        this.statementType = rd.getStatementType();
    }

    /**
     * 根据指定位置获取其列描述
     * */
    public ResultColumnDescriptor getColumnDescriptor(int position) {
        return columns[position-1];
    }
    /**
     * @return	返回statement的类型
     */
    public String	getStatementType() {
        return statementType;
    }

    public int	getColumnCount() {
        return (columns == null) ? 0 : columns.length;
    }

    public ResultColumnDescriptor[] getColumnInfo() {
        return ArrayUtil.copy(columns);
    }

    public  ResultColumnDescriptor getColumnInfo( int idx ) {
        return columns[ idx ];
    }

    /**
     * 构造一个删减的结果列描述
     * 比如从5移除 则5以后都删除
     * @param truncateFrom the starting column to remove
     *
     * @return a new ResultDescription
     */
    public ResultDescription truncateColumns(int truncateFrom) {
        ResultColumnDescriptor[] newColumns = new ResultColumnDescriptor[truncateFrom-1];
        System.arraycopy(columns, 0, newColumns, 0, newColumns.length);
        return new ResultDescription(newColumns, statementType);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException{
        int len = (columns == null) ? 0 : columns.length;
        out.writeObject(statementType);
        out.writeInt(len);
        while(len-- > 0){
            if (!(columns[len] instanceof GenericColumnDescriptor)) {
                columns[len] = new GenericColumnDescriptor(columns[len]);
            }
            out.writeObject(columns[len]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        int len;
        columns = null;
        statementType = (String)in.readObject();
        len = in.readInt();
        if (len > 0) {
            columns = new GenericColumnDescriptor[len];
            while(len-- > 0) {
                columns[len] = (ResultColumnDescriptor)in.readObject();
            }
        }
    }


    @Override
    public	int getTypeFormatId(){
        return StoredFormatIds.GENERIC_RESULT_DESCRIPTION_V01_ID;
    }

    public synchronized void setMetaData(ResultSetMetaData rsmd) {
        if (metaData == null){
            metaData = rsmd;
        }
    }

    public synchronized ResultSetMetaData getMetaData() {
        return metaData;
    }

    public int findColumnInsenstive(String columnName) {
        final Map<String,Integer> workMap;
        synchronized (this) {
            if (columnNameMap==null) {
                Map<String,Integer> map = new HashMap<String,Integer>();
                for (int i = getColumnCount(); i>=1; i--) {
                    final String key = StringUtil.SQLToUpperCase(getColumnDescriptor(i).getName());
                    final Integer value = i;
                    map.put(key, value);
                }
                columnNameMap = Collections.unmodifiableMap(map);
            }
            workMap = columnNameMap;
        }
        Integer val = (Integer) workMap.get(columnName);
        if (val==null) {
            val = (Integer) workMap.get(StringUtil.SQLToUpperCase(columnName));
        }
        if (val==null) {
            return -1;
        } else {
            return val.intValue();
        }
    }

}
