package com.zhaidou.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by roy on 15/7/14.
 */
public class AdViewAdpater extends PagerAdapter
{
    private List<View> views;
    private Context context;

    public AdViewAdpater(Context context, List<View> advPics)
    {
        this.context = context;
        this.views = advPics;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        View view = null;
        if (views.size() > 0)
        {
            if (position % views.size() < 0)
            {
                view = views.get(views.size() + position);
            } else
            {
                view = views.get(position % views.size());
            }
            ViewParent vp = view.getParent();
            if (vp != null)
            {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            ((ViewPager) container).addView(view);
        }
        return view;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1)
    {
        return arg0 == arg1;
    }

    @Override
    public int getCount()
    {
        return Integer.MAX_VALUE / 2;
    }

}
