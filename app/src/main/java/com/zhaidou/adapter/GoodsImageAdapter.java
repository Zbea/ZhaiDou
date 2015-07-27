package com.zhaidou.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by roy on 15/7/24.
 */
public class GoodsImageAdapter extends PagerAdapter
{
    List<View> items;
    Context mContext;


    public GoodsImageAdapter(Context mContext,List<View> items)
    {
        this.mContext=mContext;
        this.items=items;
    }

    @Override
    public int getCount()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        ((ViewPager)container).addView(items.get(position%items.size()),0);
        return items.get(position%items.size());
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        ((ViewPager)container).removeView(items.get(position%items.size()));
    }

    @Override
    public boolean isViewFromObject(View view, Object o)
    {
        return view==o;
    }
}
