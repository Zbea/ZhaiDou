package com.zhaidou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhaidou.R;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import java.util.List;

/**
 * Created by roy on 15/7/23.
 */
public class ShopSpecialAdapter extends BaseAdapter
{

    private List<ShopSpecialItem> items;
    private ViewHolder viewHolder;
    private Context context;
    private int screenWidth;

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public ShopSpecialAdapter(Context context, List<ShopSpecialItem> items,int screenWidth)
    {
        this.context = context;
        this.items = items;
        this.screenWidth=screenWidth;
    }

    class ViewHolder
    {
        TypeFaceTextView itemSale;
        TypeFaceTextView itemName;
        ImageView itemImage;
        TypeFaceTextView itemTime;
        ImageView isNewsView;
        ImageView newView;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_home_shop_special, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.shop_name_item);
            viewHolder.itemSale = (TypeFaceTextView) convertView.findViewById(R.id.shop_name_sale);
            viewHolder.itemTime = (TypeFaceTextView) convertView.findViewById(R.id.shop_time_item);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.itemsImageIv);
            viewHolder.itemImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth,screenWidth*316/722));
            viewHolder.isNewsView=(ImageView)convertView.findViewById(R.id.newsView);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ShopSpecialItem shopSpecialItem=items.get(position);
        viewHolder.itemName.setText(shopSpecialItem.title);
        viewHolder.itemSale.setText(shopSpecialItem.sale);
        viewHolder.itemTime.setText(""+shopSpecialItem.overTime);

        if (shopSpecialItem.isNew==1)
        {
            if (!(Boolean) SharedPreferencesUtil.getData(context, "homeNews_" + shopSpecialItem.goodsId, true))
            {
                viewHolder.isNewsView.setVisibility(View.GONE);
            } else
            {
                viewHolder.isNewsView.setVisibility(View.VISIBLE);
            }
        } else
        {
            viewHolder.isNewsView.setVisibility(View.GONE);
        }
        ToolUtils.setImageCacheUrl(shopSpecialItem.imageUrl, viewHolder.itemImage, R.drawable.icon_loading_item);
        return convertView;
    }
}
