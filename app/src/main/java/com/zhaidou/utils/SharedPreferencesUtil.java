package com.zhaidou.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.zhaidou.model.User;

import java.util.List;

/**
 * Created by wangclark on 15/7/3.
 */
public class SharedPreferencesUtil {
    private static final String SPName="zhaidou";


    public static void saveData(Context context, String key,Object data){

        String type = data.getClass().getSimpleName();
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if ("Integer".equals(type)){
            editor.putInt(key, (Integer)data);
        }else if ("Boolean".equals(type)){
            editor.putBoolean(key, (Boolean)data);
        }else if ("String".equals(type)){
            editor.putString(key, (String)data);
        }else if ("Float".equals(type)){
            editor.putFloat(key, (Float)data);
        }else if ("Long".equals(type)){
            editor.putLong(key, (Long)data);
        }

        editor.commit();
    }

        public static Object getData(Context context, String key, Object defValue){

        String type = defValue.getClass().getSimpleName();
        SharedPreferences sharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        //defValue为为默认值，如果当前获取不到数据就返回它
        if ("Integer".equals(type)){
            return sharedPreferences.getInt(key, (Integer)defValue);
        }else if ("Boolean".equals(type)){
            return sharedPreferences.getBoolean(key, (Boolean)defValue);
        }else if ("String".equals(type)){
            return sharedPreferences.getString(key, (String)defValue);
        }else if ("Float".equals(type)){
            return sharedPreferences.getFloat(key, (Float)defValue);
        }else if ("Long".equals(type)){
            return sharedPreferences.getLong(key, (Long)defValue);
        }
        return null;
    }

    public static void saveUser(Context context,User user){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("userId",user.getId());
        editor.putString("email", user.getEmail());
        editor.putString("token",user.getAuthentication_token());
        editor.putString("avatar",user.getAvatar());
        editor.putString("nickName",user.getNickName());
        editor.commit();
    }
    public static User getUser(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        int userId = (Integer)SharedPreferencesUtil.getData(context, "userId", -1);
        String email = (String) SharedPreferencesUtil.getData(context, "email", "");
        String token = (String) SharedPreferencesUtil.getData(context, "token", "");
        String avatar = (String) SharedPreferencesUtil.getData(context, "avatar", "");
        String nickName = (String) SharedPreferencesUtil.getData(context, "nickName", "");
        String mobile = (String) SharedPreferencesUtil.getData(context, "mobile", "");
        String description = (String) SharedPreferencesUtil.getData(context, "description", "");
        User user=new User(userId,email,token,nickName,avatar);
        user.setMobile(mobile);
        user.setDescription(description);
        return user;
    }

    public static void clearUser(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("userId",-1);
        editor.putString("email", "");
        editor.putString("token","");
        editor.putString("avatar","");
        editor.putString("nickName","");
        editor.commit();
    }

    public static void saveHistoryData(Context context,List<String> historyList){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("historyCount",historyList.size());
        for (int i=0;i<historyList.size();i++){
            editor.putString("history_"+i,historyList.get(i));
        }
        editor.commit();
    }

    public static void clearSearchHistory(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        int count=mSharedPreferences.getInt("historyCount",0);
        for (int i=0;i<count;i++){
            editor.remove("history_"+i);
        }
        editor.putInt("historyCount",0);
        editor.commit();
    }
}
