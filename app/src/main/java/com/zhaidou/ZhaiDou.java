package com.zhaidou;

/**
 * Created by dongsheng on 15/4/22.
 */
public class ZhaiDou {
    public static String HOME_PAGE_URL = "http://buy.zhaidou.com/?json=get_category_posts&slug=%E5%AE%B6%E9%A5%B0%E4%BC%98%E9%80%89&status=publish";
    public static String HOME_BASE_URL = "http://buy.zhaidou.com/";
    public static String TAG_BASE_URL = "http://buy.zhaidou.com/?tag=%s&json=1";

    public enum ListType {
        HOME,
        TAG,
    }
}
