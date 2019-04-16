package com.cfs.sqlkv.sql.dictionary;

import com.cfs.sqlkv.common.UUID;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-04-01 20:46
 */
public class ConglomerateDescriptorList extends ArrayList<ConglomerateDescriptor> {

    /**
     * 通过指定的conglomerateNumber获取相应的描述
     */
    public ConglomerateDescriptor getConglomerateDescriptor(long conglomerateNumber) {
        ConglomerateDescriptor returnValue = null;
        for (ConglomerateDescriptor conglomerateDescriptor : this) {
            if (conglomerateNumber == conglomerateDescriptor.getConglomerateNumber()) {
                returnValue = conglomerateDescriptor;
                break;
            }
        }
        return returnValue;
    }

    public ConglomerateDescriptor[] getConglomerateDescriptors(long conglomerateNumber) {
        int size = size(), j = 0;
        ConglomerateDescriptor[] draft = new ConglomerateDescriptor[size];
        for (ConglomerateDescriptor conglomerateDescriptor : this) {
            if (conglomerateNumber == conglomerateDescriptor.getConglomerateNumber()) {
                draft[j++] = conglomerateDescriptor;
            }
        }
        if (j == size) {
            return draft;
        }
        return Arrays.copyOf(draft, j);
    }

    public ConglomerateDescriptor getConglomerateDescriptor(String conglomerateName) {
        ConglomerateDescriptor returnValue = null;

        for (ConglomerateDescriptor conglomerateDescriptor : this) {
            if (conglomerateName.equals(conglomerateDescriptor.getConglomerateName())) {
                returnValue = conglomerateDescriptor;
                break;
            }
        }

        return returnValue;
    }

    public ConglomerateDescriptor getConglomerateDescriptor(UUID uuid)   {
        ConglomerateDescriptor returnValue = null;

        for (ConglomerateDescriptor conglomerateDescriptor : this) {
            if (uuid.equals(conglomerateDescriptor.getUUID())) {
                returnValue = conglomerateDescriptor;
                break;
            }
        }

        return returnValue;
    }

    public ConglomerateDescriptor[] getConglomerateDescriptors(UUID uuid) {
        int size = size(), j = 0;
        ConglomerateDescriptor[] draft = new ConglomerateDescriptor[size];

        for (ConglomerateDescriptor conglomerateDescriptor : this) {
            if (uuid.equals(conglomerateDescriptor.getUUID()))
                draft[j++] = conglomerateDescriptor;
        }

        if (j == size)
            return draft;

        return Arrays.copyOf(draft, j);
    }

    public void dropConglomerateDescriptor(UUID tableID, ConglomerateDescriptor cgDesc)
              {
        Iterator<ConglomerateDescriptor> iterator = iterator();
        while (iterator.hasNext()) {
            ConglomerateDescriptor localCgDesc = iterator.next();
            if (localCgDesc.getConglomerateNumber() == cgDesc.getConglomerateNumber() &&
                    localCgDesc.getConglomerateName().equals(cgDesc.getConglomerateName()) &&
                    localCgDesc.getSchemaID().equals(cgDesc.getSchemaID())) {
                iterator.remove();
                break;
            }
        }
    }

    public void dropConglomerateDescriptorByUUID(UUID conglomerateID)
              {
        Iterator<ConglomerateDescriptor> iterator = iterator();
        while (iterator.hasNext()) {
            ConglomerateDescriptor localCgDesc = iterator.next();
            if (conglomerateID.equals(localCgDesc.getUUID())) {
                iterator.remove();
                break;
            }
        }
    }
}
