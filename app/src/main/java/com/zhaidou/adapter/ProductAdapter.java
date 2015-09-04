package com.zhaidou.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.Product;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.ToolUtils;

import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by wangclark on 15/7/10.
 */
public class ProductAdapter extends BaseListAdapter<Product>{

    private int mCheckPosition=-1;
    private int pos;
    private int flags;
    private int screenWidth;

    private WeakHashMap<Integer,View> mHashMap = new WeakHashMap<Integer, View>();
    public ProductAdapter(Context context, List<Product> list,int flags,int screenWidth) {
        super(context, list);
        this.flags=flags;
        this.screenWidth=screenWidth;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        convertView=mHashMap.get(position);
        if (convertView==null)
            convertView=mInflater.inflate(R.layout.item_fragment_single,null);
        TextView tv_name = ViewHolder.get(convertView, R.id.tv_name);
        ImageView image =ViewHolder.get(convertView,R.id.iv_single_item);
        image.setLayoutParams(new LinearLayout.LayoutParams(screenWidth/2-1,(screenWidth/2-1)*175/186));
        TextView tv_money=ViewHolder.get(convertView,R.id.tv_money);
        ImageView iv_heart=ViewHolder.get(convertView,R.id.iv_heart);
        TextView tv_count=ViewHolder.get(convertView,R.id.tv_count);

        Product product = getList().get(position);
        tv_name.setText(product.getTitle());
        tv_money.setText("￥"+product.getPrice());
        tv_count.setText(product.getBean_like_count()+"");
        ToolUtils.setImageCacheUrl("http://"+product.getImage(),image);

        iv_heart.setImageResource(R.drawable.heart_normal);
        if (pos==position)
        {
            if (mCheckPosition==position)
            {
                iv_heart.setImageResource(R.drawable.heart_pressed);
            }
            else
            {
                iv_heart.setImageResource(R.drawable.heart_normal);
            }
        }
        if (flags==1)
        {
            iv_heart.setVisibility(View.GONE);
        }
        tv_count.setVisibility(View.GONE);
        if (product.isCollect())
        {
            iv_heart.setImageResource(R.drawable.heart_pressed);
            iv_heart.setPressed(true);
            iv_heart.setSelected(true);
            tv_count.setVisibility(View.GONE);
        }
        mHashMap.put(position,convertView);
        return convertView;
    }

    public void setmCheckPosition(int pos,int mCheckPosition)
    {
        this.pos=pos;
        this.mCheckPosition=mCheckPosition;
    }
}
