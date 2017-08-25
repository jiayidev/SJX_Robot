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

public class CommandAcitivty extends Activity implements View.OnClickListener {
    private RobotDBHelper robotDBHelper;
    private int command_id;
    private Map commandconfig;
    private List<Map> goallist;
    private List<Map> list;
    private TextView speed, mp3, outime, shownum, showcolor;
    private TextView goal, direction;
    public static int goalnum = -1, directionnum = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_command_config);
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        Intent intent = getIntent();
        command_id = intent.getIntExtra("id", 0);
        Log.e("commandlist", command_id + "");
        goal = (TextView) findViewById(R.id.goal);
        direction = (TextView) findViewById(R.id.direction);
        goal.setOnClickListener(this);
        direction.setOnClickListener(this);
        speed = (TextView) findViewById(R.id.speed);
        speed.setOnClickListener(this);
        mp3 = (TextView) findViewById(R.id.mp3);
        mp3.setOnClickListener(this);
        outime = (TextView) findViewById(R.id.outime);
        outime.setOnClickListener(this);
        shownum = (TextView) findViewById(R.id.shownum);
        shownum.setOnClickListener(this);
        showcolor = (TextView) findViewById(R.id.showcolor);
        showcolor.setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.setting_back).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        goallist = robotDBHelper.queryListMap("select * from card ", null);

        list = new ArrayList<>();
        Map<String, Object> map;
        map = new HashMap<>();
        map.put("name", "直行");
        list.add(map);
        map = new HashMap<>();
        map.put("name", "左岔道");
        list.add(map);
        map = new HashMap<>();
        map.put("name", "右岔道");
        list.add(map);

        List<Map> commandlist = robotDBHelper.queryListMap("select * from command where id = '" + command_id + "'", null);
        if (commandlist != null && commandlist.size() > 0) {
            commandconfig = commandlist.get(0);
            if (commandconfig.get("goal") != null) {
                if (goallist != null && goallist.size() > 0) {
                    boolean flag = false;
                    for (int i = 0, size = goallist.size(); i < size; i++) {
                        if (goallist.get(i).get("id").equals(commandconfig.get("goal"))) {
                            goal.setText(goallist.get(i).get("name").toString());
                            goalnum = i;
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
            if (commandconfig.get("direction") != null) {
                directionnum = (int) commandconfig.get("direction");
                switch (directionnum) {
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

            if (commandconfig.get("speed") != null) {
                speed.setText(commandconfig.get("speed").toString().trim());
            }
            if (commandconfig.get("music") != null) {
                mp3.setText(commandconfig.get("music").toString());
            }
            if (commandconfig.get("outime") != null) {
                outime.setText(commandconfig.get("outime").toString());
            }
            if (commandconfig.get("shownumber") != null) {
                shownum.setText(commandconfig.get("shownumber").toString());
            }
            if (commandconfig.get("showcolor") != null) {
                showcolor.setText(commandconfig.get("showcolor").toString());
            }
            switch ((int) commandconfig.get("type")) {
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


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            case R.id.goal:
                dialog_spinner(true);
                break;
            case R.id.direction:
                dialog_spinner(false);
                break;
            case R.id.speed:
                dialog_Text(0);
                break;
            case R.id.mp3:
                dialog_Text(1);
                break;
            case R.id.outime:
                dialog_Text(2);
                break;
            case R.id.shownum:
                dialog_Text(3);
                break;
            case R.id.showcolor:
                dialog_Text(4);
                break;
            case R.id.btn_sure:
                switch ((int) commandconfig.get("type")) {
                    case 0:
                        if (!speed.getText().toString().equals("") && !mp3.getText().toString().equals("") && !outime.getText().toString().equals("")
                                && !shownum.getText().toString().equals("") && !showcolor.getText().toString().equals("") && goallist != null && goallist.size() > 0) {
                            robotDBHelper.execSQL("update command set speed = '" + speed.getText().toString().trim() + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + shownum.getText().toString().trim() + "' ,showcolor = '" + showcolor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (!speed.getText().toString().equals("") && !mp3.getText().toString().equals("") && !outime.getText().toString().equals("")
                                && !shownum.getText().toString().equals("") && !showcolor.getText().toString().equals("")) {
                            robotDBHelper.execSQL("update command set goal= '" + 0 + "' ," +
                                    "direction = '" + 0 + "' ,speed = '" + speed.getText().toString().trim() + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + shownum.getText().toString().trim() + "' ,showcolor = '" + showcolor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if (!speed.getText().toString().equals("") && !mp3.getText().toString().equals("") && !outime.getText().toString().equals("")
                                && !shownum.getText().toString().equals("") && !showcolor.getText().toString().equals("")) {
                            robotDBHelper.execSQL("update command set goal= '" + 0 + "' ," +
                                    "direction = '" + 0 + "' ,speed = '" + speed.getText().toString().trim() + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + shownum.getText().toString().trim() + "' ,showcolor = '" + showcolor.getText().toString().trim() + "' where id= '" + command_id + "'");
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "请选择确认好参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (!mp3.getText().toString().equals("") && !outime.getText().toString().equals("")
                                && !shownum.getText().toString().equals("") && !showcolor.getText().toString().equals("")) {
                            robotDBHelper.execSQL("update command set goal= '" + 0 + "' ," +
                                    "direction = '" + 0 + "' ,speed = '" + 0 + "'," +
                                    "music = '" + mp3.getText().toString().trim() + "' ,outime = '" + outime.getText().toString().trim() + "' ," +
                                    "shownumber = '" + shownum.getText().toString().trim() + "' ,showcolor = '" + showcolor.getText().toString().trim() + "' where id= '" + command_id + "'");
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

    private void dialog() {
        dialog = new DeleteDialog(this);
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotDBHelper.execSQL("delete from command where id = '" + command_id + "'");
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

    private SpinnerDialog spinnerdialog;
    private SpinnerAdapter spinnerAdapter;

    private void dialog_spinner(final boolean gl) {
        spinnerdialog = new SpinnerDialog(this);
        if (gl) {
            spinnerAdapter = new SpinnerAdapter(this, goallist, gl);
        } else {
            spinnerAdapter = new SpinnerAdapter(this, list, gl);
        }
        spinnerdialog.getListView().setAdapter(spinnerAdapter);
        spinnerdialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (gl) {
                    goalnum = position;
                } else {
                    directionnum = position;
                }
                spinnerAdapter.notifyDataSetChanged();
            }
        });
        spinnerdialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gl) {
                    if (goallist != null && goallist.size() > 0 && goalnum != -1) {
                        robotDBHelper.execSQL("update command set goal= '" + goallist.get(goalnum).get("id") + "' where id= '" + command_id + "'");
                        goal.setText(goallist.get(goalnum).get("name").toString());
                        spinnerdialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "请选择站点系统卡", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    robotDBHelper.execSQL("update command set direction = '" + directionnum + "' where id= '" + command_id + "'");
                    switch (directionnum) {
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
        spinnerdialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerdialog.dismiss();
            }
        });
        spinnerdialog.show();
    }


    private MyDialog textdialog;
    private EditText editText;

    private void dialog_Text(final int type) {
        textdialog = new MyDialog(this);
        switch (type) {
            case 0:
                textdialog.getTitle().setText("速度修改");
                textdialog.getTitleTemp().setText("请输入最新速度值");
                break;
            case 1:
                textdialog.getTitle().setText("MP3通道修改");
                textdialog.getTitleTemp().setText("请输入MP3通道");
                break;
            case 2:
                textdialog.getTitle().setText("超时时间修改");
                textdialog.getTitleTemp().setText("请输入超时时间");
                break;
            case 3:
                textdialog.getTitle().setText("显示编号修改");
                textdialog.getTitleTemp().setText("请输入显示编号");
                break;
            case 4:
                textdialog.getTitle().setText("显示颜色修改");
                textdialog.getTitleTemp().setText("请输入显示颜色");
                break;
        }
        editText = (EditText) textdialog.getEditText();
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        textdialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case 0:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set speed = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textdialog.dismiss();
                            speed.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set music = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textdialog.dismiss();
                            mp3.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set outime = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textdialog.dismiss();
                            outime.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set shownumber = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textdialog.dismiss();
                            shownum.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        if (!editText.getText().toString().trim().equals("")) {
                            robotDBHelper.execSQL("update command set showcolor = '" + editText.getText().toString().trim() + "' where id= '" + command_id + "'");
                            textdialog.dismiss();
                            showcolor.setText(editText.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "请输入参数", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
        textdialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textdialog.dismiss();
            }
        });
        textdialog.show();
    }
}
