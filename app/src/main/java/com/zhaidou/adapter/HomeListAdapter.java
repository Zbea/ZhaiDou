package com.zhaidou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.model.Article;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import java.util.List;

/**
 * Created by roy on 15/7/16.
 */
public class HomeListAdapter extends BaseAdapter
{

    private List<Article> items;
    private ViewHolder viewHolder;
    private Context context;
    private int screenWidth;

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public HomeListAdapter(Context context, List<Article> items,int screenWidth)
    {
        this.context = context;
        this.items = items;
        this.screenWidth=screenWidth;
    }

    class ViewHolder
    {
        TextView title;
        TextView articleViews;
        ImageView cover;
        ImageView newView;
        View lineTo;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_strategy_list, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.articleViews = (TextView) convertView.findViewById(R.id.views);
            viewHolder.cover = (ImageView) convertView.findViewById(R.id.cover);
            viewHolder.cover.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth,screenWidth*316/722));
            viewHolder.newView = (ImageView) convertView.findViewById(R.id.newsView);
//            viewHolder.lineTo = (View) convertView.findViewById(R.id.lineTo);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Article article = items.get(position);
        viewHolder.title.setText(article.getTitle());
        viewHolder.articleViews.setText(article.getReviews() + "");
        ToolUtils.setImageCacheUrl(article.getImg_url(), viewHolder.cover,R.drawable.icon_loading_item);
        if (article.getIs_new().equals("true"))
        {
            if (!(Boolean) SharedPreferencesUtil.getData(context, "is_new_" + article.getId(), true))
            {
                viewHolder.newView.setVisibility(View.GONE);
            } else
            {
                viewHolder. newView.setVisibility(View.VISIBLE);
            }
        } else
        {
            viewHolder.newView.setVisibility(View.GONE);
        }
        return convertView;
    }
}
