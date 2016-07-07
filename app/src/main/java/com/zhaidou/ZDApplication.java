package com.zhaidou;

import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.zhaidou.easeui.helpdesk.EaseApplication;
import com.zhaidou.easeui.helpdesk.EaseHelper;
import com.zhaidou.model.User;
import com.zhaidou.utils.DeviceUtils;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.MD5Util;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import java.io.File;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by wangclark on 15/7/2.
 */
public class ZDApplication extends EaseApplication {

    public static int localVersionCode;
    public static String localVersionName;

    public static RequestQueue mRequestQueue;

    private Typeface mTypeFace;

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

        JPushInterface.setAlias(getApplicationContext(), DeviceUtils.getImei(getApplicationContext()), new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> strings) {
            }
        });

        initTypeFace();
        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            localVersionCode = packageInfo.versionCode;
            localVersionName = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        creatFile();
        setImageLoad();
        mRequestQueue = Volley.newRequestQueue(this);
        User user = SharedPreferencesUtil.getUser(this);
        if (user.getId()>0)
            EaseUtils.login(user);
    }

    /**
     * universal_image_loader基本配置
     */
    private void setImageLoad() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "zhaidou/image_cache/");
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(3)//线程池加载的数量
                .diskCacheFileCount(50)//最大缓存数量
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb sd卡(本地)缓存的最大值
                .diskCache(new LimitedAgeDiscCache(cacheDir, 48 * 60 * 60 * 1000))//设置缓存路径
//                .memoryCache(new UsingFreqLimitedMemoryCache(2* 1024 * 1024))
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(configuration);
    }

    private void creatFile() {
        String f;
        if (ToolUtils.hasSdcard()) {
            f = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zhaidou/";
        } else {
            f = getFilesDir().getAbsolutePath() + "/zhaidou/";
        }
        File file = new File(f);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    private void initTypeFace() {
        if (mTypeFace == null) {
            mTypeFace = Typeface.createFromAsset(getAssets(), "FZLTXHK.TTF");
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

    private void loginToEaseServer(final User user){
        EMChatManager.getInstance().login("zhaidou"+user.getId(), MD5Util.MD5Encode("zhaidou" + user.getId() + "Yage2016!").toUpperCase(), new EMCallBack() {
            @Override
            public void onSuccess() {
                // 登陆成功，保存用户名
                EaseHelper.getInstance().setCurrentUserName(user.getNickName());
                EMChatManager.getInstance().loadAllConversations();

//                更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                        user.getNickName());
                if (!updatenick) {
                    Log.e("LoginActivity", "update current user nick fail");
                }
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(final int code, final String message) {
//                Toast.makeText(LoginActivity.this,"登录聊天服务器失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
        System.gc();
        super.onLowMemory();
    }
}
