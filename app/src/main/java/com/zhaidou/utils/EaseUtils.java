package com.zhaidou.utils;/**
 * Created by wangclark on 16/3/15.
 */

import android.content.Context;
import android.content.Intent;

import com.zhaidou.easeui.helpdesk.Constant;
import com.zhaidou.easeui.helpdesk.ui.ChatActivity;
import com.zhaidou.model.User;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-03-15
 * Time: 10:03
 * Description:环信工具类
 * FIXME
 */
public class EaseUtils {

    public static void startKeFuActivity(Context context){
        User user = SharedPreferencesUtil.getUser(context);
        Intent intent2 = new Intent(context, ChatActivity.class);
        intent2.putExtra(Constant.EXTRA_USER_ID, "service");
        intent2.putExtra("queueName", "service");
        intent2.putExtra("user",user);
        context.startActivity(intent2);
    }

    public static void startDesignerActivity(Context context){
        User user = SharedPreferencesUtil.getUser(context);
        Intent intent2 = new Intent(context, ChatActivity.class);
        intent2.putExtra(Constant.EXTRA_USER_ID, "designer");
        intent2.putExtra("queueName", "designer");
        intent2.putExtra("user",user);
        context.startActivity(intent2);
    }
}
