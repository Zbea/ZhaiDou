package com.zhaidou.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.zhaidou.ZDApplication;

/**
 * Created by wangclark on 15/7/2.
 */
public class TypeFaceEditText extends EditText{
    public TypeFaceEditText(Context context) {
        super(context);
        initTypeFace(context);
    }

    public TypeFaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypeFace(context);
    }

    public TypeFaceEditText(Context context, AttributeSet attrs, int defStyle) {
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
