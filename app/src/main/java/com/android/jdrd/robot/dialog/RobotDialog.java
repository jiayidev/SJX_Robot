package com.android.jdrd.robot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.activity.MainActivity;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.service.ServerSocketUtil;
import com.android.jdrd.robot.util.Constant;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/8
 * 描述: 自定义机器人运行轨迹对话框
 */
public class RobotDialog extends Dialog {
    // 初始化数据库帮助类
    private static RobotDBHelper robotDBHelper;
    private Context context;
    // 横向加载机器人列表
    private GridView gridView;
    // 初始化适配器
    private SimpleAdapter robotAdapter;

    // 存储机器人列表
    public static List<Map> list;
    public static List<Map> robotList;
    // 存储机器人数据列表
    private List<Map<String, Object>> robotData_list = new ArrayList<>();

    private final String[] from = {"image", "text", "name", "imageback"};
    private final int[] to = {R.id.imageview, R.id.text, R.id.name, R.id.imageback};
    // 当前下标
    public static int CurrentIndex = -1;
    // 发送数据
    private static String sendStr;
    // IP地址
    public static String IP;
    // 创建线程
    public static Thread thread = new Thread();
    public static boolean flag;

    public RobotDialog(Context context, String str) {
        super(context, R.style.SoundRecorder);
        setCustomDialog();
        this.context = context;
        this.sendStr = str;
        flag = false;
    }

    public RobotDialog(Context context, List<Map> robotList) {
        super(context, R.style.SoundRecorder);
        // 初始化数据
        setCustomDialog();
        this.context = context;
        this.robotList = robotList;
        flag = true;
    }

