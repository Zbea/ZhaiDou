package com.zhaidou;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Environment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.zhaidou.utils.ToolUtils;

import java.io.File;

/**
 * Created by wangclark on 15/7/2.
 */
public class ZDApplication extends Application{

    public static int localVersionCode;
    public static String localVersionName;

    public static RequestQueue mRequestQueue;

    private Typeface mTypeFace;
    @Override
    public void onCreate() {
        super.onCreate();

//        CrashReport.initCrashReport(this, "900008762", false);
        initTypeFace();
        try
        {
            PackageInfo packageInfo=getApplicationContext().getPackageManager().getPackageInfo(getPackageName(),0);
            localVersionCode=packageInfo.versionCode;
            localVersionName=packageInfo.versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        creatFile();
        setImageLoad();
        mRequestQueue=Volley.newRequestQueue(this);
    }


    /**
     * universal_image_loader基本配置
     */
    private void setImageLoad()
    {
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "zhaidou/image_cache/");
        ImageLoaderConfiguration configuration = new  ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(3)//线程池加载的数量
                .diskCacheFileCount(50)//最大缓存数量
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb sd卡(本地)缓存的最大值
                .diskCache(new UnlimitedDiscCache(cacheDir))//设置缓存路径
//                .memoryCache(new UsingFreqLimitedMemoryCache(2* 1024 * 1024))
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(configuration);
    }

    private void creatFile()
    {
        String f;
        if (ToolUtils.hasSdcard())
        {
            f = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zhaidou/";
        }
        else
        {
            f = getFilesDir().getAbsolutePath() + "/zhaidou/";
        }
        File file = new File(f);
        if (!file.exists())
        {
            file.mkdirs();
        }
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

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
        System.gc();
        super.onLowMemory();
    }
}
