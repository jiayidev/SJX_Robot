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
import com.android.jdrd.robot.dialog.RobotDialog;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * 客户端
 */
public class ClientSocketUtil extends Service {

    private Context mContext;
    private RobotDBHelper robotDBHelper;
    private static ServerSocket serverSocket;
    private static Socket socket1;
    private static Socket socket2;
    private static InputStream in = null;
    private static OutputStream out = null;
    private static String msg = null;
    public static Intent intent;
    private MyReceiver receiver;
    IntentFilter filter;
    public static List<Map> socketlist = new ArrayList<>();
    private String function;

    @Override
    public void onCreate() {
        super.onCreate();
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        intent = new Intent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startServerSocket(Constant.ClientPort);
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

            String camera = intent.getStringExtra("camera");
//            Constant.debugLog("收到摄像头数据" + camera);

//            if (camera != null) {
//                try {
//                    sendDateToClient(camera, Constant.ip_ros);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    public String startServerSocket(int port) throws IOException {

        //提升service进程优先级
        setServiceForeground();

        serverSocket = new ServerSocket(port);
        Socket socket;
        Constant.debugLog("serverSocket is create....");
        while (true) {
            Constant.debugLog("waiting for connect....");
            socket = serverSocket.accept();
            socket.setKeepAlive(true);
//            socket.setSoTimeout(9000);
            new Thread(new Task(socket)).start();
        }
    }

