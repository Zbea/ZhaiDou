package com.zhaidou.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.zhaidou.R;
import com.zhaidou.model.ShopSpecialItem;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;
import com.zhaidou.view.RoundImageView;
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
        View itemLine;
        View itemLine1;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.shop_special_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TypeFaceTextView) convertView.findViewById(R.id.shop_name_item);
            viewHolder.itemSale = (TypeFaceTextView) convertView.findViewById(R.id.shop_name_sale);
            viewHolder.itemTime = (TypeFaceTextView) convertView.findViewById(R.id.shop_time_item);
            viewHolder.itemImage = (ImageView) convertView.findViewById(R.id.itemsImageIv);
            viewHolder.itemImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth,screenWidth*316/722));
            viewHolder.itemLine = (View) convertView.findViewById(R.id.itemsLine);
            viewHolder.itemLine1 = (View) convertView.findViewById(R.id.itemsLine1);
            viewHolder.isNewsView=(ImageView)convertView.findViewById(R.id.newsView);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ShopSpecialItem shopSpecialItem=items.get(position);
        if (position==0)
        {
            viewHolder.itemLine.setVisibility(View.GONE);
            viewHolder.itemLine1.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.itemLine.setVisibility(View.VISIBLE);
            viewHolder.itemLine1.setVisibility(View.VISIBLE);
        }
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
//        ToolUtils.setImageNoResetUrl(shopSpecialItem.imageUrl, viewHolder.itemImage,R.drawable.icon_loading_item);

        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icon_loading_item)
                .showImageOnFail(R.drawable.icon_loading_item)
                .resetViewBeforeLoading(false)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new RoundedBitmapDisplayer(8))//设置圆角半径
                .build();
        ImageLoader.getInstance().displayImage(shopSpecialItem.imageUrl, viewHolder.itemImage,options);
        return convertView;
    }
}
