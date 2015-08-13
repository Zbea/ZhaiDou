package com.zhaidou;

/**
 * Created by dongsheng on 15/4/22.
 */
public class ZhaiDou {

    public static String apkUrl = "http://192.168.199.173/zhaidou.apk";
    public static String apkUpdateUrl = "http://192.168.199.173/versionmanage.json";

    public static String HOME_PAGE_URL = "http://buy.zhaidou.com/?json=get_category_posts&slug=%E5%AE%B6%E9%A5%B0%E4%BC%98%E9%80%89&status=publish";
    public static String HOME_BASE_URL = "http://stg.zhaidou.com/";
//    public static String HOME_BASE_URL = "http://192.168.199.173/";
    public static String TAG_BASE_URL = "http://buy.zhaidou.com/?tag=%s&json=1";

    public static String HOT_SEARCH_URL=HOME_BASE_URL+"article/api/articles/hot_search";
    public static String INDEX_CATEGORY_FILTER=HOME_BASE_URL+"article/api/article_categories";

    //拼贴大赛
    public static String COMPETITION_URL="http://www.zhaidou.com/competitions/current?zdclient=ios";

    //获取优惠卷URL
    public static String COUPON_DATA_URL=HOME_BASE_URL+"api/v1/coupons/current";
    //天天刮奖  "http://192.168.199.230:3000/lotteries";//
    public static String PRIZE_SCRAPING_URL=HOME_BASE_URL+"lotteries";
    //获取0元特卖数据
//    public static String SPECIAL_SALE_URL=HOME_BASE_URL+"api/v1/events/current";
    public static String SPECIAL_SALE_URL="http://192.168.199.173/special_mall/api/sales/zero_sale";
    //0元特卖banner
    public static String SPECIAL_SALE_BANNER_URL="http://192.168.199.173/special_mall/api/sales/sale_banner?status=0";

    //首页分类文章
    public static String HOME_CATEGORY_URL=HOME_BASE_URL+"article/api/articles?page=";
    //首页轮播图
    public static String HOME_BANNER_URL=HOME_BASE_URL+"article/api/article_categories/index_code?code=zt001";

    //文章页面
    public static String ARTICLE_DETAIL_URL=HOME_BASE_URL+"article/articles/";

    //特卖首页
    public static String shopHomeSpecialUrl="http://192.168.199.173/special_mall/api/sales/new_sale";
    //特卖banner
    public static String shopSpecialBannerUrl="http://192.168.199.173/special_mall/api/sales/sale_banner?status=";
    //特卖列表share
    public static String shopSpecialListShareUrl="http://192.168.199.230:9000/mall/list.html?id=";
    //特卖列表
    public static String shopSpecialListUrl="http://192.168.199.173/special_mall/api/sales?sale_cate=0";
    //特卖详情
    public static String shopSpecialTadayUrl="http://192.168.199.173/special_mall/api/sales/";
    //商品详情share
    public static String goodsDetailsShareUrl="http://192.168.199.230:9000/mall/index.html?id=";
    //商品详情
    public static String goodsDetailsUrlUrl="http://192.168.199.173/special_mall/api/merchandises/";
    //购物车商品详情
    public static String goodsCartGoodsUrl="http://192.168.199.173/special_mall/api/merchandises/cart_merchandises?ids=";
    //购物车商品数量修改接口
    public static String goodsCartEditGoodsUrl="http://192.168.199.173/special_mall/api/merchandises/";
    //地址管理接口
    public static String addressManageUrl="http://192.168.199.173/special_mall/api/receivers";
    //提交订单接口
    public static String orderCommitUrl="http://192.168.199.173/special_mall/api/orders";
    //查看当天是否已经购买了0元特卖商品
    public static String orderCheckOSaleUrl="http://192.168.199.173/special_mall/api/orders/order_items_0_status";


    //TAB分类
    public static String CATEGORY_ITEM_URL=HOME_BASE_URL+"article/api/item_categories";

