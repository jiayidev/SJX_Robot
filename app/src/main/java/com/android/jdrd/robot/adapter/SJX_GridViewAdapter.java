package com.android.jdrd.robot.adapter;

import android.content.Context;
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

public class SJX_GridViewAdapter extends BaseAdapter {
    Context context;
    List<Map> list;

    public SJX_GridViewAdapter(Context _context, List<Map> _list) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        // 如果缓存convertView为空，则需要创建View
        if (convertView == null) {
            // 根据context上下文加载布局，这里的是AreaAdapter本身，即this
            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.sjx_grid_item, null);
            viewHolder = new ViewHolder();
            // 根据自定义的Item布局加载布局
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageview);
            viewHolder.imageback = (ImageView) convertView.findViewById(R.id.imageback);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            // 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(viewHolder);

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
        // 机器人状态  0->空闲   1->送餐   2->故障
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

    // ViewHolder静态类
    class ViewHolder {
        ImageView imageView;
        TextView text;
        TextView name;
        ImageView imageback;
    }

}

