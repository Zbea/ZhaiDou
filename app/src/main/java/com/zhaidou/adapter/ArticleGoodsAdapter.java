package com.zhaidou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.utils.ToolUtils;

import java.util.List;

/**
 * 评论文章中的商品适配Created by Zbea on 16/7/5.
 */
public class ArticleGoodsAdapter extends BaseListAdapter<CartGoodsItem>
{
    Context context;

    public ArticleGoodsAdapter(Context context, List<CartGoodsItem> list)
    {
        super(context, list);
        this.context = context;
    }

    @Override
    public View bindView(final int position, View convertView, ViewGroup parent)
    {
//            convertView = mHashMap.get(position);
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_article_goods, null);

        TextView goodsNameTv = ViewHolder.get(convertView, R.id.goodsNameTv);
        TextView goodsSizeTv = ViewHolder.get(convertView, R.id.goodsSizeTv);
        ImageView goodsImageTv = ViewHolder.get(convertView, R.id.goodsImageTv);
        TextView goodsPriceTv = ViewHolder.get(convertView, R.id.goodsPriceTv);
        TextView goodsNumTv = ViewHolder.get(convertView, R.id.goodsNumTv);
        TextView goodsTypeTv = ViewHolder.get(convertView, R.id.goodsTypeTv);
        TextView goodsBuyTv = ViewHolder.get(convertView, R.id.goodsBuyTv);

        final CartGoodsItem goodsItem = getList().get(position);
        goodsNameTv.setText(goodsItem.name);
        goodsSizeTv.setText(goodsItem.size);
        goodsNumTv.setText("X" + goodsItem.num);
        goodsPriceTv.setText("￥" + ToolUtils.isIntPrice(goodsItem.currentPrice + ""));
        ToolUtils.setImageCacheUrl(goodsItem.imageUrl, goodsImageTv, R.drawable.icon_loading_defalut);

        if (goodsItem.storeId.equals("T"))
        {
            goodsTypeTv.setText("淘宝");
            goodsTypeTv.setTextColor(context.getResources().getColor(R.color.yellow));
        }
        else if (goodsItem.storeId.equals("A"))
        {
            goodsTypeTv.setText("阿里巴巴");
            goodsTypeTv.setTextColor(context.getResources().getColor(R.color.yellow));
        }else if (goodsItem.storeId.equals("M"))
        {
            goodsTypeTv.setText("天猫");
            goodsTypeTv.setTextColor(context.getResources().getColor(R.color.red));
        } else if (goodsItem.storeId.equals("J"))
        {
            goodsTypeTv.setText("京东");
            goodsTypeTv.setTextColor(context.getResources().getColor(R.color.red));
        } else if (goodsItem.storeId.equals("S"))
        {
            goodsTypeTv.setText("宅豆");
            goodsTypeTv.setTextColor(context.getResources().getColor(R.color.green_color));
        }
        else
        {
            goodsTypeTv.setText("其他");
            goodsTypeTv.setTextColor(context.getResources().getColor(R.color.red));
        }
//            mHashMap.put(position, convertView);
        return convertView;
    }
}
