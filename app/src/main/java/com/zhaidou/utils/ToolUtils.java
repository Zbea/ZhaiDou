package com.zhaidou.utils;

import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhaidou.R;

/**
 * Created by roy on 15/7/15.
 */
public class ToolUtils
{
    /**
     * 是否存在sdcard
     * @return
     */
    public static boolean hasSdcard()
    {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) && Environment.getExternalStorageDirectory().exists())
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * 图片异步加载（缓存图片方法）
     * @param url
     * @param imageView
     */
    public static final void setImageCacheUrl(String url,ImageView imageView)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                //	.displayer(new RoundedBitmapDisplayer(20))//设置圆角半径
                .showImageForEmptyUri(R.drawable.icon_loading_defalut)
                .showImageOnFail(R.drawable.icon_loading_defalut)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 图片异步加载（不缓存图片设置）
     * @param url
     * @param imageView
     */
    public static final void setImageUrl(String url,ImageView imageView)
    {
        ImageLoader.getInstance().displayImage(url, imageView);
    }

    /**
     * 打印信息
     * @param msg
     */
    public static final void setLog(String msg)
    {
        Log.i("zhaidou",msg);
    }

}
