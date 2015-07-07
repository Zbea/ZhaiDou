package com.zhaidou.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.zhaidou.ZDApplication;

/**
 * Created by wangclark on 15/7/2.
 */
public class TypeFaceTextView extends TextView {
    public TypeFaceTextView(Context context) {
        super(context);
        initTypeFace(context);
    }
    public TypeFaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypeFace(context);
    }

    public TypeFaceTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTypeFace(context);
    }
    public void initTypeFace(Context context){
        ZDApplication application =(ZDApplication)context.getApplicationContext();
        Typeface mTypeFace = application.getTypeFace();
        if (mTypeFace!=null){
            setTypeface(mTypeFace);
        }
    }
}
