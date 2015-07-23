package com.zhaidou.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.model.Article;
import com.zhaidou.model.TodayShopItem;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import java.util.List;

/**
 * Created by roy on 15/7/23.
 */
public class ShopTodaySpecialAdapter extends BaseAdapter
{

    private List<TodayShopItem> items;
    private ViewHolder viewHolder;
    private Context context;

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public ShopTodaySpecialAdapter(Context context, List<TodayShopItem> items)
    {
        this.context = context;
        this.items = items;
    }

    class ViewHolder
    {
        TypeFaceTextView itemIntorduce;
        TypeFaceTextView itemName;
        ImageView itemImage;
        TypeFaceTextView itemBuy;
        TypeFaceTextView itemCurrentPrice;
        TypeFaceTextView itemFormerPrice;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_today_special_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.shopNameItem);
            viewHolder.itemIntorduce = (TypeFaceTextView) convertView.findViewById(R.id.shopIntroduceItem);
            viewHolder.itemCurrentPrice = (TypeFaceTextView) convertView.findViewById(R.id.shopCurrentPrice);
            viewHolder.itemFormerPrice = (TypeFaceTextView) convertView.findViewById(R.id.shopFormerPrice);
            viewHolder.itemBuy = (TypeFaceTextView) convertView.findViewById(R.id.buyGoodsBtn);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.shopGoodsImage);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TodayShopItem todayShopItem=items.get(position);

        viewHolder.itemFormerPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        viewHolder.itemName.setText(todayShopItem.title);
        viewHolder.itemIntorduce.setText(todayShopItem.introduce);
        viewHolder.itemCurrentPrice.setText("￥ "+todayShopItem.currentPrice);
        viewHolder.itemFormerPrice.setText("￥ "+todayShopItem.formerPrice);
        ToolUtils.setImageCacheUrl(todayShopItem.imageUrl,viewHolder.itemImage);


        return convertView;
    }
}
