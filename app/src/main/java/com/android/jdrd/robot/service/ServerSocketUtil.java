package com.android.jdrd.robot.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端
 */
public class ServerSocketUtil extends Service {

    //数据库帮助类
    private RobotDBHelper robotDBHelper;
    //创建一个服务器端的Socket，即ServerSocket
    private static ServerSocket serverSocket;
    //获取输入流
    private static InputStream in = null;
    //获取输出流
    private static OutputStream out = null;
    //消息
    private static String msg = null;

    public static Intent intent;
    private MyReceiver receiver;
    IntentFilter filter;
    public static List<Map> socketList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        //初始化Intent
        intent = new Intent();
        //启动线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startServerSocket(Constant.ServerPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        receiver = new MyReceiver();
        filter = new IntentFilter();
        filter.addAction("com.jdrd.activity.Main");
        registerReceiver(receiver, filter);
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    //启动ServerSocket
    public String startServerSocket(int port) throws IOException {

        //提升service进程优先级
        setServiceForeground();
        //创建ServerSocket对象
        serverSocket = new ServerSocket(port);
        //创建Socket对象
        Socket socket;
        //打印Log
        Constant.debugLog("serverSocket正在创建......");
        //死循环
        while (true) {
            Constant.debugLog("正在等待连接......");
            //接收请求
            socket = serverSocket.accept();
            //作用:每隔一段时间检查服务器是否处于活动状态，如果服务器端长时间没响应，自动关闭客户端socket
            //防止服务器端无效时，客户端长时间处于连接状态
            socket.setKeepAlive(true);

            //客户端socket在接收数据时，有两种超时:
            // 1.连接服务器超时，即连接超时;
            // 2.连接服务器成功后，接收服务器数据超时，即接收超时
            //设置socket 读取数据流的超时时间
//            socket.setSoTimeout(9000);

            //开启线程
            new Thread(new Task(socket)).start();
        }
    }

