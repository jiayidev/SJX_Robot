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
    // 数据库帮助类
    private RobotDBHelper robotDBHelper;
    // 编号
    private int card_id;
    // 卡名称和地址
    private EditText cardName, cardAddress;
    // 确定和删除按钮
    private Button btn_sure, btn_delete;
    // 存储系统卡列表
    private List<Map> card_list = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cardactivity);

        // 初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());

        // 获取CardConfig传递过来的数据
        Intent intent = getIntent();
        card_id = intent.getIntExtra("id", 0);

        // 初始化控件
        cardName = (EditText) findViewById(R.id.cardname);
        cardAddress = (EditText) findViewById(R.id.cardaddress);
        btn_sure = (Button) findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(this);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        // 打印log
        Constant.debugLog("card_id----->" + card_id);
        // 等于card_id == 0的话删除按钮不显示
        if (card_id == 0) {
            btn_delete.setVisibility(View.GONE);
        } else {
            // 查询ID卡
            card_list = robotDBHelper.queryListMap("select * from card where id = '" + card_id + "'", null);
            //打印log card_list列表
            Constant.debugLog("card_list----->" + card_list.toString());
            cardName.setText(card_list.get(0).get("name").toString());
            cardAddress.setText(card_list.get(0).get("address").toString());
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
            // 删除
            case R.id.btn_delete:
                dialog();
                break;
            // 确定
            case R.id.btn_sure:
                // 等于0的话没有数据，则添加一条数据
                if (card_id == 0) {
                    robotDBHelper.insert("card", new String[]{"name", "address"}, new Object[]{cardName.getText().toString(), cardAddress.getText().toString().trim()});
                } else {
                    //不等于0的话有数据，则修改name and address
                    robotDBHelper.execSQL("update card set name= '" + cardName.getText().toString().trim() + "',address = '" + cardAddress.getText().toString().trim() + "'  where id= '" + card_id + "'");
                }
                finish();
                break;
        }
    }

    private DeleteDialog dialog;

    private void dialog() {
        dialog = new DeleteDialog(this);
        // Dialog 确定按钮
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除ID卡
                robotDBHelper.execSQL("delete from card where id= '" + card_id + "'");
                finish();
            }
        });
        // Dialog 取消按钮
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
}
