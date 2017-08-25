package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/17
 * 描述: 机器人编辑页面
 */

public class RobotConfigActivity extends Activity implements View.OnClickListener {
    private RobotDBHelper robotDBHelper;
    private static int robotid;
    private Map robotconfig;
    private EditText name;
    private TextView area_text;
    public int areaid;
    private List<Map> arealist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_robot_config);
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        name = ((EditText) findViewById(R.id.name));
        findViewById(R.id.sure).setOnClickListener(this);
        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        area_text = (TextView) findViewById(R.id.area_text);
        findViewById(R.id.area).setOnClickListener(this);
        Intent intent = getIntent();
        robotid = intent.getIntExtra("id", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Map> robotlist = robotDBHelper.queryListMap("select * from robot where id = '" + robotid + "'", null);
        robotconfig = robotlist.get(0);
        name.setHint(robotconfig.get("name").toString());
        arealist = robotDBHelper.queryListMap("select * from area ", null);
        areaid = (int) robotconfig.get("area");
        area_text.setText("未选择区域");
        if (arealist != null && arealist.size() > 0) {
            if (areaid != 0) {
                for (int i = 0, size = arealist.size(); i < size; i++) {
                    if (arealist.get(i).get("id").equals(areaid)) {
                        area_text.setText(arealist.get(i).get("name").toString());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sure:
                Constant.debugLog("areaid" + areaid);
                if (!name.getText().toString().trim().equals("")) {
                    robotDBHelper.execSQL("update robot set name= '" + name.getText().toString().trim() + "'  where id= '" + robotid + "'");
                } else {
                    robotDBHelper.execSQL("update robot set name= '" + robotconfig.get("name").toString() + "'  where id= '" + robotid + "'");
                }
                finish();
                break;
            case R.id.setting_back:
                finish();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.area:
                Intent intent = new Intent(RobotConfigActivity.this, AreaConfig.class);
                intent.putExtra("id", robotid);
                startActivity(intent);
                break;
        }
    }
}
