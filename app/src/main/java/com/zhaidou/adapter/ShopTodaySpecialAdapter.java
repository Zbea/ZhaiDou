package com.zhaidou.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.fragments.GoodsDetailsFragment;
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
        TypeFaceTextView itemName;
        TypeFaceTextView itemIntorduce;
        ImageView itemImage,itemNull;
        TypeFaceTextView itemBuy;
        TextView itemCurrentPrice;
        TextView itemFormerPrice;
        TypeFaceTextView itemSales;
        TextView buyCount;
        ProgressBar buyProgressBarGreen;
        ProgressBar buyProgressBarRed;
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
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_today_special_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.shopNameItem);
            viewHolder.itemSales= (TypeFaceTextView) convertView.findViewById(R.id.shopSaleTv);
            viewHolder.itemIntorduce = (TypeFaceTextView) convertView.findViewById(R.id.shopIntroduceItem);
            viewHolder.itemCurrentPrice = (TextView) convertView.findViewById(R.id.shopCurrentPrice);
            viewHolder.itemFormerPrice = (TextView) convertView.findViewById(R.id.shopFormerPrice);
            viewHolder.itemBuy = (TypeFaceTextView) convertView.findViewById(R.id.buyGoodsBtn);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.shopGoodsImage);
            viewHolder.itemNull= (ImageView) convertView.findViewById(R.id.shopGoodsImageNo);
            viewHolder.buyCount= (TextView) convertView.findViewById(R.id.shopBuyCount);
            viewHolder.buyProgressBarGreen= (ProgressBar) convertView.findViewById(R.id.shopProgressBarGreen);
            viewHolder.buyProgressBarRed= (ProgressBar) convertView.findViewById(R.id.shopProgressBarRed);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ShopTodayItem todayShopItem=items.get(position);
        viewHolder.itemFormerPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        if (todayShopItem.totalCount!=0)
        {
            viewHolder.buyCount.setText("已抢购"+todayShopItem.percentum+"%");
            if(todayShopItem.percentum>=80)
            {
                viewHolder.buyProgressBarRed.setMax(todayShopItem.totalCount*10);
                viewHolder.buyProgressBarRed.setProgress(todayShopItem.percentum*todayShopItem.totalCount/10);
                viewHolder.buyProgressBarRed.setVisibility(View.VISIBLE);
                viewHolder.buyProgressBarGreen.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.buyProgressBarGreen.setMax(todayShopItem.totalCount*10);
                viewHolder.buyProgressBarGreen.setProgress(todayShopItem.percentum*todayShopItem.totalCount/10);
                viewHolder.buyProgressBarGreen.setVisibility(View.VISIBLE);
                viewHolder.buyProgressBarRed.setVisibility(View.GONE);
            }
        }
        viewHolder.itemName.setText("            "+todayShopItem.title);
        viewHolder.itemCurrentPrice.setText("￥"+ToolUtils.isIntPrice(""+todayShopItem.currentPrice));
        viewHolder.itemFormerPrice.getPaint().setAntiAlias(true);//去锯齿
        viewHolder.itemFormerPrice.setText("￥"+ToolUtils.isIntPrice(""+todayShopItem.formerPrice));
        viewHolder.itemIntorduce.setText(todayShopItem.comment);
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
            viewHolder.itemSales.setText("0折");
        }
//        ToolUtils.setImageUrl(todayShopItem.imageUrl,viewHolder.itemImage,R.drawable.icon_loading_defalut);
        ToolUtils.setImageNoResetUrl(todayShopItem.imageUrl,viewHolder.itemImage,R.drawable.icon_loading_defalut);

        viewHolder.itemBuy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                GoodsDetailsFragment goodsDetailsFragment = GoodsDetailsFragment.newInstance(items.get(position).title, items.get(position).goodsId);
                ((MainActivity) context).navigationToFragment(goodsDetailsFragment);
            }
        });

        if (todayShopItem.num>0)
        {
            viewHolder.itemNull.setVisibility(View.GONE);
            viewHolder.itemBuy.setBackgroundResource(R.drawable.btn_red_click_selector);
            viewHolder.itemBuy.setText("马上抢");
            viewHolder.itemBuy.setClickable(true);
        }
        else
        {
            viewHolder.itemNull.setVisibility(View.VISIBLE);
            viewHolder.itemBuy.setBackgroundResource(R.drawable.btn_no_click_selector);
            viewHolder.itemBuy.setText("抢光了");
            viewHolder.itemBuy.setClickable(false);
        }
        return convertView;
    }
}
