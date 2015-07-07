package com.zhaidou;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by wangclark on 15/7/2.
 */
public class ZDApplication extends Application{
    private Typeface mTypeFace;
    @Override
    public void onCreate() {
        super.onCreate();
        initTypeFace();
    }

    private void initTypeFace(){
        if (mTypeFace==null){
            mTypeFace =Typeface.createFromAsset(getAssets(), "FZLTXHK.TTF");
        }
    }

    public Typeface getTypeFace() {
        return mTypeFace;
    }

    @Override
    public String toString() {
        return "ZDApplication{" +
                "mTypeFace=" + mTypeFace +
                '}';
    }
}
