package com.android.jdrd.robot.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.jdrd.robot.R;
import com.android.jdrd.robot.activity.CommandActivity;
import com.android.jdrd.robot.util.Constant;

import java.util.List;
import java.util.Map;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/8
 * 描述: 运行状态适配器
 */

public class MyAdapter extends BaseAdapter {
    Context context;
    List<Map> list;

    public MyAdapter(Context _context, List<Map> _list) {
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
            convertView = inflater.inflate(R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            // 根据自定义的Item布局加载布局
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.btn = (Button) convertView.findViewById(R.id.btn);
            // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到编辑命令页面CommandActivity 并且传递id
                Intent intent = new Intent(context, CommandActivity.class);
                Constant.debugLog("commandId----->" + list.get(position).get("id").toString());
                intent.putExtra("id", (Integer) list.get(position).get("id"));
                context.startActivity(intent);
            }
        });

        switch ((int) list.get(position).get("type")) {
            case 0:
                viewHolder.text.setText(R.string.straight);
                break;
            case 1:
                viewHolder.text.setText(R.string.derail);
                break;
            case 2:
                viewHolder.text.setText(R.string.rotato);
                break;
            case 3:
                viewHolder.text.setText(R.string.wait);
                break;
            case 4:
                viewHolder.text.setText(R.string.puthook);
                break;
            case 5:
                viewHolder.text.setText(R.string.lockhook);
                break;
        }
        return convertView;
    }

    // ViewHolder静态类
    static class ViewHolder {
        TextView text;
        static Button btn;
    }

}

