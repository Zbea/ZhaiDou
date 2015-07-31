package com.zhaidou.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.dialog.CustomShopCartDeleteDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/29.
 */
public class ShopCartLoseAdapter extends BaseAdapter
{

    private List<CartItem> items;
    private ViewHolder viewHolder;
    private Context context;

    public void clear()
    {
        this.items.clear();
        notifyDataSetChanged();
    }

    public ShopCartLoseAdapter(Context context, List<CartItem> items)
    {
        this.context = context;
        this.items = items;
    }

    class ViewHolder
    {
        TypeFaceTextView itemSize;
        TypeFaceTextView itemName;
        TextView itemCurrentPrice;
        TextView itemFormalPrice;
        ImageView itemImage, itemDeleteBtn, itemLine;
        TypeFaceTextView itemNum;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_cart_goods_lose_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.cartItemNameTv);
            viewHolder.itemSize = (TypeFaceTextView) convertView.findViewById(R.id.cartItemSizeTv);
            viewHolder.itemCurrentPrice = (TextView) convertView.findViewById(R.id.cartItemCurrentPrice);
            viewHolder.itemFormalPrice = (TextView) convertView.findViewById(R.id.cartItemFormalPrice);
            viewHolder.itemNum = (TypeFaceTextView) convertView.findViewById(R.id.cartItemNum);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.cartImageItemTv);
            viewHolder.itemDeleteBtn = (ImageView) convertView.findViewById(R.id.cartItemDelBtn);
            viewHolder.itemLine = (ImageView) convertView.findViewById(R.id.cartItemLine);

            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final CartItem cartItem = items.get(position);

        viewHolder.itemName.setText(cartItem.name);
        viewHolder.itemSize.setText(cartItem.size);
        viewHolder.itemCurrentPrice.setText("￥ " + cartItem.currentPrice);
        viewHolder.itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        viewHolder.itemFormalPrice.setText("￥ " + cartItem.formalPrice);
        viewHolder.itemNum.setText(cartItem.num);
        ToolUtils.setImageCacheUrl(cartItem.imageUrl, viewHolder.itemImage);

        viewHolder.itemDeleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
//                CustomShopCartDeleteDialog.setDelateDialog(context, cartItem);
            }
        });

        return convertView;
    }
}
