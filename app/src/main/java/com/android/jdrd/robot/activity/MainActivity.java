package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.Test.Protocol;
import com.android.jdrd.robot.adapter.AreaAdapter;
import com.android.jdrd.robot.adapter.DeskAdapter;
import com.android.jdrd.robot.adapter.GridViewAdapter;
import com.android.jdrd.robot.dialog.DeleteDialog;
import com.android.jdrd.robot.dialog.MyDialog;
import com.android.jdrd.robot.dialog.RobotDialog;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.service.ServerSocketUtil;
import com.android.jdrd.robot.service.SetStaticIPService;
import com.android.jdrd.robot.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/7/27
 * 描述: 主页
 */
public class MainActivity extends Activity implements View.OnClickListener, Animation.AnimationListener {
    // 初始化广播
    private MyReceiver receiver;
    // 初始化数据库帮助类
    private RobotDBHelper robotDBHelper;

    // 存储数据  以Map键值对的形式存储
    private static List<Map> areaList = new ArrayList<>();// 区域
    private static List<Map> deskList = new ArrayList<>();// 桌面
    private static List<Map> robotList = new ArrayList<>();// 机器人
    private static List<Map> commandList = new ArrayList<>();// 命令
    private static List<Map> robotData_List = new ArrayList<>();// 机器人数据

    // 区域数据列
    private List<Map<String, Object>> areaData_list = new ArrayList<>();
    // 桌面数据列
    private List<Map<String, Object>> deskData_list = new ArrayList<>();

    // 左侧平移出的linearLayout_all
    private LinearLayout linearLayout_all;
    // 机器人LinearLayout
    private LinearLayout linear_robot;
    // 桌面LinearLayout
    private LinearLayout linear_desk;

    private RelativeLayout map_right_Relative;
    // 导航栏左侧按钮
    private ImageView imgViewMapRight;

    // 桌子横向展示
    private GridView deskView;
    // 机器人状态横向展示
    private GridView robotGirdView;
    // 区域列表
    private ListView area;
    // 区域名称
    private TextView area_text;
    // 上 下 左 右 停止 收缩
    private Button up, down, left, right, stop, shrink;

    // 当前的下标
    public static int Current_INDEX = 1;
    // 当前桌面id
    public static int CURRENT_AREA_id = 0;
    //初始化平移动画
    private TranslateAnimation translateAnimation;

    // 桌面适配器
    private DeskAdapter desk_adapter;
    // 区域适配器
    private AreaAdapter area_adapter;
    // 机器人状态适配器
    private GridViewAdapter gridViewAdapter;

    // 桌子是否编辑
    public static boolean DeskIsEdit = false;
    // 区域是否编辑
    public static boolean AreaIsEdit = false;

    // 是否向右展开
    private boolean IsRight = true;
    // 是否销毁
    private boolean IsFinish = true;
    // 是否收缩
    private boolean isShrink = false;
    //密度
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // 静态IP
        Intent SetStaticIPService = new Intent(this, SetStaticIPService.class);
        startService(SetStaticIPService);
        // 启动后台通讯服务
        Intent serverSocket = new Intent(this, ServerSocketUtil.class);
        startService(serverSocket);

        // 初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());

        //初始化控件
        // 左侧平移出来的LinearLayout
        linearLayout_all = (LinearLayout) findViewById(R.id.linearlayout_all);

        // 导航栏左侧ImageView
        imgViewMapRight = (ImageView) findViewById(R.id.imgViewmapnRight);
        imgViewMapRight.setOnClickListener(this);

        // 左侧平移出来的RelativeLayout
        map_right_Relative = (RelativeLayout) findViewById(R.id.map_right_Ralative);

        // 区域列表
        area = (ListView) findViewById(R.id.area);

        // 区域右侧编辑桌子按钮
        findViewById(R.id.config_redact).setOnClickListener(this);

        // 初始化区域名称
        area_text = (TextView) findViewById(R.id.area_text);

