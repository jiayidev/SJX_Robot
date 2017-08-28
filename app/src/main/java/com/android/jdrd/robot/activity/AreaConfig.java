package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.adapter.AreaConfigAdapter;
import com.android.jdrd.robot.adapter.CardAdapter;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/9
 * 描述: 系统卡列表
 */

public class AreaConfig extends Activity implements View.OnClickListener {
    // 区域信息适配器
    private AreaConfigAdapter myAdapter;
    // 加载区域列表
    private ListView areaList;
    // 数据库帮助类
    private RobotDBHelper robotDBHelper;
    // 机器人信息
    private Map robotConfig;
    // 存储区域列表
    private List<Map> area_list = new ArrayList<>();
    // 当前的下标
    public static int Current_position = -1;
    private static int robotId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_areaconfig);

        // 初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());

        // 获取RobotConfigActivity传递过来的Id
        Intent intent = getIntent();
        robotId = intent.getIntExtra("id", 0);

        // 查询机器人列表
        List<Map> robotList = robotDBHelper.queryListMap("select * from robot where id = '" + robotId + "'", null);
        robotConfig = robotList.get(0);


        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.add_card).setOnClickListener(this);
        areaList = (ListView) findViewById(R.id.cardlist);
        // 子列点击事件
        areaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                robotDBHelper.execSQL("update robot set area= '" + area_list.get(position).get("id") + "'  where id= '" + robotId + "'");
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 查询区域
        area_list = robotDBHelper.queryListMap("select * from area ", null);
        if (area_list != null && area_list.size() > 0 && 0 != (int) robotConfig.get("area")) {
            for (int i = 0, size = area_list.size(); i < size; i++) {
                if (area_list.get(i).get("id") == robotConfig.get("area")) {
                    Current_position = i;
                    areaList.setSelection(i);
                }
            }
        }
        // 显示到List列表上
        myAdapter = new AreaConfigAdapter(this, area_list);
        areaList.setAdapter(myAdapter);
    }

    /**
     * 点击事件
     *
     * @param v 按钮的ID
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 返回
            case R.id.setting_back:
                finish();
                break;
            // 返回
            case R.id.back:
                finish();
                break;
            // 确定
            case R.id.add_card:
                // 向CardConfigActivity传递数据
                Intent intent = new Intent(AreaConfig.this, CardConfigActivity.class);
                intent.putExtra("id", 0);
                startActivity(intent);
                break;
        }
    }
}
