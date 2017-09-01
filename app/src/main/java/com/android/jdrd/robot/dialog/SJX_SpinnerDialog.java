package com.android.jdrd.robot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.jdrd.robot.R;


/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/8
 * 描述:自定义运动到站点对话框
 */
public class SJX_SpinnerDialog extends Dialog {
    private ListView listView;
    private Button positiveButton, negativeButton;
    private TextView title;

    public SJX_SpinnerDialog(Context context) {
        super(context, R.style.SoundRecorder);
        setCustomDialog();
    }

    /**
     * 初始化
     */
    private void setCustomDialog() {
        // 加载布局
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.sjx_fragment_spinner_dialog, null);
        title = (TextView) mView.findViewById(R.id.title);
        listView = (ListView) mView.findViewById(R.id.listview);
        positiveButton = (Button) mView.findViewById(R.id.positiveButton);
        negativeButton = (Button) mView.findViewById(R.id.negativeButton);
        super.setContentView(mView);
    }

    // ListView列表
    public ListView getListView() {
        return listView;
    }

    public View getTextView() {
        return title;
    }

    public View getNegative() {
        return negativeButton;
    }

    @Override
    public void setContentView(int layoutResID) {
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
    }

    @Override
    public void setContentView(View view) {
    }

    /**
     * 确定键监听器
     */
    public void setOnPositiveListener(View.OnClickListener listener) {
        positiveButton.setOnClickListener(listener);
    }

    /**
     * 取消键监听器
     */
    public void setOnNegativeListener(View.OnClickListener listener) {
        negativeButton.setOnClickListener(listener);
    }

}