    /**
     * 为此服务设置一个状态栏，使服务始终处于前台，提高服务等级
     */
    private void setServiceForeground() {
        //系统通知栏
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.sjx_launch);//通知栏图片
        builder.setContentTitle("Socket通讯服务");//通知栏标题
        builder.setContentText("正在通讯，请勿关闭");//通知栏内容

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        //启动到前台
        startForeground(1, notification);
    }

    /**
     * @param str：要发送的字符串
     * @param ip：发送的客户端的IP
     */
    public static synchronized void sendDateToClient(String str, String ip, Socket socket) throws IOException {

        try {
            if (socket.isClosed()) {

            } else {
                //获取输出流
                out = socket.getOutputStream();
                if (ip != null) {
                    if (out != null) {
                        //写入数据
                        out.write(str.getBytes());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //创建Task线程
    class Task implements Runnable {
        private Socket socket;

        //构造方法
        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //获取本机IP地址
            String str = socket.getInetAddress().toString();
            //截取IP地址
            final String ip = str.substring(1, str.length());
            //打印IP地址
            Constant.debugLog("连接客户端的IP为:----->" + ip);
            //子线程更新UI  调用Looper.getMainLooper()
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "连接客户端IP为:" + ip, Toast.LENGTH_LONG).show();
                }
            });

            boolean IsHave = false;
            //查询机器人
            List<Map> robotList = robotDBHelper.queryListMap("select * from robot ", null);
            if (robotList != null && robotList.size() > 0) {
                for (int i = 0, size = robotList.size(); i < size; i++) {
                    if (robotList.get(i).get("ip").equals(ip)) {
                        IsHave = true;
                        //修改运行路线
                        robotDBHelper.execSQL("update robot set outline = '1' where ip= '" + ip + "'");
                        //打印日志
                        Constant.debugLog("socketList----->" + socketList.toString());
                        break;
                    }
                }
                //收到客户端的连接之后，添加新的机器人
                if (!IsHave) {
                    robotDBHelper.execSQL("insert into  robot (name,ip,state,outline,electric,robotstate,obstacle," +
                            "commandnum,excute,excutetime,commandstate,lastcommandstate,lastlocation,area) values " +
                            "('新机器人','" + ip + "',0,1,100,0,0,0,0,0,0,0,0,0)");
                }
            } else {
                robotDBHelper.execSQL("insert into  robot (name,ip,state,outline,electric,robotstate,obstacle," +
                        "commandnum,excute,excutetime,commandstate,lastcommandstate,lastlocation,area) values " +
                        "('新机器人','" + ip + "',0,1,100,0,0,0,0,0,0,0,0,0)");
            }
            //广播发送连接
            sendBroadcastMain("robot_connect");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Socket socket_cache = socket;
                    final String socket_ip = ip;
                    while (true) {
                        try {
                            if (socket_cache.isClosed()) {
                                break;
                            } else {
                                //发送心跳包    用于测试服务端与客户端是否在连接状态 每隔3秒发送一次
                                sendDateToClient("*heartbeat#", socket_ip, socket_cache);
                                Thread.sleep(3000);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //打印异常
                            Constant.debugLog("异常信息----->" + e.toString());
                            try {
                                socket.close();
                                robotDBHelper.execSQL("update robot set outline= '0' where ip= '" + ip + "'");
                                sendBroadcastMain("robot_connect");
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }).start();

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        inputStreamParse(in, ip, out);
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //打印日志
            Constant.debugLog("IsHAVE----->" + IsHave);
            if (IsHave) {
                int j = 0;
                Constant.debugLog("IsHAVE----->" + socketList.size());
                while (j < socketList.size()) {
                    //打印日志
                    Constant.debugLog(socketList.get(j).get("ip") + "socketlist.get(j).get(\"ip\")" + ip + "ip");
                    if (socketList.get(j).get("ip").equals(ip)) {
                        try {
                            // 打印Log
                            Constant.debugLog("inClose");
                            ((InputStream) socketList.get(j).get("in")).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            // 打印Log
                            Constant.debugLog("socketClose");
                            ((Socket) socketList.get(j).get("socket")).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        socketList.remove(j);
                        // 打印Log
                        Constant.debugLog("socketList----->" + socketList.toString());
                        break;
                    }
                    j++;
                    // 打印Log
                    Constant.debugLog("j socketList----->" + j);
                }
            }
            // 传递数据 以键值对的形式
            Map<String, Object> map;
            map = new HashMap<>();
            map.put("socket", socket);
            map.put("ip", ip);
            map.put("in", in);
            map.put("out", out);
            socketList.add(map);
        }
    }

    //解析输入流
    public void inputStreamParse(InputStream in, String ip, OutputStream out) {
        byte[] buffer = new byte[1024];
        int i = 0;
        boolean flag = false;
        boolean flag2 = false;
        int len = 0;
        int len1 = 0;
        String string;
        while (true) {
            byte buf = 0;
            try {
                //读取输入流
                buf = (byte) in.read();
            } catch (IOException e) {
                e.printStackTrace();
                Constant.debugLog("异常打印----->" + e.toString());
                removeSocket(ip);
            }
            len1++;
            Constant.debugLog("buf内容----->" + buf + "len1----->" + len1);
            if (-1 == buf) {
                //移除Socket
                removeSocket(ip);
                break;
            } else if (0 == buf) {
                //移除Socket
                removeSocket(ip);
                break;
            } else if ('*' == buf) {
                flag = true;
                flag2 = true;
            } else if ('#' == buf) {
                flag = false;
            }
            if (flag) {
                buffer[i] = buf;
                i++;
            } else if (flag == false && flag2) {
                msg = new String(buffer, 1, i);
                // 去空格
                msg = msg.trim();
                if (msg != null) {
                    ++len;
                    // 打印日志
                    Constant.debugLog("msg的内容----->" + msg + "  次数：" + len);
                    byte[] bytes = msg.getBytes();
                    // 打印日志
                    Constant.debugLog(bytes[0] + "bytes");
                    flag = false;
                    List<String> str = new ArrayList<>();
                    int k = 0;
                    for (int h = 1, size = bytes.length; h < size; h++) {
                        if (bytes[h] == 43) {
                            if (flag) {
                                str.add(msg.substring(k + 1, h));
                                k = h;
                            } else {
                                flag = true;
                                k = h;
                            }
                        }
                    }
                    if (Integer.valueOf(str.get(str.size() - 1)) == msg.length() + 2) {
                        Constant.debugLog("=====长度正确=====");
                        switch (bytes[0]) {
                            // 电量
                            case 97:
                                robotDBHelper.execSQL("update robot set electric = '" + str.get(0) + "' where ip= '" + ip + "'");
                                sendBroadcastRobot("robot");
                                sendBroadcastMain("robot_connect");
                                if (out != null) {
                                    string = "*r+0+8+#";
                                    try {
                                        out.write(string.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            // 运动状态   0->直行前进  1->左转   2->右转   3->旋转
                            case 98:
                                robotDBHelper.execSQL("update robot set state = '" + str.get(0) + "' where ip= '" + ip + "'");
                                sendBroadcastRobot("robot");
                                sendBroadcastMain("robot_connect");
                                if (out != null) {
                                    string = "*r+0+8+#";
                                    try {
                                        out.write(string.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            // 机器人状态  0->空闲   0->送餐   0->故障
                            case 99:
                                robotDBHelper.execSQL("update robot set robotstate = '" + str.get(0) + "' where ip= '" + ip + "'");
                                sendBroadcastRobot("robot");
                                sendBroadcastMain("robot_connect");
                                if (out != null) {
                                    string = "*r+0+8+#";
                                    try {
                                        out.write(string.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            // 障碍物
                            case 100:
                                robotDBHelper.execSQL("update robot set obstacle = '" + str.get(0) + "' where ip= '" + ip + "'");
                                sendBroadcastRobot("robot");
                                sendBroadcastMain("robot_connect");
                                if (out != null) {
                                    string = "*r+0+8+#";
                                    try {
                                        out.write(string.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            // 最后坐标
                            case 101:
                                robotDBHelper.execSQL("update robot set lastlocation = '" + str.get(0) + "' where ip= '" + ip + "'");
                                sendBroadcastRobot("robot");
                                sendBroadcastMain("robot_connect");
                                if (out != null) {
                                    string = "*r+0+8+#";
                                    try {
                                        out.write(string.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            // 接收应答   0成功   1命令校验失败
                            case 114:
                                if (str.get(0).equals("0")) {
                                    sendBroadcastMain("robot_receive_succus");
                                } else {
                                    sendBroadcastMain("robot_receive_fail");
                                }
                                break;
                        }
                    } else {
                        // 打印日志
                        Constant.debugLog("长度不对");
                        if (out != null) {
                            string = "*r+1+8+#";
                            try {
                                out.write(string.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    i = 0;
                    for (int m = 0; m < buffer.length; m++) {
                        buffer[m] = 0;
                    }
                    flag = false;
                    flag2 = false;
                }
            } else {
                // 打印日志
                Constant.debugLog((char) buf + "");
                Constant.debugLog("=====数据格式不对=====");
                string = "*r+1+8+#";
                try {
                    out.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    Constant.debugLog(e.toString());
                }
            }
        }
    }

    //移除Socket
    public void removeSocket(String ip) {
        Socket socket;
        int j = 0;
        while (j < socketList.size()) {
            if (socketList.get(j).get("ip").equals(ip)) {
                socket = (Socket) socketList.get(j).get("socket");
                socketList.remove(j);
                //修改运行轨迹
                robotDBHelper.execSQL("update robot set outline= '0' where ip= '" + ip + "'");
                //断开连接
                sendBroadcastMain("robot_unconnect");
                sendBroadcastRobot("robot");
                //关闭流
                try {
                    in.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            j++;
        }
    }


    //广播发送
    private void sendBroadcastMain(String str) {
        intent.putExtra("msg", str);
        intent.setAction("com.jdrd.activity.Main");
        sendBroadcast(intent);
    }

    //广播发送
    private void sendBroadcastRobot(String str) {
        intent.putExtra("msg", str);
        intent.setAction("com.jdrd.activity.Robot");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        //若通讯服务挂掉，再次开启服务
        Intent serverSocket = new Intent(this, ServerSocketUtil.class);
        startService(serverSocket);
        intent.putExtra("msg", "robot_destory");
        intent.setAction("com.jdrd.activity.Main");
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
