package com.cfs.sqlkv.util;

import com.cfs.sqlkv.exception.TodoException;

import static com.cfs.sqlkv.net.NetServer.COMMAND_UNKNOWN;

/**
 * @Description 参数解析工具集
 * @author zhengxiaokang
 * @Email zheng.xiaokang@qq.com
 * @create 2018-12-15 11:37
 */
public class ParamUtil {

    public static int parseServerArgs(String args[]) throws Exception{
        int command = findCommand(args);
        if (command == COMMAND_UNKNOWN) {
            //TODO:返回信息需要完整
            throw new RuntimeException("未知的参数异常");
        }
        return command;
    }

    //TODO:参数解析未实现
    private static int findCommand(String[] args) throws Exception{
       throw TodoException.e;
    }
}
