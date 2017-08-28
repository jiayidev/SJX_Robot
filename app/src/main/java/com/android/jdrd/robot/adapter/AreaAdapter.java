package com.android.jdrd.robot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.activity.MainActivity;

import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/9
 * 描述: 桌面适配器
 */

public class AreaAdapter extends BaseAdapter {
    Context context;
    List<Map<String, Object>> list;

    public AreaAdapter(Context _context, List<Map<String, Object>> _list) {
        this.list = _list;
        this.context = _context;
    }

    /**
     * 在此适配器中所代表的数据集中的条目数
     *
     * @return size
     */
    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * 获取数据集中与指定索引对应的数据项
     *
     * @param position 获取下标
     * @return
     */
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    /**
     * 获取在列表中与指定索引对应的行id
     *
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取一个在数据集中指定索引的视图来显示数据
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        // 如果缓存convertView为空，则需要创建View
        if (convertView == null) {
            // 根据context上下文加载布局，这里的是AreaAdapter本身，即this
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item, null);
            viewHolder = new ViewHolder();
            // 根据自定义的Item布局加载布局
            viewHolder.back = (RelativeLayout) convertView.findViewById(R.id.back);
            viewHolder.text = (TextView) convertView.findViewById(R.id.name);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.image2 = (ImageView) convertView.findViewById(R.id.image2);
            viewHolder.bjzt = (ImageView) convertView.findViewById(R.id.bjzt);
            // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (MainActivity.AreaIsEdit) {
            if (position == MainActivity.Current_INDEX + 1) {
                viewHolder.back.setBackgroundColor(Color.parseColor("#00923F"));
            } else {
                viewHolder.back.setBackgroundColor(Color.parseColor("#087235"));
            }
        } else {
            if (position == MainActivity.Current_INDEX) {
                viewHolder.back.setBackgroundColor(Color.parseColor("#00923F"));
            } else {
                viewHolder.back.setBackgroundColor(Color.parseColor("#087235"));
            }
        }
        viewHolder.image.setVisibility(View.GONE);
        viewHolder.image2.setVisibility(View.GONE);
        viewHolder.bjzt.setVisibility(View.GONE);
        if (list.get(position).get("image") != null) {
            viewHolder.text.setVisibility(View.GONE);
            viewHolder.bjzt.setVisibility(View.GONE);
            if (position == 0) {
                viewHolder.image2.setVisibility(View.VISIBLE);
                if (MainActivity.AreaIsEdit) {
                    viewHolder.image2.setImageResource(R.animator.btn_add_area_selector);
                } else {
                    viewHolder.image2.setImageResource(R.animator.btn_exit_area_selector);
                }
            } else {
                if (MainActivity.AreaIsEdit) {
                    viewHolder.bjzt.setVisibility(View.VISIBLE);
                    viewHolder.image.setVisibility(View.VISIBLE);
                    viewHolder.image.setImageResource(R.animator.btn_add_selector);
                }
            }
        } else {
            if (MainActivity.AreaIsEdit) {
                viewHolder.bjzt.setVisibility(View.VISIBLE);
            }
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.image.setVisibility(View.GONE);
            viewHolder.text.setText(list.get(position).get("name").toString());
        }
        return convertView;
    }

    // ViewHolder静态类
    static class ViewHolder {
        TextView text;
        ImageView image;
        ImageView image2;
        ImageView bjzt;
        RelativeLayout back;
    }

}