    /**
     * 为此服务设置一个状态栏，使服务始终处于前台，提高服务等级
     */
    private void setServiceForeground() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Socket通讯服务");
        builder.setContentText("此服务用于通讯，请勿关闭");
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        //启动到前台
        startForeground(1, notification);
    }

    /*public static void sendDateToClient(String str, String ip) throws IOException {

        String str2 = "*" + str + "#";
        if (ip.equals("/192.168.1.100")) {
            try {
                out1.write(str2.getBytes());
                out2.write(str2.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (ip.equals("/192.168.1.102")) {
            try {
                out1.write(str2.getBytes());
                out2.write(str2.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Constant.debugLog("IP不对");
        }
    }*/

    /**
     * 发送数据给ros和大屏，其他类可通过调用ServerSocketUtil.sendDateToClient(String str, String ip)来发送数据
     * @param str：要发送的字符串
     * @param ip： 要发送的客户端的IP, Constant.ip_ros为小屏IP, Constant.ip_bigScreen为大屏IP
     */
    public static synchronized void sendDateToClient(String str, String ip,Socket socket) throws IOException {

        try {
            if(socket.isClosed()){
            }else {
                out = socket.getOutputStream();
                if (ip != null) {
                    if (out != null) {
                        out.write(str.getBytes());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Task implements Runnable {
        private Socket socket;
        public Task(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            String str = socket.getInetAddress().toString();
            final String  ip = str.substring(1,str.length());
            Constant.debugLog(ip);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "连接服务端IP为： " + ip, Toast.LENGTH_LONG).show();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Socket socket_cache = socket;
                    final String socket_ip = ip;
                    while(true) {
                        try {
                            if(socket_cache.isClosed()){
                                break;
                            }else{
                                sendDateToClient("*heartbeat#", socket_ip, socket_cache);
                                Thread.sleep(3000);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Constant.debugLog(e.toString());
                            try {
                                socket.close();
                                robotDBHelper.execSQL("update robot set outline= '0' where ip= '"+ ip +"'");
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
                        inputStreamParse(in,ip,out);
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void inputStreamParse(InputStream in,String ip,OutputStream out) {
        byte[] buffer = new byte[1024];
        int i = 0;
        boolean flag = false;
        boolean flag2 = false;
        int len = 0;
        int len1 = 0;
        String string = null;
        while (true) {
            byte buf = 0;
            try {
                buf = (byte) in.read();
            } catch (IOException e) {
                e.printStackTrace();
                Constant.debugLog(e.toString());
            }
            len1++;
            Constant.debugLog("buf内容：" + buf +"len1"+len1);
            if (buf == -1) {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }else if (buf == 0) {
                break;
            }else if ('*' == buf) {
                flag = true;
                flag2 = true;
            }else if ('#' == buf) {
                flag = false;
            }
            if (flag) {
                buffer[i] = buf;
                i++;
            } else if (flag == false && flag2) {
                msg = new String(buffer, 1, i);
                msg = msg.trim();
                Constant.debugLog("msg"+msg);
                if (msg != null) {
                    ++len;
                    Constant.debugLog("msg的内容： " + msg + "  次数：" + len);
                    function = getJSONString(msg, "function");
                    if(function.equals("robot")){
                        List<Map> robotList = robotDBHelper.queryListMap("select * from robot " ,null);
                        Map robot ;
                        List<Map> datalist = new ArrayList<>();
                        for(int k = 0,size = robotList.size();k<size;k++){
                            robot = robotList.get(k);
                            Map map  = new LinkedHashMap();
                            map.put("ip",robot.get("ip"));
                            map.put("name",robot.get("name"));
                            map.put("id",robot.get("id"));
                            map.put("outline",robot.get("outline"));
                            map.put("electric",robot.get("electric"));
                            map.put("area",robot.get("area"));
                            map.put("state",robot.get("state"));
                            map.put("robotstate",robot.get("robotstate"));
                            map.put("obstacle",robot.get("obstacle"));
                            map.put("excute",robot.get("excute"));
                            map.put("excutetime",robot.get("excutetime"));
                            map.put("commandnum",robot.get("commandnum"));
                            map.put("commandstate",robot.get("commandstate"));
                            map.put("lastlocation",robot.get("lastlocation"));
                            map.put("lastcommandstate",robot.get("lastcommandstate"));
                            datalist.add(map);
                        }
                        Gson gson = new Gson();
                        Map map = new LinkedHashMap();
                        map.put("function", "robot");
                        map.put("data", datalist);
                        string = gson.toJson(map);
                    }else if(function.equals("desk")){
                        List<Map> deskList = robotDBHelper.queryListMap("select * from desk " ,null);
                        Map desk ;
                        List<Map> datalist = new ArrayList<>();
                        for(int k = 0,size = deskList.size();k<size;k++){
                            desk = deskList.get(k);
                            Map map  = new LinkedHashMap();
                            map.put("name",desk.get("name"));
                            map.put("id",desk.get("id"));
                            map.put("area",desk.get("area"));
                            datalist.add(map);
                        }
                        Gson gson = new Gson();
                        Map map = new LinkedHashMap();
                        map.put("function", "desk");
                        map.put("data", datalist);
                        string = gson.toJson(map);
                    }else if(function.equals("area")){
                        List<Map> areaList = robotDBHelper.queryListMap("select * from area " ,null);
                        Map area ;
                        List<Map> datalist = new ArrayList<>();
                        for(int k = 0,size = areaList.size();k<size;k++){
                            area = areaList.get(k);
                            Map map  = new LinkedHashMap();
                            map.put("name",area.get("name"));
                            map.put("id",area.get("id"));
                            datalist.add(map);
                        }
                        Gson gson = new Gson();
                        Map map = new LinkedHashMap();
                        map.put("function", "area");
                        map.put("data", datalist);
                        string = gson.toJson(map);
                    }else if(function.equals("command")){
                        String data = getJSONString(msg, "data");
                        List<Map> commandlit = robotDBHelper.queryListMap("select * from command where desk = '" + getJSONString(data,"desk") + "'", null);
                        RobotDialog.IP = getJSONString(data,"robot");
                        RobotDialog.robotlist = commandlit;
                        RobotDialog.CurrentIndex = -1;
                        RobotDialog.sendCommandList();
                    }
                    if(string !=null){
                        try {
                            out.write(string.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Constant.debugLog(e.toString());
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
                Constant.debugLog((char) buf + "");
                Constant.debugLog("数据格式不对");
                string = "*r+1+8+#";
                try {
                    out.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    Constant.debugLog(e.toString());
                }
            }
            string = null;
        }
    }


    private void sendBroadcastMain(String str){
        intent.putExtra("msg", str);
        intent.setAction("com.jdrd.activity.Main");
        sendBroadcast(intent);
    }
    private void sendBroadcastRobot(String str){
        intent.putExtra("msg", str);
        intent.setAction("com.jdrd.activity.Robot");
        sendBroadcast(intent);
    }

    public static String getJSONString(String str, String key) {
        JSONObject object = null;
        String jsonString = null;
        try {
            object = new JSONObject(str);
            jsonString = object.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static int getJSONInt(String data, String key) {
        JSONObject object = null;
        int jsonString = 0;
        try {
            object = new JSONObject(data);
            jsonString = object.getInt(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    @Override
    public void onDestroy() {
        //若通讯服务挂掉，再次开启服务
        Intent serverSocket = new Intent(this, ClientSocketUtil.class);
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
