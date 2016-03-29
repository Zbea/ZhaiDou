package com.zhaidou.view;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaidou.R;
import com.zhaidou.utils.PixelUtil;

import java.util.List;

/**
 * Created by wangclark on 15/7/8.
 */
public class AutoGridView extends LinearLayout implements View.OnClickListener{
    private List<String> mHistoryList;
    private Context mContext;
    private LinearLayout mChildLinearLayout;
    private float mTextWidth=0;
    private int screenWidth;
    private String[] array={"哈哈","我的","好大啊","嗒嗒嗒嗒","好大好大卡","饭卡发回合肥的","打算咖啡壶","举案说法哦","嘻嘻","嘎嘎","都回家啊啊发噶u发噶u发噶u啊发"};

    private final int TEXT_LENGTH=6;
    private int dp_55;
    private int dp_5;
    private int dp_10;
    private int dp_20;

    private OnHistoryItemClickListener onHistoryItemClickListener;
    public AutoGridView(Context context) {
        super(context);
        mContext=context;
        setOrientation(LinearLayout.VERTICAL);
        screenWidth=this.getMeasuredWidth();
        calculateDP(context);
    }

    public AutoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        setOrientation(LinearLayout.VERTICAL);
        screenWidth=this.getMeasuredWidth();
        calculateDP(context);
    }

    public AutoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
        setOrientation(LinearLayout.VERTICAL);
        screenWidth=this.getMeasuredWidth();
        calculateDP(context);
    }


    public void setHistoryList(List<String> mHistoryList) {
        Log.i("setHistoryList----->","setHistoryList");
        this.mHistoryList = mHistoryList;
        init(mContext,mHistoryList);
    }
    private void init(Context context,List<String> list){
        Log.i("init-------->",list.size()+"");
//        removeAllViews();
        for (int i=0;i<list.size();i++){
            mChildLinearLayout=new LinearLayout(mContext);
            mChildLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp_10,dp_10,dp_10, 0);
            mChildLinearLayout.setLayoutParams(lp);

            while (mTextWidth<getMeasuredWidth()&&i<list.size()){
                View view= LayoutInflater.from(mContext).inflate(R.layout.view_autogridview_item,null);
                TextView textView =(TextView)view.findViewById(R.id.tv_history_item);
//                TypeFaceTextView textView=new TypeFaceTextView(context);
//                textView.setId(100);
//                textView.setClickable(true);
//                textView.setSingleLine(true);
//                textView.setMinWidth(dp_55);
//                textView.setPadding(dp_10,dp_5,dp_10,dp_5);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0,0,dp_10,0);
                textView.setLayoutParams(layoutParams);
//                textView.setGravity(Gravity.CENTER);
//                textView.setTextSize(16);

                textView.setTag(i);
                textView.setOnClickListener(this);
//                textView.setTextColor(getResources().getColorStateList(R.color.text_color_selector));
//                textView.setTextColor(Color.parseColor("#000000"));
//                textView.setBackgroundResource(R.drawable.search_item_selector);

                TextPaint paint = textView.getPaint();
                String item=list.get(i);
                if (item.length()>TEXT_LENGTH)
                    item=item.substring(0,TEXT_LENGTH)+"...";
                Log.i("item----------------->",item);
                textView.setTextSize(13);
                textView.setText(item);
                float length =paint.measureText(item);
//                textView.setTextColor(getResources().getColor(R.color.gray_9));

                if (length+dp_10+dp_10<dp_55){
                    mTextWidth=dp_55+mTextWidth+dp_10;
                }else {
                    mTextWidth=length+dp_10+dp_10+dp_10+mTextWidth;
                }
                if (mTextWidth>=getMeasuredWidth()-dp_10*2){
                    mTextWidth=0;
                    --i;
                    break;
                }
                ++i;
                mChildLinearLayout.addView(textView);

            }
            addView(mChildLinearLayout);
        }
        mTextWidth=0;
    }

    private void calculateDP(Context context){
        dp_10=PixelUtil.dp2px(10,context);
        dp_20=PixelUtil.dp2px(20,context);
        dp_5=PixelUtil.dp2px(5,context);
        dp_55=PixelUtil.dp2px(60,context);
    }
    public List<String> getHistoryList() {
        return mHistoryList;
    }

    public void clear(){
        removeAllViews();
        Log.i("ddd----->",getChildCount()+"");
    }

    @Override
    public void onClick(View view) {
        Integer position=(Integer)view.getTag();
        onHistoryItemClickListener.onHistoryItemClick(position,mHistoryList.get(position));
    }

    public void setOnHistoryItemClickListener(OnHistoryItemClickListener onHistoryItemClickListener) {
        this.onHistoryItemClickListener = onHistoryItemClickListener;
    }

    public interface OnHistoryItemClickListener{
        public void onHistoryItemClick(int position,String history);
    }
}
