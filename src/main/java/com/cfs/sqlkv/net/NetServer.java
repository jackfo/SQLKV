package com.cfs.sqlkv.net;

import com.cfs.sqlkv.exception.TodoException;
import com.cfs.sqlkv.util.ParamUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.sql.Driver;

/**
 * @Description
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 11:29
 */
public class NetServer {

    private final static String[] COMMANDS ={"start","shutdown"};

    public final static int COMMAND_START = 0;

    public final static int COMMAND_SHUTDOWN = 1;

    /**当用户输入未知的命令解析为当前常量*/
    public final static int COMMAND_UNKNOWN = -1;

    protected final static String DEFAULT_ENCODING = "UTF8";

    private final static String DEFAULT_HOST = "localhost";

    /**默认编码格式*/
    final static Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);

    public static NetServer instance = new NetServer();

    private final static String EMBEDDED_DRIVER = "com.cfs.sqlkv.jdbc.EmbeddedDriver";

    private static Driver embeddedDriver;

    private ServerSocket serverSocket;

    private InetAddress hostAddress;

    private String hostArg = DEFAULT_HOST;

    public static void main(String[] args) {
        try{
            int command = ParamUtil.parseServerArgs(args);
            if(command == COMMAND_START){
                instance.blockingStart();
            }
        }catch (Exception e){

        }
    }

    public void blockingStart(){

    }

    /**
     *加载驱动并进行实例化
     * */
    protected void startNetWorkServer() throws Exception{
        Class<?> clazz = Class.forName(EMBEDDED_DRIVER);
        embeddedDriver = (Driver) clazz.getConstructor().newInstance();
    }

    private ServerSocket createServerSocket() throws Exception {
        //获取主机地址
        if (hostAddress == null){
            hostAddress = InetAddress.getByName(hostArg);
        }
        //
        //buildLocalAddressList(hostAddress);
        throw TodoException.e;
    }

    public void shutdown(){

    }



}
