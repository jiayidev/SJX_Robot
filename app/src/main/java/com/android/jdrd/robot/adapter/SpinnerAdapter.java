package com.android.jdrd.robot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.activity.CommandActivity;

import java.util.List;
import java.util.Map;


/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/8
 * 描述: 区域名称适配器
 */
public class SpinnerAdapter extends BaseAdapter {
    Context context;
    List<Map> list;
    boolean flag;

    public SpinnerAdapter(Context _context, List<Map> _list, boolean flag) {
        this.list = _list;
        this.context = _context;
        this.flag = flag;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_card, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.imageview = (ImageView) convertView.findViewById(R.id.imageview);
            convertView.setTag(viewHolder);//讲ViewHolder存储在View中
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageview.setVisibility(View.GONE);
        if (flag) {
            if (position == CommandActivity.goalNum) {
                viewHolder.imageview.setVisibility(View.VISIBLE);
            }
        } else {
            if (position == CommandActivity.directionNum) {
                viewHolder.imageview.setVisibility(View.VISIBLE);
            }
        }
        viewHolder.text.setText(list.get(position).get("name").toString());
        return convertView;
    }

    //内部类
    class ViewHolder {
        TextView text;
        ImageView imageview;
    }

}

