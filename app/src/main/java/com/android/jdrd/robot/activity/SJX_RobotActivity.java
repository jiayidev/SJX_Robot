package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/16
 * 描述: 机器人状态
 */

public class SJX_RobotActivity extends Activity implements View.OnClickListener {
    // 数据库帮助类
    private RobotDBHelper robotDBHelper;
    // 机器人id
    private int robotId;
    // 初始化广播
    private MyReceiver receiver;
    // 机器人信息
    private Map robotConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sjx_activity_robot);
        //初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        // 获取MainActivity传递的Id
        Intent intent = getIntent();
        robotId = intent.getIntExtra("id", 0);

        // 设置
        findViewById(R.id.setting_redact).setOnClickListener(this);
        // 返回
        findViewById(R.id.setting_back).setOnClickListener(this);
        // 返回
        findViewById(R.id.back).setOnClickListener(this);

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.jdrd.activity.Robot");
        // 注册广播
        if (receiver != null) {
            this.registerReceiver(receiver, filter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 初始化数据
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册广播
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 初始化数据
     */
    private void init() {
        // 查询机器人列表 根据id查询
        List<Map> robotList = robotDBHelper.queryListMap("select * from robot where id = '" + robotId + "'", null);
        robotConfig = robotList.get(0);
        Constant.debugLog("robot----->" + robotList.toString());
        Constant.debugLog("robotConfig----->" + robotConfig.toString());

        //查询区域列表 根据id查询
        List<Map> areaList = robotDBHelper.queryListMap("select * from area where id = '" + robotConfig.get("area") + "'", null);
        // 打印Log
        Constant.debugLog(areaList.toString());
        if (areaList != null && areaList.size() > 0) {
            ((TextView) findViewById(R.id.area)).setText(areaList.get(0).get("name").toString());
        }
        // 机器人在线状态   1->在线     0->离线
        if ((int) robotConfig.get("outline") == 1) {
            ((TextView) findViewById(R.id.outline)).setText("在线");
        } else {
            ((TextView) findViewById(R.id.outline)).setText("离线");
        }
        //机器人状态  0->空闲   1->送餐   2->故障
        if ((int) robotConfig.get("robotstate") == 0) {
            ((TextView) findViewById(R.id.robot_state)).setText("空闲");
        } else if ((int) robotConfig.get("robotstate") == 1) {
            ((TextView) findViewById(R.id.robot_state)).setText("送餐");
        } else if ((int) robotConfig.get("robotstate") == 2) {
            ((TextView) findViewById(R.id.robot_state)).setText("故障");
        }
        // 机器人运行状态  0->直行前进    0->左转    0->右转    0->旋转
        if ((int) robotConfig.get("state") == 0) {
            ((TextView) findViewById(R.id.state)).setText("直行前进");
        } else if ((int) robotConfig.get("state") == 1) {
            ((TextView) findViewById(R.id.state)).setText("左转");
        } else if ((int) robotConfig.get("state") == 2) {
            ((TextView) findViewById(R.id.state)).setText("右转");
        } else if ((int) robotConfig.get("state") == 3) {
            ((TextView) findViewById(R.id.state)).setText("旋转");
        }

        // 获取信息
        ((TextView) findViewById(R.id.name)).setText(robotConfig.get("name").toString());
        ((TextView) findViewById(R.id.ip)).setText(robotConfig.get("ip").toString());
        ((TextView) findViewById(R.id.electric)).setText(robotConfig.get("electric").toString());
        ((TextView) findViewById(R.id.command_num)).setText(robotConfig.get("commandnum").toString());
        ((TextView) findViewById(R.id.last_location)).setText(robotConfig.get("lastlocation").toString());
        // 是否有障碍物 0->无  1->有
        if ((int) robotConfig.get("obstacle") == 0) {
            ((TextView) findViewById(R.id.obstacle)).setText("无");
        } else {
            ((TextView) findViewById(R.id.obstacle)).setText("有");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 机器人状态设置
            case R.id.setting_redact:
                // 跳转到 机器人编辑页面 并传递id
                Intent intent = new Intent(SJX_RobotActivity.this, SJX_RobotConfigActivity.class);
                intent.putExtra("id", robotId);
                startActivity(intent);
                break;
            // 返回
            case R.id.setting_back:
                finish();
                break;
            // 返回
            case R.id.back:
                finish();
                break;
        }
    }

    // 广播监听
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String StringE = intent.getStringExtra("msg");
            // 打印log
            Constant.debugLog("msg----->" + StringE);
            if (StringE != null && !StringE.equals("")) {
                parseJson(StringE);
            }
        }
    }

    //解析数据
    public void parseJson(String string) {
        if (string.equals("robot")) {
            init();
        } else {
        }
    }
}
