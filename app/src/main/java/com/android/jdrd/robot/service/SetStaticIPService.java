package com.android.jdrd.robot.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.jdrd.robot.util.Constant;
import com.android.jdrd.robot.util.WifiAdmin;
import com.android.jdrd.robot.util.WifiStaticIpUtil;

import java.util.ArrayList;
import java.util.List;
/**
 * 静态IP服务
 */
public class SetStaticIPService extends Service {

    private IntentFilter mWifiFilter;
    private WifiAdmin wifiadmin;
    private boolean isscaning = true;
    private List<ScanResult> list;
    private List<ScanResult> scanlist;
    String SSID = "";
    private ScanResult scanResult;
    private List<WifiConfiguration> wifiConfigurationList;
    private String CurrentSSID = null;
    private String lastSSID = null;
    private ConnectivityManager mConnectivityManager;
    private NetworkInfo networkInfo;
    int num = 0;
    private String TAG = "测试静态IP";


    @Override
    public void onCreate() {
        super.onCreate();

        registerWIFI();
        wifiadmin = new WifiAdmin(this);
        mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    private void registerWIFI() {
        mWifiFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectReceiver, mWifiFilter);
    }

    private BroadcastReceiver mWifiConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()
                    .equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int message = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        -1);
                switch (message) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        wifiadmin.openWifi();
                        isscaning = true;
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
//                        start_Scan();
                        break;
                    default:
                        break;
                }
            }
            if (intent.getAction().equals(
                    ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo.State state = mConnectivityManager.getNetworkInfo(
                        ConnectivityManager.TYPE_WIFI).getState();
                networkInfo = mConnectivityManager.getActiveNetworkInfo();

                if (state.toString().equals("CONNECTED")) {
                    if (networkInfo != null && SSID != null) {
                        String extrainfo = networkInfo.getExtraInfo().toString();
                        if (("\"" + Constant.wifiname + "\"").equals(extrainfo)) {
                            Toast.makeText(SetStaticIPService.this, "指定网络连接成功",
                                    Toast.LENGTH_LONG).show();
                            new WifiStaticIpUtil(SetStaticIPService.this).getNetworkInformation();
                            isscaning = false;
                            /*if (wifi_disconect_dialog != null) {
                                wifi_disconect_dialog.cancleDialog();
                                wifi_disconect_dialog = null;
                            }
                            handler.sendEmptyMessage(CONNECT_SUCCES);
                            handler.sendEmptyMessage(CONNECT_ROS_SUCCES);*/
                        } else {
                            Toast.makeText(SetStaticIPService.this, "非指定网络连接", Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (state.toString().equals("DISCONNECTED")) {
                    isscaning = true;
                    networkInfo = null;
                    num = num + 1;
                    if (num <= 1) {
                        //handler.sendEmptyMessage(Constant.REEOR);
                    } else {
                        num = 0;
                    }
                } else {
                }
            }
        }
    };

    public void start_Scan() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (isscaning) {
                    // 开始扫描网络
                    wifiadmin.startScan();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    list = wifiadmin.getWifiList();
                    getshowlist();
                }
            }
        }.start();
    }

    public void getshowlist() {
        scanlist = new ArrayList<ScanResult>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).SSID.length() > 4) {
                SSID = list.get(i).SSID;
                if (SSID.equals(Constant.wifiname)) {
                    scanResult = list.get(i);
                    scanlist.add(list.get(i));
                    if (scanlist.size() > 0) {
                        conncect();
                    }
                    break;
                }
            }
        }
    }

    private void conncect() {
        wifiConfigurationList = wifiadmin.getConfiguration();
        int isConfiguration = IsConfiguration(scanResult.SSID);
        CurrentSSID = "\"" + scanResult.SSID + "\"";
        if (!CurrentSSID.equals(lastSSID)) {
            deletepasswork(lastSSID);
            lastSSID = CurrentSSID;
        }
        if (-1 != isConfiguration) {
            Log.e(TAG, "已有密码");
            if (ConnectWifi(isConfiguration)) {
                //打开缓冲动画
            }
        } else {
            Log.e(TAG, "设置密码");
            int add_config = AddWifiConfig(list, scanResult.SSID, Constant.password);
            Log.e(TAG, "add_config增加密码结果" + add_config);
            if (-1 != add_config) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (-1 == IsConfiguration(scanResult.SSID)) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e(TAG, "增加密码成功后id" + IsConfiguration(scanResult.SSID));
                        boolean wificonnectflag = ConnectWifi(IsConfiguration(scanResult.SSID));
                        Log.e("wificonnectflag=====", "" + wificonnectflag);
                        //启动缓冲动画
                    }
                }.start();
            } else {
                Log.e(TAG, "设置失败");
            }
        }
    }

    public int IsConfiguration(String SSID) {
        wifiConfigurationList = wifiadmin.getConfiguration();
        if (wifiConfigurationList != null && wifiConfigurationList.size() > 0) {
            for (int i = 0; i < wifiConfigurationList.size(); i++) {
                if (wifiConfigurationList.get(i).SSID
                        .equals("\"" + SSID + "\"")) {// 地址相同
                    return wifiConfigurationList.get(i).networkId;
                }
            }
        }
        return -1;
    }

    public boolean deletepasswork(String SSID) {
        String deleteSSID = SSID;
        if (deleteSSID != null) {
            int IsInWIfiConfig = NetinfoConfiguration(deleteSSID);
            if (-1 != IsInWIfiConfig) {
                return deleteWifiConfig(IsInWIfiConfig);
            }
        }
        return false;
    }

    public int NetinfoConfiguration(String SSID) {
        wifiConfigurationList = wifiadmin.getConfiguration();
        if (wifiConfigurationList != null && wifiConfigurationList.size() > 0) {
            for (int i = 0; i < wifiConfigurationList.size(); i++) {
                if (wifiConfigurationList.get(i).SSID.equals(SSID)) {//地址相同
                    return wifiConfigurationList.get(i).networkId;
                }
            }
        }
        return -1;
    }

    public boolean deleteWifiConfig(int networkId) {
        boolean flag = wifiadmin.getWifiManager().removeNetwork(networkId);
        return flag;
    }

    public boolean ConnectWifi(int wifiId) {
        wifiConfigurationList = wifiadmin.getConfiguration();
        for (int i = 0; i < wifiConfigurationList.size(); i++) {
            WifiConfiguration wifi = wifiConfigurationList.get(i);
            if (wifi.networkId == wifiId) {
                Log.e(TAG, "wifi.status" + wifi.status);
                if (wifi.status == 0) {
                    Log.e(TAG, "正在连接");
                    wifiadmin.getWifiManager().enableNetwork(wifiId, true);
                    return true;
                } else if (wifi.status == 1) {
                    Log.e(TAG, "无法连接");
                    wifiadmin.getWifiManager().enableNetwork(wifiId, true);
                    isscaning = true;
                    return true;
                } else if (wifi.status == 2) {
                    Log.e(TAG, "已经连接");
                    wifiadmin.getWifiManager().enableNetwork(wifiId, true);
                    isscaning = false;
                    return false;
                } else {
                    wifiadmin.getWifiManager().enableNetwork(wifiId, true);
                }
            }
        }
        return false;
    }

    public int AddWifiConfig(List<ScanResult> wifiList, String ssid, String pwd) {
        int wifiId = -1;
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult wifi = wifiList.get(i);
            if (wifi.SSID.equals(ssid)) {
                WifiConfiguration wifiCong = new WifiConfiguration();
                wifiCong.SSID = "\"" + wifi.SSID + "\"";// \"转义字符，代表"
                wifiCong.preSharedKey = "\"" + pwd + "\"";// WPA-PSK密码
                wifiCong.hiddenSSID = false;
                wifiCong.status = WifiConfiguration.Status.ENABLED;
                wifiId = wifiadmin.getWifiManager().addNetwork(wifiCong);
                if (wifiId != -1) {
                    return wifiId;
                }
                wifiConfigurationList = wifiadmin.getConfiguration();
            }
        }
        return wifiId;
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
