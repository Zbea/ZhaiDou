package com.zhaidou.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.zhaidou.R;
import com.zhaidou.model.ShopSpecialItem;
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

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public ShopSpecialAdapter(Context context, List<ShopSpecialItem> items)
    {
        this.context = context;
        this.items = items;
    }

    class ViewHolder
    {
        TypeFaceTextView itemSale;
        TypeFaceTextView itemName;
        ImageView itemImage;
        TypeFaceTextView itemTime;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_special_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.shop_name_item);
            viewHolder.itemSale = (TypeFaceTextView) convertView.findViewById(R.id.shop_name_sale);
            viewHolder.itemTime = (TypeFaceTextView) convertView.findViewById(R.id.shop_time_item);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.itemsImageIv);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ShopSpecialItem shopSpecialItem=items.get(position);

        viewHolder.itemName.setText(shopSpecialItem.title);
        viewHolder.itemSale.setText(shopSpecialItem.sale);
        viewHolder.itemTime.setText(shopSpecialItem.overTime);
        ToolUtils.setImageCacheUrl(shopSpecialItem.imageUrl,viewHolder.itemImage);

        return convertView;
    }
}
