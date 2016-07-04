package com.zhaidou.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.CartGoodsItem;
import com.zhaidou.utils.ToolUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zbea on 16/7/4.
 */
public class ShopCartAdapter extends BaseListAdapter<CartGoodsItem>
{
    private List<CartGoodsItem> items=new ArrayList<CartGoodsItem>();
    private Handler mHandler;
    private static List<CartGoodsItem> itemChecks=new ArrayList<CartGoodsItem>();
    private Map<Integer,Boolean> selected=new HashMap<Integer, Boolean>();
    private Map<Integer,View> views=new HashMap<Integer, View>();
    private static HashMap<Integer, Boolean> isSelected=new HashMap<Integer, Boolean>();
    private static HashMap<Integer, Integer> numbers=new HashMap<Integer, Integer>();

    public ShopCartAdapter(Context context, List<CartGoodsItem> list,Handler handler)
    {
        super(context, list);
        items=list;
        mHandler=handler;
    }

    @Override
    public void setList(List<CartGoodsItem> list)
    {
        items=list;
        super.setList(list);
    }

    @Override
    public View bindView(final int position, View convertView, ViewGroup parent)
    {
        convertView=views.get(position);
        if (convertView==null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.shop_cart_goods_item, null);
        TextView itemName = ViewHolder.get(convertView, R.id.cartItemNameTv);
        TextView itemSize = ViewHolder.get(convertView, R.id.cartItemSizeTv);
        TextView itemflags = ViewHolder.get(convertView, R.id.cartItemIsFlags);
        TextView itemCurrentPrice = ViewHolder.get(convertView, R.id.cartItemCurrentPrice);
        TextView itemFormalPrice = ViewHolder.get(convertView, R.id.cartItemFormalPrice);
        TextView itemSubBtn = ViewHolder.get(convertView, R.id.cartItemSubBtn);
        TextView itemAddBtn = ViewHolder.get(convertView, R.id.cartItemAddBtn);
        TextView itemNum = ViewHolder.get(convertView, R.id.cartItemNum);
        TextView itemLoseNum = ViewHolder.get(convertView, R.id.cartItemLoseNum);
        TextView numLimit = ViewHolder.get(convertView, R.id.cartItemNumLimit);
        ImageView itemImage = ViewHolder.get(convertView, R.id.cartImageItemTv);
        CheckBox itemCheck = ViewHolder.get(convertView, R.id.chatItemCB);
        TextView isOver = ViewHolder.get(convertView, R.id.cartItemIsOver);
        TextView islose = ViewHolder.get(convertView, R.id.cartItemIsLose);
        TextView isDate = ViewHolder.get(convertView, R.id.cartItemIsDate);
        ImageView itemDeleteBtn = ViewHolder.get(convertView, R.id.cartItemDelBtn);
        ImageView itemLine = ViewHolder.get(convertView, R.id.cartItemLine);
        LinearLayout cartNumView = ViewHolder.get(convertView, R.id.cartNumView);
        LinearLayout cartNumLoseView = ViewHolder.get(convertView, R.id.cartNumLoseView);

        if (getCount() > 1)
        {
            if (position == getCount() - 1)
            {
                itemLine.setVisibility(View.GONE);
            }
            if (position == 0)
            {
                itemLine.setVisibility(View.VISIBLE);
            }
        } else
        {
            itemLine.setVisibility(View.GONE);
        }

        final CartGoodsItem cartGoodsItem=getItem(position);

        //判断商品是否下架或者卖光处理
        if (!cartGoodsItem.isOver.equals("true")&&!cartGoodsItem.isPublish.equals("true")&&!cartGoodsItem.isDate.equals("true"))
        {
            itemflags.setVisibility(View.GONE);
            cartNumView.setVisibility(View.VISIBLE);
            cartNumLoseView.setVisibility(View.GONE);
            itemCheck.setVisibility(View.VISIBLE);
            itemCheck.setChecked(false);

            itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    if (!b)
                    {
                        cartGoodsItem.isCheck=true;
                        isSelected.put(position,true);
                        itemChecks.add(cartGoodsItem);
                    }
                    else
                    {
                        cartGoodsItem.isCheck=false;
                        isSelected.put(position,false);
                        itemChecks.remove(cartGoodsItem);
                    }
                    mHandler.sendEmptyMessage(10);
                }
            });
        } else
        {
            itemCheck.setVisibility(View.GONE);
            cartNumView.setVisibility(View.GONE);
            itemflags.setVisibility(View.VISIBLE);
            cartNumLoseView.setVisibility(View.VISIBLE);
            itemName.setTextColor(ColorStateList.valueOf(R.color.text_gary_color));
        }

        numLimit.setVisibility(cartGoodsItem.num > cartGoodsItem.count ? View.VISIBLE : View.GONE);

        if (cartGoodsItem.isPublish.equals("true"))
        {
            isOver.setVisibility(View.GONE);
            islose.setVisibility(View.VISIBLE);
            isDate.setVisibility(View.GONE);
        }
        if (cartGoodsItem.isDate.equals("true"))
        {
            isOver.setVisibility(View.GONE);
            islose.setVisibility(View.GONE);
            isDate.setVisibility(View.VISIBLE);
        }
        if (cartGoodsItem.isOver.equals("true"))
        {
            isOver.setVisibility(View.VISIBLE);
            islose.setVisibility(View.GONE);
            isDate.setVisibility(View.GONE);
        }
        if (cartGoodsItem.isOSale.equals("true"))
        {
            cartNumView.setVisibility(View.GONE);
            cartNumLoseView.setVisibility(View.VISIBLE);
        }
        itemCheck.setChecked(cartGoodsItem.isCheck);
        itemName.setText(cartGoodsItem.name);
        itemSize.setText(cartGoodsItem.size);
        itemCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.currentPrice));
        itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        itemFormalPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.formalPrice));
        itemNum.setText("" + cartGoodsItem.num);
        itemLoseNum.setText("" + cartGoodsItem.num);
        ToolUtils.setImageCacheUrl(cartGoodsItem.imageUrl, itemImage, R.drawable.icon_loading_defalut);

        views.put(position,convertView);
        return null;
    }


    public final static  List<CartGoodsItem> getItemChecks()
    {
        return itemChecks;
    }

    public final static void setItemChecks(List<CartGoodsItem> itemChecks)
    {
        ShopCartAdapter.itemChecks=itemChecks;
    }
}
