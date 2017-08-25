package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.adapter.CardAdapter;
import com.android.jdrd.robot.dialog.DeleteDialog;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/17
 * 描述: 系统卡编辑页面
 */

public class CardConfigActivity extends Activity implements View.OnClickListener {
    private RobotDBHelper robotDBHelper;
    private int card_id;
    private EditText cardName, cardAdress;
    private Button btn_sure, btn_delete;
    private List<Map> card_list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cardactivity);
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());

        Intent intent = getIntent();// 收取 email
        card_id = intent.getIntExtra("id", 0);

        cardName = (EditText) findViewById(R.id.cardname);
        cardAdress = (EditText) findViewById(R.id.cardaddress);
        btn_sure = (Button) findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(this);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        Constant.debugLog("card_id" + card_id);
        if (card_id == 0) {
            btn_delete.setVisibility(View.GONE);
        } else {
            card_list = robotDBHelper.queryListMap("select * from card where id = '" + card_id + "'", null);
            Constant.debugLog("card_list" + card_list.toString());
            cardName.setText(card_list.get(0).get("name").toString());
            cardAdress.setText(card_list.get(0).get("address").toString());
        }
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
            case R.id.btn_delete:
                dialog();
                break;
            case R.id.btn_sure:
                if (card_id == 0) {
                    robotDBHelper.insert("card", new String[]{"name", "address"}, new Object[]{cardName.getText().toString(), cardAdress.getText().toString().trim()});
                } else {
                    robotDBHelper.execSQL("update card set name= '" + cardName.getText().toString().trim() + "',address = '" + cardAdress.getText().toString().trim() + "'  where id= '" + card_id + "'");
                }
                finish();
                break;
        }
    }

    private DeleteDialog dialog;

    private void dialog() {
        dialog = new DeleteDialog(this);
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotDBHelper.execSQL("delete from card where id= '" + card_id + "'");
                finish();
            }
        });
        dialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