        // 机器人整体LinearLayout
        linear_robot = (LinearLayout) findViewById(R.id.linear_robot);
        linear_robot.setOnClickListener(this);

        // 桌面整体LinearLayout
        linear_desk = (LinearLayout) findViewById(R.id.linear_desk);
        linear_desk.setOnClickListener(this);

        // ActionBar
        findViewById(R.id.main).setOnClickListener(this);

        // 头RelativeLayout
        findViewById(R.id.activity_main).setOnClickListener(this);

        // 初始化机器人列表
        robotGirdView = (GridView) findViewById(R.id.robotgirdview);
        robotGirdView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                } else {
                    // 跳转到RobotActivity 并传递数据
                    Intent intent = new Intent(MainActivity.this, RobotActivity.class);
                    intent.putExtra("id", (Integer) robotData_List.get(position).get("id"));
                    startActivity(intent);
                }
            }
        });

        // 初始化上按钮
        up = (Button) findViewById(R.id.up);
        up.setOnClickListener(this);
        // 初始化下按钮
        down = (Button) findViewById(R.id.down);
        down.setOnClickListener(this);
        // 初始化左按钮
        left = (Button) findViewById(R.id.left);
        left.setOnClickListener(this);
        // 初始化右按钮
        right = (Button) findViewById(R.id.right);
        right.setOnClickListener(this);
        // 初始化停止按钮
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);
        // 初始化收缩按钮
        shrink = (Button) findViewById(R.id.shrink);
        shrink.setOnClickListener(this);

        //获取数据
        desk_adapter = new DeskAdapter(this, deskData_list);

        // 初始化桌面列表
        deskView = (GridView) findViewById(R.id.gview);
        deskView.setAdapter(desk_adapter);
        // 桌面子列表点击事件
        deskView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                } else {
                    // 获取区域数据
                    getAreaData();
                    if (areaList != null && areaList.size() > 0 && CURRENT_AREA_id != 0) {
                        if (DeskIsEdit) {
                            if (position == 0) {
                                // 跳转到DeskConfigPathActivity 并传递area
                                Intent intent = new Intent(MainActivity.this, DeskConfigPathActivity.class);
                                intent.putExtra("area", CURRENT_AREA_id);
                                startActivity(intent);
                            } else {
                                // 跳转到DeskConfigPathActivity 并传递area
                                Intent intent = new Intent(MainActivity.this, DeskConfigPathActivity.class);
                                intent.putExtra("area", CURRENT_AREA_id);
                                intent.putExtra("id", (Integer) deskData_list.get(position).get("id"));
                                startActivity(intent);
                            }
                            // 获取桌面数据
                            getDeskData();
                        } else {
                            // 打印Log
                            Constant.debugLog("position----->" + CURRENT_AREA_id);
                            commandList = robotDBHelper.queryListMap("select * from command where desk = '" + deskData_list.get(position).get("id") + "'", null);
                            if (commandList != null && commandList.size() > 0) {
                                robotDialog(commandList);
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "请添加并选择区域", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        area_adapter = new AreaAdapter(this, areaData_list);
        area.setAdapter(area_adapter);
        // 区域子列表点击事件
        area.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取区域数据
                getAreaData();
                if (AreaIsEdit) {
                    if (position == 0) {
                        AreaIsEdit = false;
                    } else if (position == 1) {
                        dialog();
                    } else {
                        dialog(areaData_list.get(position).get("name").toString(), (int) areaData_list.get(position).get("id"));
                    }
                    getAreaData();
                } else {
                    if (position == 0) {
                        AreaIsEdit = true;
                    } else {
                        if (!IsRight) {
                            startAnimationLeft();
                        }
                        DeskIsEdit = false;
                        CURRENT_AREA_id = (int) areaData_list.get(position).get("id");
                        Current_INDEX = position;
                        area_text.setText(areaData_list.get(position).get("name").toString());
                        // 获取桌面数据
                        getDeskData();
                    }
                    // 获取区域数据
                    getAreaData();
                }
            }
        });
        robotDBHelper.execSQL("update robot set outline= '0' ");
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.jdrd.activity.Main");
        // 注册广播
        if (receiver != null) {
            this.registerReceiver(receiver, filter);
        }
    }

    // 设置机器人列表属性
    private void setGridView() {
        int size = robotData_List.size();
        int length = 76;
        int height = 106;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        int gridViewWidth = (int) (size * (length + 30) * density);
        if (gridViewWidth <= 340 * density) {
            gridViewWidth = (int) (340 * density);
        }
        int itemWidth = (int) (length * density);
        int itemHeight = (int) (height * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridViewWidth, itemHeight);
        Constant.linearWidth = (int) (76 * density);
        robotGirdView.setLayoutParams(params); // 重点
        robotGirdView.setColumnWidth(itemWidth); // 重点
        robotGirdView.setHorizontalSpacing((int) (8 * density)); // 间距
        robotGirdView.setStretchMode(GridView.NO_STRETCH);
        robotGirdView.setNumColumns(size); // 重点
        gridViewAdapter = new GridViewAdapter(getApplicationContext(),
                robotData_List);
        robotGirdView.setAdapter(gridViewAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消广播注册
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAreaData();
        if (CURRENT_AREA_id == 0) {
            if (areaList != null && areaList.size() > 0) {
                CURRENT_AREA_id = (int) areaList.get(0).get("id");
                Current_INDEX = 1;
                area_text.setText(areaList.get(0).get("name").toString());
            } else {
                area_text.setText("请选择左侧区域");
            }
        } else {
            for (int i = 0, size = areaList.size(); i < size; i++) {
                if (((int) areaList.get(i).get("id")) == CURRENT_AREA_id) {
                    area_text.setText(areaList.get(i).get("name").toString());
                    CURRENT_AREA_id = (int) areaList.get(i).get("id");
                    Current_INDEX = i + 1;
                }
            }
        }
        getDeskData();
        getRobotData();
        gridViewAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 机器人列表整体
            case R.id.linear_robot:
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            // ActionBar
            case R.id.main:
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            // 导航栏左侧菜单按钮
            case R.id.imgViewmapnRight:
                startAnimationLeft();
                break;
            // 区域右侧编辑按钮
            case R.id.config_redact:
                if (DeskIsEdit) {
                    DeskIsEdit = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.config_redact).setBackground(getResources().getDrawable(R.animator.btn_direct_selector, null));
                    }
                } else {
                    DeskIsEdit = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.config_redact).setBackground(getResources().getDrawable(R.animator.btn_exit_selector, null));
                    }
                }
                getDeskData();
                break;
            // 机器人列表整体
            case R.id.robotgirdview:
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            // 桌面列表整体
            case R.id.linear_desk:
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            // 前进命令
            case R.id.up:
                robotDialog("*u+6+#");
                // 发送命令
                //robotDialog(Protocol.getSendData(16, Protocol.getCommandData(Protocol.MN_PATTERN)));
                break;
            // 后退命令
            case R.id.down:
                robotDialog("*d+6+#");
                break;
            // 左转命令
            case R.id.left:
                robotDialog("*l+6+#");
                break;
            // 右转命令
            case R.id.right:
                robotDialog("*r+6+#");
                break;
            // 停止命令
            case R.id.stop:
                robotDialog("*s+6+#");
                break;
            // 右下角收缩FloatButton按钮
            case R.id.shrink:
                // 关闭左侧区域
                if (!IsRight) {
                    startAnimationLeft();
                }
                // 点击展开 or 收缩
                startAnimationShrink();
                break;
        }
    }

    // 获取桌面书局
    public List<Map<String, Object>> getDeskData() {
        // 先清除一次
        deskData_list.clear();
        try {
            // 查询桌面列表
            deskList = robotDBHelper.queryListMap("select * from desk where area = '" + CURRENT_AREA_id + "'", null);
            // 打印Log
            Constant.debugLog("Robot----->" + deskList.toString());
            Constant.debugLog("Robot----->" + "CURRENT_AREA_id" + CURRENT_AREA_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> map;
        if (DeskIsEdit) {
            map = new HashMap<>();
            map.put("image", R.animator.btn_add_desk_selector);
            map.put("id", 0);
//            map.put("name",getString(R.string.config_add));
            deskData_list.add(map);
        }
        if (deskList != null && deskList.size() > 0) {
            for (int i = 0, size = deskList.size(); i < size; i++) {
                map = new HashMap<>();
                if (DeskIsEdit) {
//                    map.put("image", R.mipmap.ic_launcher);
                } else {
//                    map.put("image", R.mipmap.bg);
                }
                map.put("id", deskList.get(i).get("id"));
                map.put("name", deskList.get(i).get("name"));
                map.put("area", deskList.get(i).get("area"));
                deskData_list.add(map);
            }
        }
        desk_adapter.notifyDataSetChanged();
        return deskData_list;
    }

    //获取机器人数据
    public List<Map> getRobotData() {
        // 先清除一次
        robotData_List.clear();
        try {
            // 查询机器人列表
            robotList = robotDBHelper.queryListMap("select * from robot", null);
            // 打印Log
            Constant.debugLog("robotList----->" + robotList.toString());

            List<Map> robotData_ListCache = new ArrayList<>();
            int j;
            boolean flag;
            for (int i = 0, size = robotList.size(); i < size; i++) {
                // 打印log
                Constant.debugLog("size----->" + size + " ip----->" + robotList.get(i).get("ip").toString());
                String ip = robotList.get(i).get("ip").toString();
                j = 0;
                int h = ServerSocketUtil.socketList.size();
                flag = false;
                while (j < h) {
                    if (ip.equals(ServerSocketUtil.socketList.get(j).get("ip"))) {
                        // 打印Log
                        Constant.debugLog("<-----对比----->");
                        // 修改运行轨迹
                        robotDBHelper.execSQL("update robot set outline= '1' where ip = '" + robotList.get(i).get("ip") + "'");
                        robotList.get(i).put("outline", 1);
                        robotData_ListCache.add(robotList.get(i));
                        robotList.remove(i);
                        flag = true;
                        break;
                    }
                    j++;
                    h = ServerSocketUtil.socketList.size();
                }
                size = robotList.size();
                if (flag) {
                    i--;
                }
            }
            robotData_List.addAll(robotData_ListCache);
            robotData_List.addAll(robotList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setGridView();
        return robotData_List;
    }

    // 获取区域数据
    public List<Map<String, Object>> getAreaData() {
        // 先清除一次
        areaData_list.clear();
        try {
            // 查询区域列表
            areaList = robotDBHelper.queryListMap("select * from area", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> map;
        map = new HashMap<>();
        map.put("image", R.mipmap.qybianji_no);
//        map.put("name",getString(R.string.config_redact));
        areaData_list.add(map);
        if (AreaIsEdit) {
            map = new HashMap<>();
            map.put("image", R.mipmap.add_area);
//            map.put("name",getString(R.string.config_add));
            areaData_list.add(map);
        }
        if (areaList != null && areaList.size() > 0) {
            for (int i = 0, size = areaList.size(); i < size; i++) {
                map = new HashMap<>();
                if (AreaIsEdit) {
//                    map.put("image", R.mipmap.ic_launcher);
                } else {
//                    map.put("image", R.mipmap.bg);
                }
                map.put("id", areaList.get(i).get("id"));
                map.put("name", areaList.get(i).get("name"));
                areaData_list.add(map);
            }
        }
        area_adapter.notifyDataSetChanged();
        return areaData_list;
    }

    // 左侧区域平移动画
    private void startAnimationLeft() {
        if (IsFinish) {
            IsFinish = false;
            if (IsRight) {
                linearLayout_all.setVisibility(View.VISIBLE);
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, -Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0F
                );
                // 设置一个动画的持续时间
                translateAnimation.setDuration(500);
                // 设置动画是否停留在最后一帧，为true则是停留在最后一帧
                translateAnimation.setFillAfter(true);
                // 给一个动画设置监听，设置类似侦听动画的开始或动画重复的通知
                translateAnimation.setAnimationListener(MainActivity.this);
                // 左侧区域平移出来
                map_right_Relative.startAnimation(translateAnimation);

                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0F
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_robot.startAnimation(translateAnimation);
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0F
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_desk.startAnimation(translateAnimation);
            } else {
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, -Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                translateAnimation.setAnimationListener(MainActivity.this);
                map_right_Relative.startAnimation(translateAnimation);

                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_robot.startAnimation(translateAnimation);
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_desk.startAnimation(translateAnimation);
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        map_right_Relative.clearAnimation();
        if (IsRight) {
            IsRight = false;
        } else {
            IsRight = true;
            linearLayout_all.setVisibility(View.GONE);
        }
        IsFinish = true;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    // FloatButton按钮展开 和 收缩
    @SuppressWarnings("deprecation")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startAnimationShrink() {
        Animation translate;
        if (isShrink) {
            //  默认收缩状态的图标
            findViewById(R.id.shrink).setBackground(getResources().getDrawable(R.animator.btn_shrink_selector));
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_left);
            translate.setAnimationListener(animationListener);
            // 向左运行
            left.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_right);
            translate.setAnimationListener(animationListener);
            // 向右运行
            right.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_up);
            translate.setAnimationListener(animationListener);
            // 前进运行
            up.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_down);
            translate.setAnimationListener(animationListener);
            // 后退运行
            down.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_stop);
            translate.setAnimationListener(animationListener);
            // 停止运行
            stop.startAnimation(translate);
        } else {
            // 点击展开的图标
            findViewById(R.id.shrink).setBackground(getResources().getDrawable(R.animator.btn_shrink_out_selector));
            // 上 下 左 右 停止 的按钮显示出来
            left.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
            up.setVisibility(View.VISIBLE);
            right.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
            // 左运行
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_left);
            translate.setAnimationListener(animationListener);
            left.startAnimation(translate);
            // 右运行
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_right);
            translate.setAnimationListener(animationListener);
            right.startAnimation(translate);
            // 前进运行
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_up);
            translate.setAnimationListener(animationListener);
            up.startAnimation(translate);
            // 后退运行
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_down);
            translate.setAnimationListener(animationListener);
            down.startAnimation(translate);
            // 停止运行
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_stop);
            translate.setAnimationListener(animationListener);
            stop.startAnimation(translate);
        }
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isShrink) {
                // FloatButton收缩
                isShrink = false;
                up.setVisibility(View.GONE);
                down.setVisibility(View.GONE);
                left.setVisibility(View.GONE);
                right.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
            } else {
                // FloatButton展开
                isShrink = true;
                left.setVisibility(View.VISIBLE);
                down.setVisibility(View.VISIBLE);
                up.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    // 初始化区域Dialog
    private MyDialog dialog;
    private EditText editText;
    private TextView title;

    // 区域Dialog
    private void dialog() {
        dialog = new MyDialog(this);
        editText = (EditText) dialog.getEditText();
        // 确定Dialog
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "区域名称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    // 添加新区域
                    robotDBHelper.insert("area", new String[]{"name"}, new Object[]{editText.getText().toString()});
                    // 获取区域数据
                    getAreaData();
                    // 销毁当前Dialog
                    dialog.dismiss();
                }
            }
        });
        // 取消Dialog
        dialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 销毁当前Dialog
                dialog.dismiss();
            }
        });
        // 显示Dialog
        dialog.show();
    }

    // 修改区域Dialog
    private void dialog(String name, final int id) {
        dialog = new MyDialog(this);
        editText = (EditText) dialog.getEditText();
        editText.setText(name);
        title = (TextView) dialog.getTitle();
        // 确定Dialog
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "区域名称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    // 修改区域名称
                    robotDBHelper.execSQL("update area set name= '" + editText.getText().toString().trim() + "' where id= '" + id + "'");
                    dialog.dismiss();
                }
            }
        });

        // 删除Dialog
        dialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除当前区域
                deleteDialog(id);
            }
        });
        // 设置Dialog 左侧取消按钮Text 为 删除
        ((Button) dialog.getNegative()).setText(R.string.btn_delete);
        //显示Dialog
        dialog.show();
    }

    // 机器人运行Dialog
    private RobotDialog robotDialog;

    private void robotDialog(String str) {
        robotDialog = new RobotDialog(this, str);
        robotDialog.show();
    }

    /**
     * 测试
     *
     * @param data 数据
     */
    private void robotDialog(byte[] data) {
        robotDialog = new RobotDialog(this, data);
        robotDialog.show();
    }

    private void robotDialog(List<Map> list) {
        robotDialog = new RobotDialog(this, list);
        robotDialog.show();
    }

    // 删除Dialog
    private DeleteDialog deleteDialog;

    // 根据id删除区域
    private void deleteDialog(final int id) {
        deleteDialog = new DeleteDialog(this);
        deleteDialog.getTemplate().setText("确定删除区域吗？");
        // 确定Dialog
        deleteDialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除区域
                robotDBHelper.execSQL("delete from area where id= '" + id + "'");
                List<Map> deskList;
                // 删除之后再次查询桌面
                deskList = robotDBHelper.queryListMap("select * from desk where area = '" + id + "'", null);
                for (int i = 0, size = deskList.size(); i < size; i++) {
                    // 删除命令
                    robotDBHelper.execSQL("delete from command where desk= '" + deskList.get(i).get("id") + "'");
                }
                // 删除桌面
                robotDBHelper.execSQL("delete from desk where area= '" + id + "'");
                // 获取区域数据
                getAreaData();
                if (areaList != null && areaList.size() > 0) {
                    CURRENT_AREA_id = (int) areaList.get(0).get("id");
                    Current_INDEX = 1;
                    area_text.setText(areaList.get(0).get("name").toString());
                } else {
                    area_text.setText("请选择左侧区域");
                }
                // 获取桌面数据
                getDeskData();
                // 销毁当前Dialog
                deleteDialog.dismiss();
                dialog.dismiss();
            }
        });
        // 取消Dialog
        deleteDialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 销毁当前Dialog
                deleteDialog.dismiss();
            }
        });
        // 显示Dialog
        deleteDialog.show();
    }

    // 注册广播
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String StringE = intent.getStringExtra("msg");
            // 打印log
            Constant.debugLog("msg----->" + StringE);
            if (StringE != null && !StringE.equals("")) {
                // 解析命令
                parseJson(StringE);
            }
        }
    }

    // 解析命令
    public void parseJson(String string) {
        if (string.equals("robot_connect") || string.equals("robot_unconnect")) {
            // 获取机器人数据
            getRobotData();
            // 刷新
            gridViewAdapter.notifyDataSetInvalidated();
            Constant.debugLog("=====连接成功=====");
        } else if (string.equals("robot_receive_succus")) {
            Constant.debugLog("=====收到指令成功=====");
            synchronized (RobotDialog.thread) {
                if (RobotDialog.CurrentIndex == -1) {
                    RobotDialog.CurrentIndex = 0;
                }
                RobotDialog.thread.notify();
            }
        } else if (string.equals("robot_receive_fail")) {
            Constant.debugLog("=====收到指令失败=====");
            if (RobotDialog.flag) {
                RobotDialog.sendCommandList();
            } else {
                RobotDialog.sendCommand();
            }
        } else if (string.equals("robot_destory")) {
            Constant.debugLog("=====销毁机器人=====");
            robotDBHelper.execSQL("update robot set outline= '0' ");
        } else {
        }
    }


}
