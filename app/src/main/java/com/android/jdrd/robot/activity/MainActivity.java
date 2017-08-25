package com.android.jdrd.robot.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.adapter.AreaAdapter;
import com.android.jdrd.robot.adapter.DeskAdapter;
import com.android.jdrd.robot.adapter.GridViewAdapter;
import com.android.jdrd.robot.dialog.DeleteDialog;
import com.android.jdrd.robot.dialog.MyDialog;
import com.android.jdrd.robot.dialog.RobotDialog;
import com.android.jdrd.robot.helper.RobotDBHelper;
import com.android.jdrd.robot.service.ServerSocketUtil;
import com.android.jdrd.robot.service.SetStaticIPService;
import com.android.jdrd.robot.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/7/27
 * 描述: 主页
 */
public class MainActivity extends Activity implements View.OnClickListener, Animation.AnimationListener {
    private MyReceiver receiver;
    private RobotDBHelper robotDBHelper;
    private static List<Map> areaList = new ArrayList<>(), deskList = new ArrayList<>(), robotList = new ArrayList<>(), commandlit = new ArrayList<>();
    private LinearLayout linearlayout_all, linear_robot, linear_desk;
    private RelativeLayout map_right_Ralative;
    private ImageView imgViewmapnRight;
    private GridView deskview, robotgirdview;
    public static int Current_INDEX = 1;
    private TranslateAnimation translateAnimation;
    private List<Map<String, Object>> Areadata_list = new ArrayList<>();
    private List<Map<String, Object>> Deskdata_list = new ArrayList<>();
    private static List<Map> Robotdata_list = new ArrayList<>();
    private DeskAdapter desk_adapter;
    private AreaAdapter area_adapter;
    public static boolean DeskIsEdit = false, AreaIsEdit = false;
    private boolean IsRight = true, IsFinish = true;
    private ListView area;
    private float density;
    private TextView area_text;
    private Button up, down, left, right, stop, shrink;
    public static int CURRENT_AREA_id = 0;
    private final String[] from = {"image", "name", "text"};
    private final int[] to = {R.id.image, R.id.name, R.id.text};
    private GridViewAdapter gridViewAdapter;
    private boolean isShrink = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Intent SetStaticIPService = new Intent(this, SetStaticIPService.class);
        startService(SetStaticIPService);
        //启动后台通讯服务
        Intent serverSocket = new Intent(this, ServerSocketUtil.class);
        startService(serverSocket);
        //startService(new Intent(this, ClientSocketUtil.class));
        robotDBHelper = RobotDBHelper.getInstance(getApplicationContext());
        linearlayout_all = (LinearLayout) findViewById(R.id.linearlayout_all);
        imgViewmapnRight = (ImageView) findViewById(R.id.imgViewmapnRight);
        map_right_Ralative = (RelativeLayout) findViewById(R.id.map_right_Ralative);
        area = (ListView) findViewById(R.id.area);
        imgViewmapnRight.setOnClickListener(this);
        findViewById(R.id.config_redact).setOnClickListener(this);
        area_text = (TextView) findViewById(R.id.area_text);
        linear_robot = (LinearLayout) findViewById(R.id.linear_robot);
        linear_desk = (LinearLayout) findViewById(R.id.linear_desk);
        findViewById(R.id.main).setOnClickListener(this);
        linear_robot.setOnClickListener(this);
        linear_desk.setOnClickListener(this);
        findViewById(R.id.main).setOnClickListener(this);
        robotgirdview = (GridView) findViewById(R.id.robotgirdview);
        robotgirdview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!IsRight) {
                    startAnimationLeft();
                } else {
                    Intent intent = new Intent(MainActivity.this, RobotActivity.class);
                    intent.putExtra("id", (Integer) Robotdata_list.get(position).get("id"));
                    startActivity(intent);
                }
            }
        });
        findViewById(R.id.activity_main).setOnClickListener(this);
        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        stop = (Button) findViewById(R.id.stop);
        shrink = (Button) findViewById(R.id.shrink);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        stop.setOnClickListener(this);
        shrink.setOnClickListener(this);

        findViewById(R.id.shrink).setOnClickListener(this);
        deskview = (GridView) findViewById(R.id.gview);
        //获取数据
        desk_adapter = new DeskAdapter(this, Deskdata_list);
        deskview.setAdapter(desk_adapter);
        deskview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!IsRight) {
                    startAnimationLeft();
                } else {
                    getAreaData();
                    if (areaList != null && areaList.size() > 0 && CURRENT_AREA_id != 0) {
                        if (DeskIsEdit) {
                            if (position == 0) {
                                Intent intent = new Intent(MainActivity.this, DeskConfigPathAcitivty.class);
                                intent.putExtra("area", CURRENT_AREA_id);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(MainActivity.this, DeskConfigPathAcitivty.class);
                                intent.putExtra("area", CURRENT_AREA_id);
                                intent.putExtra("id", (Integer) Deskdata_list.get(position).get("id"));
                                startActivity(intent);
                            }
                            getDeskData();
                        } else {
                            Constant.debugLog("position" + CURRENT_AREA_id);
                            commandlit = robotDBHelper.queryListMap("select * from command where desk = '" + Deskdata_list.get(position).get("id") + "'", null);
                            if (commandlit != null && commandlit.size() > 0) {
                                robotDialog(commandlit);
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "请添加并选择区域", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        area_adapter = new AreaAdapter(this, Areadata_list);
        area.setAdapter(area_adapter);
        area.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getAreaData();
                if (AreaIsEdit) {
                    if (position == 0) {
                        AreaIsEdit = false;
                    } else if (position == 1) {
                        dialog();
                    } else {
                        dialog(Areadata_list.get(position).get("name").toString(), (int) Areadata_list.get(position).get("id"));
                    }
                    getAreaData();
                } else {
                    if (position == 0) {
                        AreaIsEdit = true;
                    } else {
                        if (!IsRight) {
                            startAnimationLeft();
                        }
                        DeskIsEdit = false;
                        CURRENT_AREA_id = (int) Areadata_list.get(position).get("id");
                        Current_INDEX = position;
                        area_text.setText(Areadata_list.get(position).get("name").toString());
                        getDeskData();
                    }
                    getAreaData();
                }
            }
        });
        robotDBHelper.execSQL("update robot set outline= '0' ");
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.jdrd.activity.Main");
        if (receiver != null) {
            this.registerReceiver(receiver, filter);
        }
    }

    private void setGridView() {
        int size = Robotdata_list.size();
        int length = 76;
        int height = 106;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        int gridviewWidth = (int) (size * (length + 30) * density);
        if (gridviewWidth <= 340 * density) {
            gridviewWidth = (int) (340 * density);
        }
        int itemWidth = (int) (length * density);
        int itemHeight = (int) (height * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, itemHeight);
        Constant.linearWidth = (int) (76 * density);
        robotgirdview.setLayoutParams(params); // 重点
        robotgirdview.setColumnWidth(itemWidth); // 重点
        robotgirdview.setHorizontalSpacing((int) (8 * density)); // 间距
        robotgirdview.setStretchMode(GridView.NO_STRETCH);
        robotgirdview.setNumColumns(size); // 重点
        gridViewAdapter = new GridViewAdapter(getApplicationContext(),
                Robotdata_list);
        robotgirdview.setAdapter(gridViewAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAreaData();
        if (CURRENT_AREA_id == 0) {
            if (areaList != null && areaList.size() > 0) {
                CURRENT_AREA_id = (int) areaList.get(0).get("id");
                Current_INDEX = 1;
                area_text.setText(areaList.get(0).get("name").toString());
            } else {
                area_text.setText("请选择左侧区域");
            }
        } else {
            for (int i = 0, size = areaList.size(); i < size; i++) {
                if (((int) areaList.get(i).get("id")) == CURRENT_AREA_id) {
                    area_text.setText(areaList.get(i).get("name").toString());
                    CURRENT_AREA_id = (int) areaList.get(i).get("id");
                    Current_INDEX = i + 1;
                }
            }
        }
        getDeskData();
        getRobotData();
        gridViewAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_robot:
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            case R.id.main:
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            case R.id.imgViewmapnRight:
                startAnimationLeft();
                break;
            case R.id.config_redact:
                if (DeskIsEdit) {
                    DeskIsEdit = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.config_redact).setBackground(getResources().getDrawable(R.animator.btn_direct_selector, null));
                    }
                } else {
                    DeskIsEdit = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.config_redact).setBackground(getResources().getDrawable(R.animator.btn_exit_selector, null));
                    }
                }
                getDeskData();
                break;
            case R.id.robotgirdview:
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            case R.id.linear_desk:
                if (!IsRight) {
                    startAnimationLeft();
                }
                break;
            case R.id.up:
                robotDialog("*u+6+#");
                break;
            case R.id.down:
                robotDialog("*d+6+#");
                break;
            case R.id.left:
                robotDialog("*l+6+#");
                break;
            case R.id.right:
                robotDialog("*r+6+#");
                break;
            case R.id.stop:
                robotDialog("*s+6+#");
                break;
            case R.id.shrink:
                if (!IsRight) {
                    startAnimationLeft();
                }
                startAnimationShrink();
                break;
        }
    }

    public List<Map<String, Object>> getDeskData() {
        Deskdata_list.clear();
        try {
            deskList = robotDBHelper.queryListMap("select * from desk where area = '" + CURRENT_AREA_id + "'", null);
            Log.e("Robot", deskList.toString());
            Log.e("Robot", "CURRENT_AREA_id" + CURRENT_AREA_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> map;
        if (DeskIsEdit) {
            map = new HashMap<>();
            map.put("image", R.animator.btn_add_desk_selector);
            map.put("id", 0);
//            map.put("name",getString(R.string.config_add));
            Deskdata_list.add(map);
        }
        if (deskList != null && deskList.size() > 0) {
            for (int i = 0, size = deskList.size(); i < size; i++) {
                map = new HashMap<>();
                if (DeskIsEdit) {
//                    map.put("image", R.mipmap.ic_launcher);
                } else {
//                    map.put("image", R.mipmap.bg);
                }
                map.put("id", deskList.get(i).get("id"));
                map.put("name", deskList.get(i).get("name"));
                map.put("area", deskList.get(i).get("area"));
                Deskdata_list.add(map);
            }
        }
        desk_adapter.notifyDataSetChanged();
        return Deskdata_list;
    }

    public List<Map> getRobotData() {
        Robotdata_list.clear();
        try {
            robotList = robotDBHelper.queryListMap("select * from robot", null);
            Constant.debugLog("robotList" + robotList.toString());
            List<Map> Robotdata_listcache = new ArrayList<>();
            int j;
            boolean flag;
            for (int i = 0, size = robotList.size(); i < size; i++) {
                Constant.debugLog("size" + size + " ip" + robotList.get(i).get("ip").toString());
                String ip = robotList.get(i).get("ip").toString();
                j = 0;
                int h = ServerSocketUtil.socketList.size();
                flag = false;
                while (j < h) {
                    if (ip.equals(ServerSocketUtil.socketList.get(j).get("ip"))) {
                        Constant.debugLog("对比");
                        robotDBHelper.execSQL("update robot set outline= '1' where ip = '" + robotList.get(i).get("ip") + "'");
                        robotList.get(i).put("outline", 1);
                        Robotdata_listcache.add(robotList.get(i));
                        robotList.remove(i);
                        flag = true;
                        break;
                    }
                    j++;
                    h = ServerSocketUtil.socketList.size();
                }
                size = robotList.size();
                if (flag) {
                    i--;
                }
            }
            Robotdata_list.addAll(Robotdata_listcache);
            Robotdata_list.addAll(robotList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setGridView();
        return Robotdata_list;
    }

    public List<Map<String, Object>> getAreaData() {
        Areadata_list.clear();
        try {
            areaList = robotDBHelper.queryListMap("select * from area", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> map;
        map = new HashMap<>();
        map.put("image", R.mipmap.qybianji_no);
//        map.put("name",getString(R.string.config_redact));
        Areadata_list.add(map);
        if (AreaIsEdit) {
            map = new HashMap<>();
            map.put("image", R.mipmap.add_area);
//            map.put("name",getString(R.string.config_add));
            Areadata_list.add(map);
        }
        if (areaList != null && areaList.size() > 0) {
            for (int i = 0, size = areaList.size(); i < size; i++) {
                map = new HashMap<>();
                if (AreaIsEdit) {
//                    map.put("image", R.mipmap.ic_launcher);
                } else {
//                    map.put("image", R.mipmap.bg);
                }
                map.put("id", areaList.get(i).get("id"));
                map.put("name", areaList.get(i).get("name"));
                Areadata_list.add(map);
            }
        }
        area_adapter.notifyDataSetChanged();
        return Areadata_list;
    }

    private void startAnimationLeft() {
        if (IsFinish) {
            IsFinish = false;
            if (IsRight) {
                linearlayout_all.setVisibility(View.VISIBLE);
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, -Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0F
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                translateAnimation.setAnimationListener(MainActivity.this);
                map_right_Ralative.startAnimation(translateAnimation);

                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0F
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_robot.startAnimation(translateAnimation);
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0F
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_desk.startAnimation(translateAnimation);
            } else {
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, -Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                translateAnimation.setAnimationListener(MainActivity.this);
                map_right_Ralative.startAnimation(translateAnimation);

                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_robot.startAnimation(translateAnimation);
                translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, Constant.linearWidth,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f,
                        Animation.ABSOLUTE, 0.0f
                );
                translateAnimation.setDuration(500);
                translateAnimation.setFillAfter(true);
                linear_desk.startAnimation(translateAnimation);
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        map_right_Ralative.clearAnimation();
        if (IsRight) {
            IsRight = false;
        } else {
            IsRight = true;
            linearlayout_all.setVisibility(View.GONE);
        }
        IsFinish = true;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @SuppressWarnings("deprecation")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startAnimationShrink() {
        Animation translate;
        if (isShrink) {
            findViewById(R.id.shrink).setBackground(getResources().getDrawable(R.animator.btn_shrink_selector));
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_left);
            translate.setAnimationListener(animationListener);
            left.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_right);
            translate.setAnimationListener(animationListener);
            right.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_up);
            translate.setAnimationListener(animationListener);
            up.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_down);
            translate.setAnimationListener(animationListener);
            down.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_in_stop);
            translate.setAnimationListener(animationListener);
            stop.startAnimation(translate);
        } else {
            findViewById(R.id.shrink).setBackground(getResources().getDrawable(R.animator.btn_shrink_out_selector));
            left.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
            up.setVisibility(View.VISIBLE);
            right.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_left);
            translate.setAnimationListener(animationListener);
            left.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_right);
            translate.setAnimationListener(animationListener);
            right.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_up);
            translate.setAnimationListener(animationListener);
            up.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_down);
            translate.setAnimationListener(animationListener);
            down.startAnimation(translate);
            translate = AnimationUtils.loadAnimation(this, R.animator.translate_out_stop);
            translate.setAnimationListener(animationListener);
            stop.startAnimation(translate);
        }
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isShrink) {
                isShrink = false;
                up.setVisibility(View.GONE);
                down.setVisibility(View.GONE);
                left.setVisibility(View.GONE);
                right.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
            } else {
                isShrink = true;
                left.setVisibility(View.VISIBLE);
                down.setVisibility(View.VISIBLE);
                up.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    private MyDialog dialog;
    private EditText editText;
    private TextView title;

    private void dialog() {
        dialog = new MyDialog(this);
        editText = (EditText) dialog.getEditText();
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "区域名称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    robotDBHelper.insert("area", new String[]{"name"}, new Object[]{editText.getText().toString()});
                    getAreaData();
                    dialog.dismiss();
                }
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

    private void dialog(String name, final int id) {
        dialog = new MyDialog(this);
        editText = (EditText) dialog.getEditText();
        editText.setText(name);
        title = (TextView) dialog.getTitle();
        dialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "区域名称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    robotDBHelper.execSQL("update area set name= '" + editText.getText().toString().trim() + "' where id= '" + id + "'");
                    dialog.dismiss();
                }
            }
        });

        dialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog(id);
            }
        });
        ((Button) dialog.getNegative()).setText(R.string.btn_delete);
        dialog.show();
    }

    private RobotDialog robotDialog;

    private void robotDialog(String str) {
        robotDialog = new RobotDialog(this, str);
        robotDialog.show();
    }

    private void robotDialog(List<Map> list) {
        robotDialog = new RobotDialog(this, list);
        robotDialog.show();
    }

    private DeleteDialog deleteDialog;

    private void deleteDialog(final int id) {
        deleteDialog = new DeleteDialog(this);
        deleteDialog.getTemplate().setText("确定删除区域吗？");
        deleteDialog.setOnPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotDBHelper.execSQL("delete from area where id= '" + id + "'");
                List<Map> desklist;
                desklist = robotDBHelper.queryListMap("select * from desk where area = '" + id + "'", null);
                for (int i = 0, size = desklist.size(); i < size; i++) {
                    robotDBHelper.execSQL("delete from command where desk= '" + desklist.get(i).get("id") + "'");
                }
                robotDBHelper.execSQL("delete from desk where area= '" + id + "'");
                getAreaData();
                if (areaList != null && areaList.size() > 0) {
                    CURRENT_AREA_id = (int) areaList.get(0).get("id");
                    Current_INDEX = 1;
                    area_text.setText(areaList.get(0).get("name").toString());
                } else {
                    area_text.setText("请选择左侧区域");
                }
                getDeskData();
                deleteDialog.dismiss();
                dialog.dismiss();
            }
        });
        deleteDialog.setOnNegativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
        deleteDialog.show();
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String StringE = intent.getStringExtra("msg");
            Constant.debugLog("msg" + StringE);
            if (StringE != null && !StringE.equals("")) {
                pasreJson(StringE);
            }
        }
    }

    public void pasreJson(String string) {
        if (string.equals("robot_connect") || string.equals("robot_unconnect")) {
            getRobotData();
            gridViewAdapter.notifyDataSetInvalidated();
        } else if (string.equals("robot_receive_succus")) {
            synchronized (RobotDialog.thread) {
                if (RobotDialog.CurrentIndex == -1) {
                    RobotDialog.CurrentIndex = 0;
                }
                RobotDialog.thread.notify();
            }
        } else if (string.equals("robot_receive_fail")) {
            if (RobotDialog.flag) {
                RobotDialog.sendCommandList();
            } else {
                RobotDialog.sendCommand();
            }
        } else if (string.equals("robot_destory")) {
            robotDBHelper.execSQL("update robot set outline= '0' ");
        } else {
        }
    }
}
