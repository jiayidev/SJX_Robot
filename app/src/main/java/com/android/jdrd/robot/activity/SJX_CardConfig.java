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
import com.android.jdrd.robot.adapter.SJX_CardAdapter;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/17
 * 描述:系统卡列表
 */

public class SJX_CardConfig extends Activity implements View.OnClickListener {
    // 系统卡适配器
    private SJX_CardAdapter myAdapter;
    // 卡列表
    private ListView cardList;
    // 数据库帮助类
    private RobotDBHelper robotDBHelper;
    // 存储系统卡
    private List<Map> card_list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sjx_activity_card);

        // 初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());

        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.add_card).setOnClickListener(this);
        cardList = (ListView) findViewById(R.id.cardlist);
        // 子列表点击事件
        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 向CardConfigActivity传递数据
                Intent intent = new Intent(SJX_CardConfig.this, SJX_CardConfigActivity.class);
                intent.putExtra("id", (Integer) card_list.get(position).get("id"));
                // 打印Log日志
                Constant.debugLog("id----->" + card_list.get(position).get("id").toString());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 查询ID卡
        card_list = robotDBHelper.queryListMap("select * from card ", null);
        myAdapter = new SJX_CardAdapter(this, card_list);
        // 加载适配器
        cardList.setAdapter(myAdapter);
    }

    /**
     * 按钮点击事件
     *
     * @param v 获取按钮id
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_back:
                finish();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.add_card:
                // 跳转到系统卡编辑页面CardConfigActivity 并传值
                Intent intent = new Intent(SJX_CardConfig.this, SJX_CardConfigActivity.class);
                intent.putExtra("id", 0);
                startActivity(intent);
                break;
        }
    }
}
