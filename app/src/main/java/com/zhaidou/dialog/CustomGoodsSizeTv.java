package com.zhaidou.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.model.Specification;
import com.zhaidou.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zbea on 16/7/5.
 */
public class CustomGoodsSizeTv
{
    private Context mContext;
    private List<TextView> textViews=new ArrayList<TextView>();

    /**
     *
     * @param mContext 上下文
     * @param isType//规格类型1为父规格，2单纬规格，3子规格
     * @param mSizeId//指定sizeId
     * @param views//该规格子类全部textView集合
     * @param specifications//该类全部规格
     * @param onInitListener
     * @param onClickListener
     */
    public void showSizeView(Context mContext,int isType,String mSizeId,FlowLayout views, final List<Specification> specifications,OnInitSizeListener onInitListener,final OnClickSizeListener onClickListener)
    {
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 5;
        lp.topMargin = 10;
        lp.bottomMargin = 5;
        for (int i = 0; i <specifications.size() ; i++)
        {
            final int position = i;
            Specification specification = specifications.get(i);
            View view = LayoutInflater.from(mContext).inflate(R.layout.goods_details_size_item, null);
            final TextView textView = (TextView) view.findViewById(R.id.sizeTitleTv);
            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onClickListener.onClick(position,textViews,textView,specifications.get(position));
                }
            });

            if (isType==1)//是否为父亲规格
            {
                if (mSizeId!=null)
                {
                    for (int j = 0; j < specification.sizess.size(); j++)
                    {
                        if (mSizeId.equals(specification.sizess.get(j).sizeId))
                        {
                            textView.setSelected(true);
                            onInitListener.onInit(position,textView,specification);
                        }
                    }
                } else
                {
                    if (0 == position)
                    {
                        textView.setSelected(true);
                        onInitListener.onInit(position,textView,specification);
                    }
                }
            }
            else//不为父时要考虑规格数量为0的情况
            {
                if (specification.num < 1)
                {
                    textView.setBackgroundResource(R.drawable.goods_no_click_selector);
                    textView.setTextColor(Color.parseColor("#999999"));
                    textView.setClickable(false);
                } else
                {
                    if (mSizeId!=null)//当初始规格不为null时，默认选中初始规格
                    {
                        if (specification.sizeId.equals(mSizeId+""))
                        {
                            textView.setSelected(true);
                            onInitListener.onInit(position,textView,specification);
                        }
                    }
                }
            }
            textView.setText(isType!=3?specification.title:specification.title1);
            textViews.add(textView);
            views.addView(view, lp);
        }
    }


    public interface OnInitSizeListener
    {
        void onInit(int position,TextView textView,Specification specification);
    }

    public interface OnClickSizeListener
    {
        void onClick(int position,List<TextView> texts,TextView textView,Specification specification);
    }

}
