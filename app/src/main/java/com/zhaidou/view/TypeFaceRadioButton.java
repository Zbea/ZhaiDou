package com.zhaidou.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.zhaidou.ZDApplication;

/**
 * Created by roy on 15/8/15.
 */
public class TypeFaceRadioButton extends RadioButton
{
    public TypeFaceRadioButton(Context context)
    {
        super(context);
        initTypeFace(context);
    }

    public TypeFaceRadioButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initTypeFace(context);
    }

    public TypeFaceRadioButton(Context context, AttributeSet attrs, int defStyle)
    {
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
