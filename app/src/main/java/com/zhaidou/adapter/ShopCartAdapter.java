package com.zhaidou.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.dialog.CustomShopCartDeleteDialog;
import com.zhaidou.model.CartItem;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.TypeFaceTextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/7/29.
 */
public class ShopCartAdapter extends BaseAdapter
{

    private List<CartItem> items;
    private ViewHolder viewHolder;
    private Context context;
    private CreatCartDB creatCartDB;
    public List<CartItem> checkItems = new ArrayList<CartItem>();

    /**
     * 刷新
     */
    public void refresh()
    {
        notifyDataSetChanged();
    }

    public void allNoChecks()
    {
        checkItems.removeAll(checkItems);
    }

    public void allChecks()
    {
        checkItems.removeAll(checkItems);
        checkItems=items;
    }

    public ShopCartAdapter(Context context, List<CartItem> items)
    {
        this.context = context;
        this.items = items;
        creatCartDB = new CreatCartDB(context);
    }

    class ViewHolder
    {
        TypeFaceTextView itemSize;
        TypeFaceTextView itemName;
        TypeFaceTextView itemCurrentPrice;
        TypeFaceTextView itemFormalPrice;
        ImageView itemImage, itemDeleteBtn, itemLine;
        TypeFaceTextView itemSubBtn, itemNum, itemAddBtn;
        CheckBox itemCheck;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_cart_goods_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.cartItemNameTv);
            viewHolder.itemSize = (TypeFaceTextView) convertView.findViewById(R.id.cartItemSizeTv);
            viewHolder.itemCurrentPrice = (TypeFaceTextView) convertView.findViewById(R.id.cartItemCurrentPrice);
            viewHolder.itemFormalPrice = (TypeFaceTextView) convertView.findViewById(R.id.cartItemFormalPrice);
            viewHolder.itemSubBtn = (TypeFaceTextView) convertView.findViewById(R.id.cartItemSubBtn);
            viewHolder.itemAddBtn = (TypeFaceTextView) convertView.findViewById(R.id.cartItemAddBtn);
            viewHolder.itemNum = (TypeFaceTextView) convertView.findViewById(R.id.cartItemNum);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.cartImageItemTv);
            viewHolder.itemCheck = (CheckBox) convertView.findViewById(R.id.chatItemCB);
            viewHolder.itemDeleteBtn = (ImageView) convertView.findViewById(R.id.cartItemDelBtn);
            viewHolder.itemLine = (ImageView) convertView.findViewById(R.id.cartItemLine);
            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CartItem cartItem = items.get(position);


        if (items.size()>1)
        {
            if (position == items.size()-1)
            {
                viewHolder.itemLine.setVisibility(View.GONE);
            }
            if (position == 0)
            {
                viewHolder.itemLine.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            viewHolder.itemLine.setVisibility(View.GONE);
        }
        checkItems.removeAll(checkItems);

        viewHolder.itemName.setText(cartItem.name);
        viewHolder.itemSize.setText(cartItem.size);
        viewHolder.itemCurrentPrice.setText("￥ " + cartItem.currentPrice);
        viewHolder.itemFormalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        viewHolder.itemFormalPrice.setText("￥ " + cartItem.formalPrice);
        viewHolder.itemNum.setText("" + cartItem.num);
        ToolUtils.setImageCacheUrl(cartItem.imageUrl, viewHolder.itemImage);

        viewHolder.itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if (b)
                {
                    cartItem.isCheck=true;
                    checkItems.add(cartItem);
                } else
                {
                    cartItem.isCheck=false;
                    checkItems.remove(cartItem);
                }
                sendBroadCastCheckRefrsh();
            }
        });

        if (cartItem.isCheck)
        {
            viewHolder.itemCheck.setChecked(true);
        }
        else
        {
            viewHolder.itemCheck.setChecked(false);
        }



        viewHolder.itemDeleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
//                CustomShopCartDeleteDialog.setDelateDialog(context, cartItem);
                notifyDataSetChanged();
                checkItems.remove(cartItem);
                sendBroadCastCheckRefrsh();

            }
        });

        viewHolder.itemSubBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (cartItem.num - 1 > 0)
                {
                    cartItem.num = cartItem.num - 1;
                    CreatCartTools.editByData(creatCartDB,cartItem);
                } else
                {
//                    CustomShopCartDeleteDialog.setDelateDialog(context, cartItem);
                }
                sendBroadCastEditAll();
                editCheckNum();
            }

        });

        viewHolder.itemAddBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                cartItem.num = cartItem.num + 1;
                CreatCartTools.editByData(creatCartDB,cartItem);
                sendBroadCastEditAll();
                editCheckNum();
            }
        });

        return convertView;
    }

    /**
     * 修改选中数量，刷新广播
     */
    private void editCheckNum()
    {
        checkItems=getEditCheckGoods();
        ToolUtils.setLog("shuaxing选中1");
        sendBroadCastCheckRefrsh();
    }

    /**
     * 当添加删除修改数量后，item数据也要跟着变化
     * @return
     */
    private List<CartItem> getEditCheckGoods()
    {
        for (int i = 0; i <items.size() ; i++)
        {
            for (int j = 0; j <checkItems.size() ; j++)
            {
                if (items.get(i).sizeId==checkItems.get(j).sizeId)
                {
                    checkItems.get(j).num=items.get(i).num;
                }
            }
        }
        return checkItems;
    }

    /**
     * 发送全局修改数量广播刷新
     */
    public void sendBroadCastEditAll()
    {
        //发送数量修改广播
        Intent intent=new Intent(ZhaiDou.IntentRefreshCartGoodsTag);
        context.sendBroadcast(intent);
    }

    /**
     * 发送刷新选中广播
     */
    private void sendBroadCastCheckRefrsh()
    {
        ToolUtils.setLog("shuaxing选中2");
        //刷新选中广播
        Intent intent=new Intent(ZhaiDou.IntentRefreshCartGoodsCheckTag);
        context.sendBroadcast(intent);
    }



    /**
     * 得到选中的商品集合
     * @return
     */
    public List<CartItem> getCheckGoods()
    {
        return checkItems;
    }

}
