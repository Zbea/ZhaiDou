package com.zhaidou.utils;/**
 * Created by wangclark on 16/3/15.
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.easeui.helpdesk.Constant;
import com.zhaidou.easeui.helpdesk.EaseHelper;
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
        if ((Integer)SharedPreferencesUtil.getData(context, "userId", -1)==-1){
            Intent intent=new Intent(context,LoginActivity.class);
            context.startActivity(intent);
            return;
        }
        Intent intent2 = new Intent(context, ChatActivity.class);
        intent2.putExtra(Constant.EXTRA_USER_ID, "service");
        intent2.putExtra("queueName", "service");
        intent2.putExtra("user",user);
        context.startActivity(intent2);
    }

    public static void startDesignerActivity(Context context){
        User user = SharedPreferencesUtil.getUser(context);
        if ((Integer)SharedPreferencesUtil.getData(context, "userId", -1)==-1){
            Intent intent=new Intent(context,LoginActivity.class);
            context.startActivity(intent);
            return;
        }
        Intent intent2 = new Intent(context, ChatActivity.class);
        intent2.putExtra(Constant.EXTRA_USER_ID, "designer");
        intent2.putExtra("queueName", "designer");
        intent2.putExtra("user",user);
        context.startActivity(intent2);
    }

    public static void login(final User user){
        EMChatManager.getInstance().login("zhaidou"+user.getId(), MD5Util.MD5Encode("zhaidou" + user.getId() + "Yage2016!").toUpperCase(), new EMCallBack() {
            @Override
            public void onSuccess() {
                EaseHelper.getInstance().setCurrentUserName(user.getNickName());
                EMChatManager.getInstance().loadAllConversations();
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
            }
        });
    }

}
