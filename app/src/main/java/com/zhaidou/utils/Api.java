package com.zhaidou.utils;/**
 * Created by wangclark on 16/6/7.
 */

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-06-07
 * Time: 17:08
 * Description:封装网络请求
 * FIXME
 */
public class Api {





    public static void deleteComment(SuccessListener successListener,ErrorListener errorListener){
        if (successListener!=null){
            successListener.onSuccess(new Object());
        }
    }


    public interface SuccessListener{
        public void onSuccess(Object object);
    }
    public interface ErrorListener{
        public void onError(Object object);
    }
}
