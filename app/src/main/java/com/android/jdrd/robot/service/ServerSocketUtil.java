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
import org.json.JSONException;
import org.json.JSONObject;
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

    @Override
    public void onCreate() {
        super.onCreate();
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        intent = new Intent();

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
                    Toast.makeText(getApplicationContext(), "连接客户端IP为： " + ip, Toast.LENGTH_LONG).show();
                }
            });


            boolean IsHave = false;
            List<Map> robotList = robotDBHelper.queryListMap("select * from robot " ,null);
            if(robotList !=null && robotList.size() > 0) {
                for (int i = 0, size = robotList.size(); i < size; i++) {
                    if (robotList.get(i).get("ip").equals(ip)) {
                        IsHave = true;
                        robotDBHelper.execSQL("update robot set outline = '1' where ip= '"+ ip +"'");
                        Constant.debugLog("socketlist"+socketlist.toString());
                        break;
                    }
                }
                if(!IsHave){
                    robotDBHelper.execSQL("insert into  robot (name,ip,state,outline,electric,robotstate,obstacle," +
                            "commandnum,excute,excutetime,commandstate,lastcommandstate,lastlocation,area) values " +
                            "('新机器人','"+ip+"',0,1,100,0,0,0,0,0,0,0,0,0)");
                }
            }else{
                robotDBHelper.execSQL("insert into  robot (name,ip,state,outline,electric,robotstate,obstacle," +
                        "commandnum,excute,excutetime,commandstate,lastcommandstate,lastlocation,area) values " +
                        "('新机器人','"+ip+"',0,1,100,0,0,0,0,0,0,0,0,0)");
            }
            sendBroadcastMain("robot_connect");
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
            Constant.debugLog(IsHave+"IsHAVE");
            if(IsHave){
                int j = 0;
                Constant.debugLog(socketlist.size()+"IsHAVE");
                while(j < socketlist.size()){
                    Constant.debugLog(socketlist.get(j).get("ip")+"socketlist.get(j).get(\"ip\")"+ip+"ip");
                    if(socketlist.get(j).get("ip").equals(ip)){
                        try {
                            Constant.debugLog("inclose");
                            ((InputStream)socketlist.get(j).get("in")).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Constant.debugLog("socketclose");
                            ((Socket)socketlist.get(j).get("socket")).close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        socketlist.remove(j);
                        Constant.debugLog("socketlist"+socketlist.toString());
                        break;
                    }
                    j++;
                    Constant.debugLog("j socketlist" +j);
                }
            }
            Map<String, Object> map;
            map = new HashMap<>();
            map.put("socket", socket);
            map.put("ip", ip);
            map.put("in", in);
            map.put("out", out);
            socketlist.add(map);
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
                removeSocket(ip);
            }
            len1++;
            Constant.debugLog("buf内容：" + buf +"len1"+len1);
            if ( -1 == buf ) {
                removeSocket(ip);
                break;
            }else if (0 == buf) {
                removeSocket(ip);
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
                if (msg != null) {
                    ++len;
                    Constant.debugLog("msg的内容： " + msg + "  次数：" + len);
                    byte[] bytes = msg.getBytes();
                    Constant.debugLog(bytes[0]+"bytes");
                    flag = false;
                    List<String> str = new ArrayList<>();
                    int k = 0;
                    for(int h = 1,size = bytes.length;h<size;h++){
                        if(bytes[h]==43){
                            if(flag){
                                str.add(msg.substring(k+1,h));
                                k = h;
                            }else{
                                flag = true;
                                k = h;
                            }
                        }
                    }
                    if(Integer.valueOf(str.get(str.size()-1)) == msg.length() +2){
                        Constant.debugLog("长度正确");
                        switch (bytes[0]) {
                            case 97:
                                robotDBHelper.execSQL("update robot set electric = '"+str.get(0)+"' where ip= '"+ ip +"'");
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
                            case 98:
                                robotDBHelper.execSQL("update robot set state = '"+str.get(0)+"' where ip= '"+ ip +"'");
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
                            case 99:
                                robotDBHelper.execSQL("update robot set robotstate = '"+str.get(0)+"' where ip= '"+ ip +"'");
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
                            case 100:
                                robotDBHelper.execSQL("update robot set obstacle = '"+str.get(0)+"' where ip= '"+ ip +"'");
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
                            case 101:
                                robotDBHelper.execSQL("update robot set lastlocation = '"+str.get(0)+"' where ip= '"+ ip +"'");
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
                            //r
                            case 114:
                                if(str.get(0).equals("0")){
                                    sendBroadcastMain("robot_receive_succus");
                                }else{
                                    sendBroadcastMain("robot_receive_fail");
                                }
                                break;
                        }
                    }else{
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
        }
    }

    public void removeSocket(String ip){
        Socket socket ;
        int j = 0;
        while(j < socketlist.size()){
            if(socketlist.get(j).get("ip").equals(ip)){
                socket = (Socket) socketlist.get(j).get("socket");
                socketlist.remove(j);
                robotDBHelper.execSQL("update robot set outline= '0' where ip= '"+ ip +"'");
                sendBroadcastMain("robot_unconnect");
                sendBroadcastRobot("robot");
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
