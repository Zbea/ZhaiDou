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

/**
 * Created by Zbea on 16/7/4.
 */
public class ShopCartAdapter extends BaseListAdapter<CartGoodsItem>
{
    private Handler mHandler;
    private static List<CartGoodsItem> itemChecks=new ArrayList<CartGoodsItem>();
    private static HashMap<String, Boolean> isSelected=new HashMap<String, Boolean>();

    public ShopCartAdapter(Context context, List<CartGoodsItem> list,Handler handler)
    {
        super(context, list);
        mHandler=handler;
    }

    @Override
    public void setList(List<CartGoodsItem> list)
    {
        HashMap<String, Boolean> initSelected=new HashMap<String, Boolean>();
        for (int i = 0; i < list.size(); i++)
        {
            String sizeId=list.get(i).sizeId;
            initSelected.put(sizeId, isSelected.get(sizeId)!=null?isSelected.get(sizeId):false);
        }
        isSelected=initSelected;
        super.setList(list);
    }

    @Override
    public View bindView(final int position, View convertView, ViewGroup parent)
    {
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

        final CartGoodsItem cartGoodsItem=getList().get(position);

        //判断商品是否下架或者卖光处理
        if (!cartGoodsItem.isOver.equals("true")&&!cartGoodsItem.isPublish.equals("true")&&!cartGoodsItem.isDate.equals("true"))
        {
            itemflags.setVisibility(View.GONE);
            cartNumView.setVisibility(View.VISIBLE);
            cartNumLoseView.setVisibility(View.GONE);
            itemCheck.setVisibility(View.VISIBLE);
            itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    if (b)
                    {
                        isSelected.put(cartGoodsItem.sizeId,true);
                    }
                    else
                    {
                        isSelected.put(cartGoodsItem.sizeId,false);
                    }
                    setRefreshCheckView();
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
        itemCheck.setChecked(isSelected.get(cartGoodsItem.sizeId)?true:false);
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
        itemName.setText(cartGoodsItem.name);
        itemSize.setText(cartGoodsItem.size);
        itemCurrentPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.currentPrice));
        itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        itemFormalPrice.setText("￥" + ToolUtils.isIntPrice("" + cartGoodsItem.formalPrice));
        itemNum.setText("" + cartGoodsItem.num);
        itemLoseNum.setText("" + cartGoodsItem.num);
        ToolUtils.setImageCacheUrl(cartGoodsItem.imageUrl, itemImage, R.drawable.icon_loading_defalut);
        return convertView;
    }


    public List<CartGoodsItem> getItemChecks()
    {
        itemChecks.clear();
        for (int i = 0; i <getList().size() ; i++)
        {
            CartGoodsItem cartGoodsItem=getList().get(i);
            if (isSelected.get(cartGoodsItem.sizeId))
            {
                if (!cartGoodsItem.isOver.equals("true")&&!cartGoodsItem.isPublish.equals("true")&&!cartGoodsItem.isDate.equals("true"))
                    itemChecks.add(cartGoodsItem);
            }
        }
        return itemChecks;
    }

    public HashMap<String, Boolean> getIsSelected()
    {
        return isSelected;
    }

    public void setIsSelected(CartGoodsItem cartGoodsItem)
    {
        isSelected.remove(cartGoodsItem.sizeId);
    }
    /**
     * 刷新选中数据
     */
    public void setRefreshCheckView()
    {
        mHandler.sendEmptyMessage(10);
    }
}
