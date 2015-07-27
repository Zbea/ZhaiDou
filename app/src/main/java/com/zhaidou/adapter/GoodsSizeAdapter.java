package com.zhaidou.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.model.Article;
import com.zhaidou.model.GoodsSizeItem;
import com.zhaidou.utils.ToolUtils;

import java.util.List;

/**
 * Created by roy on 15/7/24.
 * 商品规格选择列表适配器
 */
public class GoodsSizeAdapter extends BaseAdapter
{

    private List<GoodsSizeItem> items;
    private ViewHolder viewHolder;
    private Context context;

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public GoodsSizeAdapter(Context context, List<GoodsSizeItem> items)
    {
        this.context = context;
        this.items = items;
    }

    class ViewHolder
    {
        TextView title;
    }

    @Override
    public int getCount()
    {
        return items.size();
    }

    @Override
    public Object getItem(int arg0)
    {
        return items.get(arg0);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.goods_details_size_item, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.sizeTitleTv);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        GoodsSizeItem article = items.get(position);

        viewHolder.title.setText(article.title);
        return convertView;
    }
}
