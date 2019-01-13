package com.cfs.sqlkv.jdbc;

import com.cfs.sqlkv.container.Monitor;

import java.util.Enumeration;
import java.util.Properties;

/**
 * @author zhengxiaokang
 * @Description
 * @Email zheng.xiaokang@qq.com
 * @create 2019-01-06 09:45
 */
public class JDBC {

    private Properties properties = new Properties();

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * 如果内联的Driver尚未被激活则进行相应初始化激活
     * */
    public void init(String protocol){
        addProperty("jdbc", InternalDriver.class.getName());
        if(InternalDriver.activeDriver()==null){
            init(properties);
        }
    }

    /**
     * 初始化监听器
     * */
    private void init(final Properties props){
        Monitor monitor = new Monitor();
        monitor.init(props);
    }
}
