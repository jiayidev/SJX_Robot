package com.android.jdrd.robot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.adapter.MyAdapter;
import com.android.jdrd.robot.adapter.SpinnerAdapter;
import com.android.jdrd.robot.dialog.DeleteDialog;
import com.android.jdrd.robot.dialog.MyDialog;
import com.android.jdrd.robot.dialog.SpinnerDialog;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/8
 * 描述: 编辑命令
 */

public class CommandActivity extends Activity implements View.OnClickListener {
    // 初始化数据库帮助类
    private RobotDBHelper robotDBHelper;
    private int command_id;
    private Map commandConfig;
    private List<Map> goalList;
    private List<Map> list;
    private TextView speed, mp3, outTime, showNum, showColor;
    private TextView goal, direction;
    // 目标数和方向数
    public static int goalNum = -1, directionNum = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_command_config);

        // 初始化数据库
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());

        // 获取DeskConfigPathActivity 传递过来的数据
        Intent intent = getIntent();
        command_id = intent.getIntExtra("id", 0);
        Log.e("commandList----->", command_id + "");

        // 初始化控件
        // 站点
        goal = (TextView) findViewById(R.id.goal);
        goal.setOnClickListener(this);
        // 方向
        direction = (TextView) findViewById(R.id.direction);
        direction.setOnClickListener(this);
        // 编辑页面 速度
        speed = (TextView) findViewById(R.id.speed);
        speed.setOnClickListener(this);
        // 编辑页面 MP3通道
        mp3 = (TextView) findViewById(R.id.mp3);
        mp3.setOnClickListener(this);
        // 编辑页面 超时时间
        outTime = (TextView) findViewById(R.id.outime);
        outTime.setOnClickListener(this);
        // 编辑页面 显示编号
        showNum = (TextView) findViewById(R.id.shownum);
        showNum.setOnClickListener(this);
        // 编辑页面 显示颜色
        showColor = (TextView) findViewById(R.id.showcolor);
        showColor.setOnClickListener(this);
        // 编辑页面 左侧删除按钮
        findViewById(R.id.btn_delete).setOnClickListener(this);
        // 编辑页面 返回按钮
        findViewById(R.id.setting_back).setOnClickListener(this);
        // 编辑页面 返回按钮
        findViewById(R.id.back).setOnClickListener(this);

        // 查询ID卡列表
        goalList = robotDBHelper.queryListMap("select * from card ", null);

        // 存储map的数据
        list = new ArrayList<>();
        // 以键值对来传递值
        Map<String, Object> map;
        // 直行
        map = new HashMap<>();
        map.put("name", "直行");
        list.add(map);
        // 左岔道
        map = new HashMap<>();
        map.put("name", "左岔道");
        list.add(map);
        // 右岔道
        map = new HashMap<>();
        map.put("name", "右岔道");
        list.add(map);

        // 查询命令列表
        List<Map> commandList = robotDBHelper.queryListMap("select * from command where id = '" + command_id + "'", null);
        if (commandList != null && commandList.size() > 0) {
            commandConfig = commandList.get(0);
            if (commandConfig.get("goal") != null) {
                if (goalList != null && goalList.size() > 0) {
                    boolean flag = false;
                    for (int i = 0, size = goalList.size(); i < size; i++) {
                        if (goalList.get(i).get("id").equals(commandConfig.get("goal"))) {
                            goal.setText(goalList.get(i).get("name").toString());
                            goalNum = i;
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        goal.setText("请选择站点");
                    }
                }
            } else {
                goal.setText("请选择站点");
            }
            // 获取机器人运行的目标
            if (commandConfig.get("direction") != null) {
                directionNum = (int) commandConfig.get("direction");
                // 设置机器人运行的方向 0->直行  1->左岔道  2->右岔道
                switch (directionNum) {
                    case 0:
                        direction.setText("直行");
                        break;
                    case 1:
                        direction.setText("左岔道");
                        break;
                    case 2:
                        direction.setText("右岔道");
                        break;
                }
            } else {
                direction.setText("请选择方向");
            }

            // 设置机器人运行时的速度
            if (commandConfig.get("speed") != null) {
                speed.setText(commandConfig.get("speed").toString().trim());
            }
            // 设置机器人运行时的音乐
            if (commandConfig.get("music") != null) {
                mp3.setText(commandConfig.get("music").toString());
            }
            // 设置机器人运行时的超时时间
            if (commandConfig.get("outime") != null) {
                outTime.setText(commandConfig.get("outime").toString());
            }
            // 设置机器人运行时的显示编号
            if (commandConfig.get("shownumber") != null) {
                showNum.setText(commandConfig.get("shownumber").toString());
            }
            // 设置机器人运行时的显示颜色
            if (commandConfig.get("showcolor") != null) {
                showColor.setText(commandConfig.get("showcolor").toString());
            }

            switch ((int) commandConfig.get("type")) {
                case 0:
                    break;
                case 1:
                    findViewById(R.id.linear_goal).setVisibility(View.GONE);
                    findViewById(R.id.linear_direction).setVisibility(View.GONE);
                    break;
                case 2:
                    findViewById(R.id.linear_goal).setVisibility(View.GONE);
                    findViewById(R.id.linear_direction).setVisibility(View.GONE);
                    ((TextView) findViewById(R.id.speed_text)).setText("旋转速度");
                    break;
                case 3:
                    findViewById(R.id.linear_goal).setVisibility(View.GONE);
                    findViewById(R.id.linear_direction).setVisibility(View.GONE);
                    findViewById(R.id.linear_speed).setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
        findViewById(R.id.btn_sure).setOnClickListener(this);
    }

    /**
     * 按钮点击事件
     *
     * @param v 获取按钮的ID
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
            // 删除
            case R.id.btn_delete:
                dialog();
                break;
            // 站点
            case R.id.goal:
                dialog_spinner(true);
                break;
            // 方向
            case R.id.direction:
                dialog_spinner(false);
                break;
            // 速度
            case R.id.speed:
                dialog_Text(0);
                break;
            // MP3音乐
            case R.id.mp3:
                dialog_Text(1);
                break;
            case R.id.outime:
                dialog_Text(2);
                break;
            // 显示编号
            case R.id.shownum:
                dialog_Text(3);
                break;
            // 显示颜色
            case R.id.showcolor:
                dialog_Text(4);
                break;
            // 确定
            case R.id.btn_sure:
                switch ((int) commandConfig.get("type")) {
                    // 速度
                    case 0:
                        if (!speed.getText().toString().equals("") && !mp3.getText().toString().equals("") && !outTime.getText().toString().equals("")
                                && !showNum.getText().toString().equals("") && !showColor.getText().toString().equals("") && goalList != null && goalList.size() > 0) {
                            // 修改速度
                            robotDBHelper.execSQL("update command set speed = '" + speed.getText().toString().trim() + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outTime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + showNum.getText().toString().trim() + "' ,showcolor = '" + showColor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 站点
                    case 1:
                        if (!speed.getText().toString().equals("") && !mp3.getText().toString().equals("") && !outTime.getText().toString().equals("")
                                && !showNum.getText().toString().equals("") && !showColor.getText().toString().equals("")) {
                            // 修改站点
                            robotDBHelper.execSQL("update command set goal= '" + 0 + "' ," +
                                    "direction = '" + 0 + "' ,speed = '" + speed.getText().toString().trim() + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outTime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + showNum.getText().toString().trim() + "' ,showcolor = '" + showColor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 站点
                    case 2:
                        if (!speed.getText().toString().equals("") && !mp3.getText().toString().equals("") && !outTime.getText().toString().equals("")
                                && !showNum.getText().toString().equals("") && !showColor.getText().toString().equals("")) {
                            // 修改站点
                            robotDBHelper.execSQL("update command set goal= '" + 0 + "' ," +
                                    "direction = '" + 0 + "' ,speed = '" + speed.getText().toString().trim() + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outTime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + showNum.getText().toString().trim() + "' ,showcolor = '" + showColor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 站点
                    case 3:
                        if (!mp3.getText().toString().equals("") && !outTime.getText().toString().equals("")
                                && !showNum.getText().toString().equals("") && !showColor.getText().toString().equals("")) {
                            // 修改站点
                            robotDBHelper.execSQL("update command set goal= '" + 0 + "' ," +
                                    "direction = '" + 0 + "' ,speed = '" + 0 + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outTime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + showNum.getText().toString().trim() + "' ,showcolor = '" + showColor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private DeleteDialog dialog;

    // 删除命令Dialog
    private void dialog() {
        dialog = new DeleteDialog(this);
        // 确定删除
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除命令
                robotDBHelper.execSQL("delete from command where id = '" + command_id + "'");
                finish();
            }
        });
        // 取消按钮
        dialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 销毁Dialog
                dialog.dismiss();
            }
        });
        // 显示Dialog
        dialog.show();
    }

    private SpinnerDialog spinnerdialog;
    private SpinnerAdapter spinnerAdapter;

    // 自定义运动到站点对话框
    private void dialog_spinner(final boolean gl) {
        spinnerdialog = new SpinnerDialog(this);
        if (gl) {
            spinnerAdapter = new SpinnerAdapter(this, goalList, gl);
        } else {
            spinnerAdapter = new SpinnerAdapter(this, list, gl);
        }
        // 加载适配器
        spinnerdialog.getListView().setAdapter(spinnerAdapter);
        // 子列表点击事件
        spinnerdialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (gl) {
                    goalNum = position;
                } else {
                    directionNum = position;
                }
                spinnerAdapter.notifyDataSetChanged();
            }
        });
        // 确定Dialog
        spinnerdialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gl) {
                    if (goalList != null && goalList.size() > 0 && goalNum != -1) {
                        robotDBHelper.execSQL("update command set goal= '" + goalList.get(goalNum).get("id") + "' where id= '" + command_id + "'");
                        goal.setText(goalList.get(goalNum).get("name").toString());
                        spinnerdialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "请选择站点系统卡", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    robotDBHelper.execSQL("update command set direction = '" + directionNum + "' where id= '" + command_id + "'");
                    switch (directionNum) {
                        case 0:
                            direction.setText("直行");
                            break;
                        case 1:
                            direction.setText("左岔道");
                            break;
                        case 2:
                            direction.setText("右岔道");
                            break;
                    }
                    spinnerdialog.dismiss();
                }
            }
        });
        // 取消Dialog
        spinnerdialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 销毁窗体
                spinnerdialog.dismiss();
            }
        });
        // 显示Dialog
        spinnerdialog.show();
    }


    private MyDialog textDialog;
    private EditText editText;

    private void dialog_Text(final int type) {
        textDialog = new MyDialog(this);
        // type=  0->速度  1->MP3通道  2->超时时间  3->显示编号  4->显示颜色
        switch (type) {
            case 0:
                textDialog.getTitle().setText("速度修改");
                textDialog.getTitleTemp().setText("请输入最新速度值");
                break;
            case 1:
                textDialog.getTitle().setText("MP3通道修改");
                textDialog.getTitleTemp().setText("请输入MP3通道");
                break;
            case 2:
                textDialog.getTitle().setText("超时时间修改");
                textDialog.getTitleTemp().setText("请输入超时时间");
                break;
            case 3:
                textDialog.getTitle().setText("显示编号修改");
                textDialog.getTitleTemp().setText("请输入显示编号");
                break;
            case 4:
                textDialog.getTitle().setText("显示颜色修改");
                textDialog.getTitleTemp().setText("请输入显示颜色");
                break;
        }
        editText = (EditText) textDialog.getEditText();
        // 输入类型为数字文本
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        // 确定Dialog
        textDialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 修改数据库 type=  0->修改速度  1->修改MP3通道  2->修改超时时间  3->修改显示编号  4->修改显示颜色
                switch (type) {
                    // 修改速度
                    case 0:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set speed = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textDialog.dismiss();
                            speed.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 修改MP3通道
                    case 1:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set music = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textDialog.dismiss();
                            mp3.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 修改超时时间
                    case 2:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set outime = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textDialog.dismiss();
                            outTime.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 修改显示编号
                    case 3:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set shownumber = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textDialog.dismiss();
                            showNum.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    // 修改显示颜色
                    case 4:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set showcolor = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textDialog.dismiss();
                            showColor.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
        // 取消Dialog
        textDialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 销毁窗体
                textDialog.dismiss();
            }
        });
        // 显示Dialog
        textDialog.show();
    }
}
