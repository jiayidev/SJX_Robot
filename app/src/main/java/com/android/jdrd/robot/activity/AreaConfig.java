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
    private AreaConfigAdapter myAdapter;
    private ListView arealist;
    public static int Current_position = -1;
    private RobotDBHelper robotDBHelper;
    private Map robotconfig;
    private static int robotid;
    private List<Map> area_list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_areaconfig);

        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        Intent intent = getIntent();
        robotid = intent.getIntExtra("id", 0);
        List<Map> robotlist = robotDBHelper.queryListMap("select * from robot where id = '" + robotid + "'", null);
        robotconfig = robotlist.get(0);


        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.add_card).setOnClickListener(this);
        arealist = (ListView) findViewById(R.id.cardlist);
        arealist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                robotDBHelper.execSQL("update robot set area= '" + area_list.get(position).get("id") + "'  where id= '" + robotid + "'");
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        area_list = robotDBHelper.queryListMap("select * from area ", null);
        if (area_list != null && area_list.size() > 0 && 0 != (int) robotconfig.get("area")) {
            for (int i = 0, size = area_list.size(); i < size; i++) {
                if (area_list.get(i).get("id") == robotconfig.get("area")) {
                    Current_position = i;
                    arealist.setSelection(i);
                }
            }
        }
        myAdapter = new AreaConfigAdapter(this, area_list);
        arealist.setAdapter(myAdapter);
    }

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
                Intent intent = new Intent(AreaConfig.this, CardConfigActivity.class);
                intent.putExtra("id", 0);
                startActivity(intent);
                break;
        }
    }
}