    /**
     * 初始化
     */
    private void setCustomDialog() {
        // 加载要执行的机器人布局
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_robot_dialog, null);
        // 初始化要执行的机器人列表
        gridView = (GridView) mView.findViewById(R.id.robot_girdview);
        list = new ArrayList<>();
        // 初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(context);
        try {
            // 查询机器人列表 根据区域名称 and 在线状态
            list = robotDBHelper.queryListMap("select * from robot where area = '" + MainActivity.CURRENT_AREA_id + "' and outline = '1'", null);
            // 打印log
            Constant.debugLog("要执行的机器人列表----->" + list.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list != null && list.size() > 0) {
            int i = 0;
            int j = list.size();
            Map<String, Object> map;
            while (i < j) {
                // 以键值对的形式存储数据
                map = new HashMap<>();
                map.put("image", R.mipmap.zaixian);
                map.put("name", list.get(i).get("name"));
                map.put("ip", list.get(i).get("ip"));
                // 机器人状态 0->空闲  1->送餐  2->故障
                switch ((int) list.get(i).get("robotstate")) {
                    case 0:
                        map.put("text", "空闲");
                        map.put("imageback", R.mipmap.kongxian);
                        break;
                    case 1:
                        map.put("text", "送餐");
                        map.put("imageback", R.mipmap.fuwuzhong);
                        break;
                    case 2:
                        map.put("text", "故障");
                        map.put("imageback", R.mipmap.guzhang);
                        break;
                }
                robotData_list.add(map);
                i++;
            }
            // 打印log
            Constant.debugLog(robotData_list.toString());
            // 简单的适配器   没有自定义  调用系统提供的适配器
            robotAdapter = new SimpleAdapter(getContext(), robotData_list, R.layout.robot_grid_item, from, to);
            // 加载适配器
            gridView.setAdapter(robotAdapter);
            // 要执行的机器人子项列表
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    IP = robotData_list.get(position).get("ip").toString();
                    if (flag) {
                        CurrentIndex = -1;
                        // 发送命令
                        sendCommandList();
                        // 销毁当前Dialog
                        dismiss();
                    } else {
                        // 发送命令
                        sendCommand();
                        // 销毁当前Dialog
                        dismiss();
                    }
                }
            });
        }

        super.setContentView(mView);
    }

    //发送命令
    public static void sendCommand() {
        for (Map map : ServerSocketUtil.socketList) {
            if (map.get("ip").equals(IP)) {
                final OutputStream out = (OutputStream) map.get("out");
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (out != null) {
                            try {
                                out.write(sendStr.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
            }
        }
    }

    // 发送命令列表
    public static void sendCommandList() {
        // 打印log
        Constant.debugLog(robotList.toString());
        for (Map map : ServerSocketUtil.socketList) {
            // 检查IP是否相同
            if (map.get("ip").equals(IP)) {
                final OutputStream out = (OutputStream) map.get("out");
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (out != null) {
                            try {
                                if (CurrentIndex == -1) {
                                    // 等待
                                    out.write("*s+6+#".getBytes());
                                    synchronized (thread) {
                                        thread.wait();
                                    }
                                }
                                Constant.debugLog("当前下标CurrentIndex----->" + CurrentIndex);
                                int size;
                                for (size = robotList.size(); CurrentIndex < size; CurrentIndex++) {
                                    switch ((int) robotList.get(CurrentIndex).get("type")) {
                                        // 前进
                                        case 0:
                                            // 查询系统卡 根据ID查询
                                            List<Map> card_list = robotDBHelper.queryListMap("select * from card where id = '" + robotList.get(CurrentIndex).get("goal") + "'", null);
                                            if (card_list != null && card_list.size() > 0) {
                                                sendStr = "*g+" + card_list.get(0).get("address") + "+" + robotList.get(CurrentIndex).get("direction") + "+" + robotList.get(CurrentIndex).get("speed")
                                                        + "+" + robotList.get(CurrentIndex).get("music") + "+" + robotList.get(CurrentIndex).get("outime") + "+"
                                                        + robotList.get(CurrentIndex).get("shownumber") + "+" + robotList.get(CurrentIndex).get("showcolor");
                                            }
                                            setSendStr(out, sendStr);
                                            synchronized (thread) {
                                                thread.wait();
                                            }
                                            break;
                                        // 左转
                                        case 1:
                                            sendStr = "*d+" + robotList.get(CurrentIndex).get("speed")
                                                    + "+" + robotList.get(CurrentIndex).get("music") + "+" + robotList.get(CurrentIndex).get("outime") + "+"
                                                    + robotList.get(CurrentIndex).get("shownumber") + "+" + robotList.get(CurrentIndex).get("showcolor");
                                            setSendStr(out, sendStr);
                                            synchronized (thread) {
                                                thread.wait();
                                            }
                                            break;
                                        // 右转
                                        case 2:
                                            sendStr = "*r+" + robotList.get(CurrentIndex).get("speed")
                                                    + "+" + robotList.get(CurrentIndex).get("music") + "+" + robotList.get(CurrentIndex).get("outime") + "+"
                                                    + robotList.get(CurrentIndex).get("shownumber") + "+" + robotList.get(CurrentIndex).get("showcolor");
                                            setSendStr(out, sendStr);
                                            synchronized (thread) {
                                                thread.wait();
                                            }
                                            break;
                                        // 旋转
                                        case 3:
                                            sendStr = "*w+" + robotList.get(CurrentIndex).get("music") + "+" + robotList.get(CurrentIndex).get("outime") + "+"
                                                    + robotList.get(CurrentIndex).get("shownumber") + "+" + robotList.get(CurrentIndex).get("showcolor");
                                            setSendStr(out, sendStr);
                                            synchronized (thread) {
                                                thread.wait();
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                thread.start();
            }
        }
    }

    /**
     * 发送命令格式
     * @param out 输出流
     * @param str 发送内容
     */
    private static void setSendStr(OutputStream out, String str) {
        if (str.length() >= 6) {
            str = str + "+" + (str.length() + 5) + "+#";
        } else {
            str = str + "+" + (str.length() + 4) + "+#";
        }
        try {
            out.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}