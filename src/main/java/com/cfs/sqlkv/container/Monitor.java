package com.cfs.sqlkv.container;

import com.cfs.sqlkv.common.context.ContextService;

import java.util.Enumeration;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 10:05
 */
public class Monitor {

    public  Properties bootProperties;
    public  Properties applicationProperties;

    private ContextService contextService;

    public  final void init(Properties properties){

        bootProperties = properties;

        initServices(bootProperties);
    }

    public void initServices(Properties properties) {
        if (properties == null){
            return;
        }
        ContextService contextService = new ContextService();
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();){
            String key = (String) e.nextElement();
            String value = (String) properties.get(key);
            try {
                Class.forName(value).newInstance();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

}
