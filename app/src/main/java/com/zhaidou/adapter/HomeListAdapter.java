package com.zhaidou.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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
        View view = LayoutInflater.from(context).inflate(R.layout.home_item_list, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView articleViews = (TextView) view.findViewById(R.id.views);
        ImageView cover = (ImageView) view.findViewById(R.id.cover);
        cover.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth,screenWidth*316/722));
        ImageView newView = (ImageView) view.findViewById(R.id.newsView);

        Article article = items.get(position);

        title.setText(article.getTitle());
        articleViews.setText(article.getReviews() + "");
        ToolUtils.setImageCacheUrl(article.getImg_url(), cover,R.drawable.icon_loading_item);

        SharedPreferences editor = context.getSharedPreferences(String.valueOf(article.getId()), 0);
        if (article.getIs_new().equals("true"))
        {
            if (!(Boolean) SharedPreferencesUtil.getData(context, "is_new_" + article.getId(), true))
            {
                newView.setVisibility(View.GONE);
            } else
            {
                newView.setVisibility(View.VISIBLE);
            }
        } else
        {
            newView.setVisibility(View.GONE);
        }


//
//        if (convertView == null)
//        {
//            convertView = LayoutInflater.from(context).inflate(R.layout.home_item_list, null);
//            viewHolder = new ViewHolder();
//            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
//            viewHolder.articleViews = (TextView) convertView.findViewById(R.id.views);
//            viewHolder.cover = (ImageView) convertView.findViewById(R.id.cover);
//            convertView.setTag(viewHolder);
//        }
//        else
//        {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
//
//
//
//        Article article = items.get(position);
//
//        viewHolder.title.setText(article.getTitle());
//        viewHolder.articleViews.setText(article.getReviews() + "");
//        viewHolder.newView = (ImageView) convertView.findViewById(R.id.newsView);
//        ToolUtils.setImageUrl(article.getImg_url(), viewHolder.cover);
//
//        SharedPreferences editor=context.getSharedPreferences(String.valueOf(article.getId()),0);
//        if (article.getIs_new().equals("true"))
//        {
//            if (editor.getBoolean("is_new",false))
//            {
//                viewHolder.newView.setVisibility(View.GONE);
//            }
//            else
//            {
//                viewHolder.newView.setVisibility(View.VISIBLE);
//            }
//        }
//        else
//        {
//            viewHolder.newView.setVisibility(View.GONE);
//        }
        return view;
    }
}
