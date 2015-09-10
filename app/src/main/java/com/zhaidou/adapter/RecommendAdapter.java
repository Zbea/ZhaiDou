package com.zhaidou.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.RecommendItem;
import com.zhaidou.utils.ToolUtils;

import java.util.List;


public class RecommendAdapter extends BaseListAdapter<RecommendItem> {


    public RecommendAdapter(Context context, List<RecommendItem> list) {
        super(context,list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView=mInflater.inflate(R.layout.setting_recommend_item,null);
        TextView tv_item = ViewHolder.get(convertView,R.id.recommendTitle);
        TextView tv_info= ViewHolder.get(convertView,R.id.recommendInfo);
        ImageView iv_image = ViewHolder.get(convertView,R.id.recommendImage);

        RecommendItem recommendItem=getList().get(position);
        tv_item.setText(recommendItem.title);
        tv_info.setText(recommendItem.info);

        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(20))//设置圆角半径
                .showImageOnLoading(R.drawable.icon_loading_defalut)
                .showImageForEmptyUri(R.drawable.icon_loading_defalut)
                .showImageOnFail(R.drawable.icon_loading_defalut)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(recommendItem.imageUrl, iv_image,options);

        return convertView;
    }
}
