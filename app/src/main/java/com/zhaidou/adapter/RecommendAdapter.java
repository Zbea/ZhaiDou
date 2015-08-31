package com.zhaidou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        ToolUtils.setImageCacheUrl(recommendItem.imageUrl,iv_image);

        return convertView;
    }
}
