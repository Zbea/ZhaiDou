package com.zhaidou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.utils.DeviceUtils;

import java.util.List;

/**
 * Created by Zbea on 16/7/5. 商品介绍信息适配器
 */
public class GoodsInfoAdapter extends BaseListAdapter<GoodInfo>
{
    Context mContext;

    public GoodsInfoAdapter(Context context, List<GoodInfo> list)
    {
        super(context, list);
        mContext=context;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_goods_info, null);
        TextView tv_key = ViewHolder.get(convertView, R.id.tv_key);
        tv_key.setMaxWidth(DeviceUtils.getScreenWidth(mContext) / 2 - 20);
        TextView tv_value = ViewHolder.get(convertView, R.id.tv_value);
        GoodInfo goodInfo = getList().get(position);
        tv_key.setText(goodInfo.getTitle());
        tv_value.setText(goodInfo.getValue());
        return convertView;
    }
}
