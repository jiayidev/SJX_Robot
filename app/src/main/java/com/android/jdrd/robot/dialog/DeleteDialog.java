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
 * 描述:自定义删除指令对话框
 */
public class DeleteDialog extends Dialog {
    private EditText editText;
    private Button positiveButton, negativeButton;
    private TextView title;
    private TextView title_template;

    public DeleteDialog(Context context) {
        super(context, R.style.SoundRecorder);
        setCustomDialog();
    }

    /**
     * 初始化
     */
    private void setCustomDialog() {
        // 加载布局
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_delete_dialog,null);
        title = (TextView) mView.findViewById(R.id.title);
        title_template = (TextView) mView.findViewById(R.id.title_template);
        editText = (EditText) mView.findViewById(R.id.editText);
        positiveButton = (Button) mView.findViewById(R.id.positiveButton);
        negativeButton = (Button) mView.findViewById(R.id.negativeButton);
        super.setContentView(mView);
    }

    public View getEditText(){
        return editText;
    }
    public TextView getTextView(){
        return title;
    }
    // 内容
    public TextView getTemplate(){
        return title_template;
    }
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