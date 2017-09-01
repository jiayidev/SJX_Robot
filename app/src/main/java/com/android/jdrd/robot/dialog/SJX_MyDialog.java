package com.android.jdrd.robot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.jdrd.robot.R;


/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/8
 * 描述: 自定义区域对话框
 */
public class SJX_MyDialog extends Dialog {
    private EditText editText;
    private Button positiveButton, negativeButton;
    private TextView title,title_template;

    public SJX_MyDialog(Context context) {
        super(context, R.style.SoundRecorder);
        setCustomDialog();
    }

    /**
     * 初始化
     */
    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.sjx_fragment_dialog,null);
        title = (TextView) mView.findViewById(R.id.title);
        editText = (EditText) mView.findViewById(R.id.editText);
        positiveButton = (Button) mView.findViewById(R.id.positiveButton);
        negativeButton = (Button) mView.findViewById(R.id.negativeButton);
        title_template = (TextView) mView.findViewById(R.id.title_template);
        super.setContentView(mView);
    }

    //输入框
    public View getEditText(){
        return editText;
    }
    // 标题
    public TextView getTitle(){
        return title;
    }
    // 内容
    public TextView getTitleTemp(){
        return title_template;
    }
    // 确定
    public View getNegative(){
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
     *
     */
    public void setOnPositiveListener(View.OnClickListener listener){
        positiveButton.setOnClickListener(listener);
    }
    /**
     * 取消键监听器
     *
     */
    public void setOnNegativeListener(View.OnClickListener listener){
        negativeButton.setOnClickListener(listener);
    }
}