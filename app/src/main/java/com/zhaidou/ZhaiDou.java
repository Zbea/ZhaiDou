package com.zhaidou;

/**
 * Created by dongsheng on 15/4/22.
 */
public class ZhaiDou {
    public static String HOME_PAGE_URL = "http://buy.zhaidou.com/?json=get_category_posts&slug=%E5%AE%B6%E9%A5%B0%E4%BC%98%E9%80%89&status=publish";
    public static String HOME_BASE_URL = "http://buy.zhaidou.com/";
    public static String TAG_BASE_URL = "http://buy.zhaidou.com/?tag=%s&json=1";

    public static String HOT_SEARCH_URL="http://192.168.199.171/article/api/articles/hot_search";
    public static String INDEX_CATEGORY_FILTER="http://192.168.199.171/article/api/article_categories";

    /**
     * 我的头像保存目录
     */
    public static String MyAvatarDir = "/sdcard/zhaidou/avatar/";

    public enum ListType {
        HOME,
        TAG,
    }
}
//http://192.168.1.45/article/api/article_categories?catetory_id=13