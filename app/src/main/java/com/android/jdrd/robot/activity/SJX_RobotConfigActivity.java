package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/17
 * 描述: 机器人编辑页面
 */

public class SJX_RobotConfigActivity extends Activity implements View.OnClickListener {
    // 初始化数据库类
    private RobotDBHelper robotDBHelper;
    // 机器人id
    private static int robotId;
    // 机器人信息
    private Map robotConfig;
    // 机器人名称
    private EditText name;
    // 区域名称
    private TextView area_text;
    // 区域id
    public int areaId;
    // 存储区域数据
    private List<Map> areaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sjx_activity_robot_config);

        //初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        // 机器人名称
        name = ((EditText) findViewById(R.id.name));
        // 确定
        findViewById(R.id.sure).setOnClickListener(this);
        // 返回
        findViewById(R.id.setting_back).setOnClickListener(this);
        // 返回
        findViewById(R.id.back).setOnClickListener(this);
        // 机器人编辑左侧  ->区域
        area_text = (TextView) findViewById(R.id.area_text);
        // 机器人编辑右侧  ->必选
        findViewById(R.id.area).setOnClickListener(this);
        // 获取RobotActivity传递过来的Id
        Intent intent = getIntent();
        robotId = intent.getIntExtra("id", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 查询机器人列表 根据id查询
        List<Map> robotList = robotDBHelper.queryListMap("select * from robot where id = '" + robotId + "'", null);
        robotConfig = robotList.get(0);
        name.setHint(robotConfig.get("name").toString());
        // 查询区域列表
        areaList = robotDBHelper.queryListMap("select * from area ", null);
        areaId = (int) robotConfig.get("area");
        area_text.setText("未选择区域");
        if (areaList != null && areaList.size() > 0) {
            if (areaId != 0) {
                for (int i = 0, size = areaList.size(); i < size; i++) {
                    if (areaList.get(i).get("id").equals(areaId)) {
                        area_text.setText(areaList.get(i).get("name").toString());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击确定按钮
            case R.id.sure:
                Constant.debugLog("区域areaId----->" + areaId);
                if (!name.getText().toString().trim().equals("")) {
                    // 修改机器人名称
                    robotDBHelper.execSQL("update robot set name= '" + name.getText().toString().trim() + "'  where id= '" + robotId + "'");
                } else {
                    // 获取原有机器人名称 然后再修改
                    robotDBHelper.execSQL("update robot set name= '" + robotConfig.get("name").toString() + "'  where id= '" + robotId + "'");
                }
                finish();
                break;
            // 返回
            case R.id.setting_back:
                finish();
                break;
            // 返回
            case R.id.back:
                finish();
                break;
            // 区域
            case R.id.area:
                // 跳转到系统卡列表AreaConfig 并传递id
                Intent intent = new Intent(SJX_RobotConfigActivity.this, SJX_AreaConfig.class);
                intent.putExtra("id", robotId);
                startActivity(intent);
                break;
        }
    }
}
