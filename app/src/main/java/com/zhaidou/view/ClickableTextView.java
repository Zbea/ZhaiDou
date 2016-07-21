package com.zhaidou.view;/**
 * Created by wangclark on 16/5/3.
 */

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-05-03
 * Time: 17:24
 * Description:可点击的Textview
 * FIXME
 */
public class ClickableTextView extends TypeFaceTextView {
    int position;
    public ClickableTextView(Context context) {
        super(context);
    }

    public ClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setClickText(String text,List<String> ids,OnTextClickListener onTextClickListener,int position){
        this.position=position;
        setMovementMethod(LinkMovementMethod.getInstance());
        setText(addClickablePart(text,ids,onTextClickListener), BufferType.SPANNABLE);
    }

    private SpannableStringBuilder addClickablePart(String str, List<String> ids, final OnTextClickListener onTextClickListener) {
        if (TextUtils.isEmpty(str))
            return null;
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(str);
        List<String> mCategories = Arrays.asList(str.split("、"));
        if (mCategories.size() > 0) {
            // 最后一个
            for (int i = 0; i < mCategories.size(); i++)
            {
                final String category =mCategories.get(i);
                final String id = ids.get(i);
                final int start = str.indexOf(category);
                ssb.setSpan(new ClickableSpan() {

                    @Override
                    public void onClick(View widget) {
                        onTextClickListener.onTextClick(category,id,position);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(Color.parseColor("#666666"));
                        ds.setUnderlineText(false);
                    }

                }, start, start + category.length(), 0);
            }
        }
        return ssb;
    }

    public interface OnTextClickListener{
        public void onTextClick(String categoryStr,String id,int position);
    }
}
