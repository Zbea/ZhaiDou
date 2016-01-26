package com.zhaidou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2015-10-12
 * Time: 11:58
 * Description: 获取设备相关信息的工具类
 * FIXME
 */
public class DeviceUtils {
    private static final String TAG = "DeviceUtils";

    /**
     * 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * mac地址
     *
     * @param context
     * @return
     */
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    //获取屏幕的相关属性
    public static DisplayMetrics getDisplayMetrics(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static String getImei(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
    //获取屏幕的宽度
    public static int getScreenWidth(Context context){
        return getDisplayMetrics(context).widthPixels;
    }

    //获取屏幕的高度
    public static int getScreenHeight(Context context){
        return getDisplayMetrics(context).heightPixels;
    }

    //获取屏幕的密度
    public static float getDensity(Context context){
        return getDisplayMetrics(context).density;
    }

    //dp转像素px
    public static int dp2px(Context context, int dp){
        return (int)(dp * getDensity(context));
    }

    //px转dp
    public static int px2dp(Context context, int px){
        return (int)(px/getDensity(context));
    }


    public static final boolean isApkInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
