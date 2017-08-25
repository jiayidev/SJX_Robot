package com.android.jdrd.robot.util;

import android.util.Log;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/2
 * 描述: 常量类
 */
public class Constant {

    //wifi静态ip参数
    static final String dns1 = "192.168.1.1";
    static final String dns2 = "192.168.0.1";
    static String gateway = "";
    static int prefix = 24;
    static String IP = "";
    static final String IPLast = "178";
    static String isConnectSocket = "";
    //测试用WI-FI02
    public static String wifiname = "MAIKESITE";
    public static String password = "MKST8888";
    //Socket服务器端口
    public static int ServerPort = 8899;
    public static int ClientPort = 8899;
    //是否打印日志
    private static final boolean isDebug = true;
    //日志标题
    private static final String TAG = "SJX_Robot---->";
    public static int linearWidth;

    //唯一对象
    private static Constant constant;

    public static Constant getConstant(){
        if(constant != null){
            return constant;
        }else {
            constant = new Constant();
            return constant;
        }
    }

    public static void debugLog(String string){
        if(isDebug){
            Log.e(TAG,string);
        }
    }
}