    //用户简单信息
    public static String USER_SIMPLE_PROFILE_URL=HOME_BASE_URL+"api/v1/users/";
    //用户编辑信息
    public static String USER_EDIT_PROFILE_URL=HOME_BASE_URL+"api/v1/profiles/";
    //用户退出登录
    public static String USER_LOGOUT_URL=HOME_BASE_URL+"api/v1/user_tokens/logout";
    //用户登录
    public static String USER_LOGIN_URL=HOME_BASE_URL+"api/v1/user_tokens";
    //第三方登录--验证
    public static String USER_LOGIN_THIRD_VERIFY_URL=HOME_BASE_URL+"api/v1/users/verification_other";
    //用户注册
    public static String USER_REGISTER_URL=HOME_BASE_URL+"api/v1/users";
    //用户收藏
    public static String USER_COLLECT_ITEM_URL=HOME_BASE_URL+"article/api/article_items/like_article_items?per_page=10&page=";
    //用户取消收藏
    public static String USER_DELETE_COLLECT_ITEM_URL=HOME_BASE_URL+"article/api/article_items/like";
    //用户豆搭
    public static String USER_COLLOCATION_ITEM_URL=HOME_BASE_URL+"api/v1/users/";

    //搜索单品列表
    public static String SEARCH_PRODUCT_URL=HOME_BASE_URL+"article/api/article_items/search";
    //
    public static String ARTICLE_ITEM_WITH_CATEGORY=HOME_BASE_URL+"article/api/article_items?item_catetory_id=";

    public static String SEARCH_ARTICLES_URL=HOME_BASE_URL+"article/api/articles/search";

    public static String ARTICLES_WITH_CATEGORY=HOME_BASE_URL+"article/api/articles?catetory_id=";

    public static String FORWARD_URL=HOME_BASE_URL+"retweet";
    /**
     * 我的头像保存目录
     */
    public static String MyAvatarDir = "/sdcard/zhaidou/avatar/";

    public enum ListType {
        HOME,
        TAG,
    }

    /** 未付款*/
    public final static int STATUS_UNPAY=0;
    /**已付款*/
    public final static int STATUS_PAYED=1;
    /** 超时过期*/
    public final static int STATUS_OVER_TIME=2;
    /**已取消（已付款）*/
    public final static int STATUS_ORDER_CANCEL_PAYED=3;
    /**已发货*/
    public final static int STATUS_DELIVERY=4;
    /**交易成功*/
    public final static int STATUS_DEAL_SUCCESS=5;
    /**申请退货*/
    public final static int STATUS_APPLY_GOOD_RETURN=6;
    /**退货中*/
    public final static int STATUS_GOOD_RETURNING=7;
    /**退货成功*/
    public final static int STATUS_RETURN_GOOD_SUCCESS=8;
    /**未付款取消*/
    public final static int STATUS_UNPAY_CANCEL=9;
    /**交易关闭*/
    public final static int STATUS_DEAL_CLOSE=10;
    /**退款成功*/
    public final static int STATUS_RETURN_MONEY_SUCCESS=11;


    //广播标识集合
    /**
     * 首页点击标识刷新
     */
    public static String IntentRefreshListTag="com.zhaidou.home.refesh.list";
    /**
     *  购物车商品刷新
     */
    public static String IntentRefreshCartGoodsTag="com.zhaidou.home.refesh.cart.goods";
    /**
     * 购物车选中商品刷新
     */
    public static String IntentRefreshCartGoodsCheckTag="com.zhaidou.home.refesh.cart.goods.check";
    /**
     * 登录成功
     */
    public static String IntentRefreshLoginTag="com.zhaidou.home.refesh.login.success";
    /**
     * 登录退出
     */
    public static String IntentRefreshLoginExitTag="com.zhaidou.home.refesh.login.exit";



}
//http://192.168.1.45/article/api/article_categories?catetory_id=13