package com.zhaidou.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.zhaidou.R;
import com.zhaidou.model.ShopTodayItem;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by roy on 15/7/23.
 */
public class ShopTodaySpecialAdapter extends BaseAdapter
{

    private List<ShopTodayItem> items;
    private ViewHolder viewHolder;
    private Context context;

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public ShopTodaySpecialAdapter(Context context, List<ShopTodayItem> items)
    {
        this.context = context;
        this.items = items;
    }

    class ViewHolder
    {
        TypeFaceTextView itemIntorduce;
        TypeFaceTextView itemName;
        ImageView itemImage,itemNull;
        TypeFaceTextView itemBuy;
        TypeFaceTextView itemCurrentPrice;
        TypeFaceTextView itemFormerPrice;
        TypeFaceTextView itemSales;
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
            viewHolder.itemSales= (TypeFaceTextView) convertView.findViewById(R.id.shopSaleTv);
            viewHolder.itemIntorduce = (TypeFaceTextView) convertView.findViewById(R.id.shopIntroduceItem);
            viewHolder.itemCurrentPrice = (TypeFaceTextView) convertView.findViewById(R.id.shopCurrentPrice);
            viewHolder.itemFormerPrice = (TypeFaceTextView) convertView.findViewById(R.id.shopFormerPrice);
            viewHolder.itemBuy = (TypeFaceTextView) convertView.findViewById(R.id.buyGoodsBtn);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.shopGoodsImage);
            viewHolder.itemNull= (ImageView) convertView.findViewById(R.id.shopGoodsImageNo);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ShopTodayItem todayShopItem=items.get(position);
        viewHolder.itemFormerPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        viewHolder.itemName.setText(todayShopItem.title);
        viewHolder.itemIntorduce.setText("                           "+todayShopItem.designer);
        viewHolder.itemCurrentPrice.setText("￥ "+todayShopItem.currentPrice);
        viewHolder.itemFormerPrice.getPaint().setAntiAlias(true);//去锯齿
        viewHolder.itemFormerPrice.setText("￥ "+todayShopItem.formerPrice);
        if(todayShopItem.formerPrice!=0)
        {
            DecimalFormat df = new DecimalFormat("##.0");
            String zk=df.format(todayShopItem.currentPrice/todayShopItem.formerPrice*10);
            if (zk.contains(".0"))
            {
                int sales=(int)Double.parseDouble(zk);
                viewHolder.itemSales.setText(sales+"折");
            }
            else
            {
                Double sales=Double.parseDouble(zk);
                viewHolder.itemSales.setText(sales+"折");
            }
        }
        else
        {
            viewHolder.itemSales.setVisibility(View.GONE);
        }

        ToolUtils.setImageCacheUrl(todayShopItem.imageUrl,viewHolder.itemImage);
        if (todayShopItem.num>0)
        {
            viewHolder.itemNull.setVisibility(View.GONE);
            viewHolder.itemBuy.setBackgroundResource(R.drawable.btn_red_click_selector);
            viewHolder.itemBuy.setText("马上抢");
            viewHolder.itemBuy.setFocusable(true);
        }
        else
        {
            viewHolder.itemNull.setVisibility(View.VISIBLE);
            viewHolder.itemBuy.setBackgroundResource(R.drawable.btn_no_click_selector);
            viewHolder.itemBuy.setText("抢光了");
            viewHolder.itemBuy.setFocusable(false);
        }
        return convertView;
    }
}
