package com.zhaidou.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.zhaidou.R;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by roy on 15/7/15.
 */
public class ToolUtils
{

    /**
     * 处理价格为.0或者.00时取整数
     * @return
     */
    public static String isIntPrice(String price)
    {
        String mPrice=null;
        for (int i=0 ;i<price.length(); i++)
        {
            char c = price.charAt(i);
            //当不为整数的时候
            if ('.' == c)
            {
                //当存在小数点一位的时候
                if (price.length() == (i + 2))
                {
                    if (String.valueOf(price.charAt(i+1)).equals(""+0))
                    {
                        mPrice=price.substring(0,i);
                        return mPrice;
                    }
                }
                //当存在小数点两位时候
               if (price.length()==(i+3))
               {
                   if (String.valueOf(price.charAt(i+1)).equals(""+0)&&String.valueOf(price.charAt(i+2)).equals(""+0))
                   {
                       mPrice=price.substring(0,i);
                       return mPrice;
                   }
               }
            }
        }
        mPrice=price;
        return mPrice;
    }

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
     * 判断手机号码格式是否正确
     * @param phone
     * @return
     */
    public static boolean isPhoneOk(String phone)
    {
        Pattern p=Pattern.compile("(1[358]\\d{9})|(14[57]\\d{8})|(17[0678]\\d{8})");
        Matcher m=p.matcher(phone);
        return m.matches();
    }

    /**
     * 判断邮箱格式是否正确
     * @param email
     * @return
     */
    public static boolean isEmailOK(String email)
    {
        if (email!=null&email.length()>0)
        {
            Pattern p=Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+");
            Matcher m=p.matcher(email);
            return m.matches();
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
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 图片异步加载（缓存图片方法）
     * @param url
     * @param imageView
     * @param resId 设置加载过程中背景底图
     */
    public static final void setImageCacheUrl(String url,ImageView imageView,int resId)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(resId)
                .showImageForEmptyUri(resId)
                .showImageOnFail(resId)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .cacheInMemory(true) // default  设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default  设置下载的图片是否缓存在SD卡中
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
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
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icon_loading_osale)
                .showImageOnFail(R.drawable.icon_loading_osale)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 图片异步加载（不缓存图片设置）
     * @param url
     * @param imageView
     * @param resId 设置加载过程中背景底图
     */
    public static final void setImageUrl(String url,ImageView imageView,int resId)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .showImageOnLoading(resId)
                .showImageForEmptyUri(R.drawable.icon_loading_osale)
                .showImageOnFail(R.drawable.icon_loading_osale)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 本地图片异步加载（不缓存图片设置，处理内存溢出）
     * @param url
     * @param imageView
     */
    public static final void setImagePreventMemoryLeaksUrl(String url,ImageView imageView)
    {
        DisplayImageOptions options=new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    /**
     * 打印信息
     * @param msg
     */
    public static final void setLog(String msg)
    {
        Log.i("zhaidou",msg);
    }


    public static final void setToast(Context mContext,String msg)
    {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

}
