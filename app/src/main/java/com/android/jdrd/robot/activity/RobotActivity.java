package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.dialog.RobotDialog;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/16
 * 描述: 机器人状态
 */

public class RobotActivity extends Activity implements View.OnClickListener {
    private RobotDBHelper robotDBHelper;
    private int robotid;
    private MyReceiver receiver;
    private Map robotconfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_robot);
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        Intent intent =getIntent();
        robotid = intent.getIntExtra("id",0);
        findViewById(R.id.setting_redact).setOnClickListener(this);
        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.jdrd.activity.Robot");
        if(receiver != null ){
            this.registerReceiver(receiver,filter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( receiver !=null ) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init(){
        List<Map> robotlist = robotDBHelper.queryListMap("select * from robot where id = '"+ robotid +"'" ,null);
        robotconfig = robotlist.get(0);
        Constant.debugLog("robot     "+robotlist.toString());
        Constant.debugLog("robotconfig     "+robotconfig.toString());

        List<Map> arealist = robotDBHelper.queryListMap("select * from area where id = '"+ robotconfig.get("area") +"'" ,null);
        Constant.debugLog(arealist.toString());
        if(arealist != null && arealist.size()>0){
            ((TextView)findViewById(R.id.area)).setText(arealist.get(0).get("name").toString());
        }
        if((int)robotconfig.get("outline") == 1){
            ((TextView)findViewById(R.id.outline)).setText("在线");
        }else{
            ((TextView)findViewById(R.id.outline)).setText("离线");
        }
        if((int)robotconfig.get("robotstate") == 0){
            ((TextView)findViewById(R.id.robot_state)).setText("空闲");
        }else if((int)robotconfig.get("robotstate") == 1){
            ((TextView)findViewById(R.id.robot_state)).setText("送餐");
        }else if((int)robotconfig.get("robotstate") == 2){
            ((TextView)findViewById(R.id.robot_state)).setText("故障");
        }
        if((int)robotconfig.get("state") == 0){
            ((TextView)findViewById(R.id.state)).setText("直行前进");
        }else if((int)robotconfig.get("state") == 1){
            ((TextView)findViewById(R.id.state)).setText("左转");
        }else if((int)robotconfig.get("state") == 2){
            ((TextView)findViewById(R.id.state)).setText("右转");
        }else if((int)robotconfig.get("state") == 3){
            ((TextView)findViewById(R.id.state)).setText("旋转");
        }
        ((TextView)findViewById(R.id.name)).setText(robotconfig.get("name").toString());
        ((TextView)findViewById(R.id.ip)).setText(robotconfig.get("ip").toString());
        ((TextView)findViewById(R.id.electric)).setText(robotconfig.get("electric").toString());
        ((TextView)findViewById(R.id.command_num)).setText(robotconfig.get("commandnum").toString());
        ((TextView)findViewById(R.id.last_location)).setText(robotconfig.get("lastlocation").toString());
        if((int)robotconfig.get("obstacle") == 0){
            ((TextView)findViewById(R.id.obstacle)).setText("无");
        }else{
            ((TextView)findViewById(R.id.obstacle)).setText("有");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.setting_redact:
                Intent intent = new Intent(RobotActivity.this, RobotConfigActivity.class);
                intent.putExtra("id", robotid);
                startActivity(intent);
                break;
            case R.id.setting_back:
                finish();
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String StringE = intent.getStringExtra("msg");
            Constant.debugLog("msg"+StringE);
            if(StringE !=null && !StringE.equals("")){
                pasreJson(StringE);
            }
        }
    }
    public void pasreJson(String string){
        if(string.equals("robot")){
            init();
        }else {
        }
    }
}
