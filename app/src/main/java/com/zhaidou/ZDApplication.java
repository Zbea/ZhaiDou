package com.zhaidou;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Environment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.controller.EaseUI;
import com.easemob.easeui.model.EaseNotifier;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.zhaidou.easeui.helpdesk.Constant;
import com.zhaidou.easeui.helpdesk.EaseApplication;
import com.zhaidou.easeui.helpdesk.ui.ChatActivity;
import com.zhaidou.utils.DeviceUtils;
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
                System.out.println("ZDApplication.gotResult------->" + s);
            }
        });

//        CrashReport.initCrashReport(this, "900008762", false);
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
//        initNotify();
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

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
        System.gc();
        super.onLowMemory();
    }

    private void initNotify() {
        EaseUI.getInstance().init(getApplicationContext());
        EaseUI.getInstance().getNotifier().setNotificationInfoProvider(new EaseNotifier.EaseNotificationInfoProvider() {

            @Override
            public String getTitle(EMMessage message) {
                //修改标题,这里使用默认
                return null;
            }

            @Override
            public int getSmallIcon(EMMessage message) {
                //设置小图标，这里为默认
                return 0;
            }

            @Override
            public String getDisplayedText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = EaseCommonUtils.getMessageDigest(message, getApplicationContext());
                if (message.getType() == EMMessage.Type.TXT) {
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                }
                String from = "设计师";
                if (message.getFrom().contentEquals("service")) {
                    from = message.getFrom().replaceFirst("service", "客服");
                } else if (message.getFrom().contentEquals("designer")) {
                    from = message.getFrom().replaceFirst("designer", "设计师");
                }
                return from + ": " + ticker;
            }

            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
//                return null;
                return messageNum + "条未读消息";
            }

            @Override
            public Intent getLaunchIntent(EMMessage message) {
                //设置点击通知栏跳转事件
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                EMMessage.ChatType chatType = message.getChatType();
                if (chatType == EMMessage.ChatType.Chat) { // 单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", Constant.CHATTYPE_SINGLE);
                } else { // 群聊信息
                    // message.getTo()为群聊id
                    intent.putExtra("userId", message.getTo());
                    if (chatType == EMMessage.ChatType.GroupChat) {
                        intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
                    } else {
                        intent.putExtra("chatType", Constant.CHATTYPE_CHATROOM);
                    }

                }
                return intent;
            }
        });
    }
}
