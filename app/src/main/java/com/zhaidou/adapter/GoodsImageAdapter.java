package com.zhaidou.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zhaidou.R;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.ToolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roy on 15/7/24.商品顶部图片适配器
 */
public class GoodsImageAdapter extends PagerAdapter
{
    List<ImageView> imageViews = new ArrayList<ImageView>();

    public void initImages(List<String> images,Context mContext)
    {
        for (int i = 0; i < images.size(); i++)
        {
            ImageView imageView = new ImageView(mContext);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(DeviceUtils.getScreenWidth(mContext), DeviceUtils.getScreenWidth(mContext));
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            ToolUtils.setImageCacheUrl(images.get(i), imageView, R.drawable.icon_loading_goods_details);
            imageViews.add(imageView);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return imageViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o)
    {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView(imageViews.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        container.addView(imageViews.get(position));
        return imageViews.get(position);
    }
}
