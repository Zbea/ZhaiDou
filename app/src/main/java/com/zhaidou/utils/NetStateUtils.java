package com.zhaidou.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


/**
 * Created by roy on 15/7/13.
 */
public class NetStateUtils extends BroadcastReceiver
{

    private boolean isNetState;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        NetGetState(context, intent);
    }

    private void NetGetState(Context context, Intent intent)
    {
        NetworkInfo networkInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.CONNECTIVITY_ACTION);
        if (networkInfo.isConnected()&&networkInfo.isAvailable())
        {
            switch(networkInfo.getType())
            {
                case ConnectivityManager.TYPE_WIFI:
                    isNetState=true;
                    Log.i("zhaidou","wifi链接");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    isNetState=true;
                    Log.i("zhaidou","mobile链接");
                    break;
                default:
                    isNetState=false;
                    break;
            }
        }
        else
        {
            isNetState=false;
        }


    }
}
