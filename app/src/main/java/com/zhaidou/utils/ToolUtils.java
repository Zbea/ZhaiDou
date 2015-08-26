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
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

//        ImageLoader.getInstance().init(getImageLoaderConfiguration(imageView.getContext()));
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
                .showImageOnLoading(R.drawable.icon_loading_osale)
                .showImageForEmptyUri(R.drawable.icon_loading_osale)
                .showImageOnFail(R.drawable.icon_loading_osale)
                .resetViewBeforeLoading(true)//default 设置图片在加载前是否重置、复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoader.getInstance().displayImage(url, imageView,options);
    }

    private static ImageLoaderConfiguration getImageLoaderConfiguration(Context context){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                // max width, max height，即保存的每个缓存文件的最大长宽
                .memoryCacheExtraOptions(480, 800)
                        // Can slow ImageLoader, use it carefully (Better don't use it)设置缓存的详细信息，最好不要设置这个
                        //.discCacheExtraOptions(480, 800, Bitmap.CompressFormat.JPEG, 75, null)
                        // 线程池内加载的数量
                .threadPoolSize(3)
                        // 线程优先级
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()

                        // You can pass your own memory cache implementation你可以通过自己的内存缓存实现
                        // .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                        // .memoryCacheSize(2 * 1024 * 1024)
                        //硬盘缓存50MB
                .diskCacheSize(50 * 1024 * 1024)
                        //将保存的时候的URI名称用MD5
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                        // 加密
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())//将保存的时候的URI名称用HASHCODE加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheFileCount(100) //缓存的File数量
//            .diskCache(new UnlimitedDiscCache(cacheDir))// 自定义缓存路径
                        // .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        // .imageDownloader(new BaseImageDownloader(context, 5 * 1000,
                        // 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
//                .writeDebugLogs() // Remove for release app
                .build();
        return config;
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
