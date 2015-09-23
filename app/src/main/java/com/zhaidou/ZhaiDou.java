package com.zhaidou;

/**
 * Created by dongsheng on 15/4/22.
 */
public class ZhaiDou {

    public static String HOME_PAGE_URL = "http://buy.zhaidou.com/?json=get_category_posts&slug=%E5%AE%B6%E9%A5%B0%E4%BC%98%E9%80%89&status=publish";
    public static String HOME_BASE_URL = "http://stg.zhaidou.com/";
//    public static String HOME_BASE_URL = "http://www.zhaidou.com/";
//    public static String HOME_BASE_URL = "http://192.168.199.171/";
    public static String TAG_BASE_URL = "http://buy.zhaidou.com/?tag=%s&json=1";

    public static String ApkUrl = HOME_BASE_URL+"api/v1/app_versions?os=2";

    //0元特卖页面：status=0,普通特卖页面：status=1,首页：status=2
    public static String BannerUrl=HOME_BASE_URL+"special_mall/api/sales/sale_banner?status=";
    //APP换量
    public static String settingRecommendAppUrl=HOME_BASE_URL+"api/v1/app_exchanges?sys=2";

    public static String HOT_SEARCH_URL=HOME_BASE_URL+"article/api/articles/hot_search";
    public static String INDEX_CATEGORY_FILTER=HOME_BASE_URL+"article/api/article_categories";
    //拼贴大赛
    public static String COMPETITION_URL="http://www.zhaidou.com/competitions/current?zdclient=ios";

    //天天刮奖
    public static String PRIZE_SCRAPING_URL=HOME_BASE_URL+"lotteries";
    //获取0元特卖数据
    public static String SPECIAL_SALE_URL=HOME_BASE_URL+"special_mall/api/sales/zero_sale";
    //首页分类文章
    public static String HOME_CATEGORY_URL=HOME_BASE_URL+"article/api/articles?page=";
    //首页轮播图
    public static String HOME_BANNER_URL=HOME_BASE_URL+"article/api/article_categories/index_code?code=zt001";

    //文章页面
    public static String ARTICLE_DETAIL_URL=HOME_BASE_URL+"article/articles/";

    //特卖首页
    public static String shopHomeSpecialUrl=HOME_BASE_URL+"special_mall/api/sales/new_sale";
    //特卖列表share
    public static String shopSpecialListShareUrl=HOME_BASE_URL+"mall/list.html?id=";
    //特卖列表
    public static String shopSpecialListUrl=HOME_BASE_URL+"special_mall/api/sales?sale_cate=0";
    //特卖详情
    public static String shopSpecialTadayUrl=HOME_BASE_URL+"special_mall/api/sales/";
    //商品详情share
    public static String goodsDetailsShareUrl=HOME_BASE_URL+"mall/index.html?id=";
    //商品详情
    public static String goodsDetailsUrlUrl=HOME_BASE_URL+"special_mall/api/merchandises/";
    //购物车商品详情
    public static String goodsCartGoodsUrl=HOME_BASE_URL+"special_mall/api/merchandises/cart_merchandises?ids=";
    //购物车商品数量修改接口
    public static String goodsCartEditGoodsUrl=HOME_BASE_URL+"special_mall/api/merchandises/";
    //地址管理接口
    public static String addressManageUrl=HOME_BASE_URL+"special_mall/api/receivers";
    //提交订单接口
    public static String orderCommitUrl=HOME_BASE_URL+"special_mall/api/orders";
    //查看当天是否已经购买了0元特卖商品
    public static String orderCheckOSaleUrl=HOME_BASE_URL+"special_mall/api/orders/order_items_0_status";

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


    //后台统计
    public static String URL_STATISTICS=HOME_BASE_URL+"api/v1/device_tokens";

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


    //订单列表
    public static String URL_ORDER_LIST=HOME_BASE_URL+"special_mall/api/orders";
    //收货地址
    public static String ORDER_RECEIVER_URL=HOME_BASE_URL+"special_mall/api/receivers/";
    //省市区
    public static String ORDER_ADDRESS_URL=HOME_BASE_URL+"special_mall/api/sales/provider";
// 淘宝订单
    public static String URL_TAOBAO_ORDER="https://login.m.taobao.com/login.htm?tpl_redirect_url=https://h5.m.taobao.com/mlapp/olist.html";


    public static String BROADCAST_WXAPI_FILTER="com.zhaidou.wxapi.pay";



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
     *  购物车商品减一
     */
    public static String IntentRefreshCartGoodsSubTag="com.zhaidou.home.refesh.cart.goods.sub";
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
    /**
     * 代付款加一
     */
    public static String IntentRefreshUnPayAddTag="com.zhaidou.home.refesh.unpay.add";
    /**
     * 代付款减一
     */
    public static String IntentRefreshUnPayDesTag="com.zhaidou.home.refesh.unpay.des";
    /**
     * 收藏减一
     */
    public static String IntentRefreshCollectDesTag="com.zhaidou.home.refesh.collect.des";
    /**
     * 代付款减一
     */
    public static String IntentRefreshUnPayTag="com.zhaidou.home.refesh.unpay.count";
    /**
     * 订单支付成功刷新按钮
     */
    public static String IntentRefreshPaySuccessTag="com.zhaidou.home.refesh.pay.success";
    /**
     * 商品详情零元特卖购买后刷新
     */
    public static String IntentRefreshGoodsDetailsTag="com.zhaidou.home.refesh.goods.details";

}
//http://192.168.1.45/article/api/article_categories?catetory_id=13