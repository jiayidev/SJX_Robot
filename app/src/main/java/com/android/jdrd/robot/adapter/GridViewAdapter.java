package com.android.jdrd.robot.adapter;

import android.content.Context;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jdrd.robot.R;

import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/9
 * 描述: 机器人状态适配器
 */

public class GridViewAdapter extends BaseAdapter {
    Context context;
    List<Map> list;

    public GridViewAdapter(Context _context, List<Map> _list) {
        this.list = _list;
        this.context = _context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageview);
            viewHolder.imageback = (ImageView) convertView.findViewById(R.id.imageback);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);//讲ViewHolder存储在View中

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Map map = list.get(position);

        if (("1").equals(map.get("outline").toString())) {
            viewHolder.imageView.setImageResource(R.mipmap.zaixian);
        } else {
            viewHolder.imageView.setImageResource(R.mipmap.lixiang02);
        }
        viewHolder.name.setText(map.get("name").toString());
        switch ((int) map.get("robotstate")) {
            case 0:
                viewHolder.text.setText("空闲");
                viewHolder.imageback.setImageResource(R.mipmap.kongxian);
                break;
            case 1:
                viewHolder.text.setText("送餐");
                viewHolder.imageback.setImageResource(R.mipmap.fuwuzhong);
                break;
            case 2:
                viewHolder.text.setText("故障");
                viewHolder.imageback.setImageResource(R.mipmap.guzhang);
                break;
        }
        return convertView;
    }

    //内部类
    class ViewHolder {
        ImageView imageView;
        TextView text;
        TextView name;
        ImageView imageback;
    }

}

