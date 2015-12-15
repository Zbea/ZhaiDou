package com.zhaidou;

/**
 * Created by dongsheng on 15/4/22.
 */
public class ZhaiDou {

    public static String HOME_PAGE_URL = "http://buy.zhaidou.com/?json=get_category_posts&slug=%E5%AE%B6%E9%A5%B0%E4%BC%98%E9%80%89&status=publish";
//    public static String HOME_BASE_URL = "http://stg.zhaidou.com/";
//        public static String HOME_BASE_URL = "http://www.zhaidou.com/";
    public static String HOME_BASE_URL = "http://121.42.206.45:7082/";

    //首页banner
    public static String HomeBannerUrl="http://121.42.206.45:7082/index/getBoardContent.action?boardCodes=";
    //首页特卖列表
    public static String HomeShopListUrl="http://121.42.206.45:7082/index/getSpecialSaleList.action?pageSize=20&pageNo=";
    //首页特卖商品列表
    public static String HomeGoodsListUrl="http://121.42.206.45:7082/index/getActivityProductList.action?activityCode=";
    //商品加入购物车
    public static String GoodsDetailsAddUrl="http://121.42.206.45:7082/cart/addUserCart.action?businessType=01&version=1.0.0&clientType=app&quantity=1&userId=1&productSKUId=";
    //商品详情接口
    public static String HomeGoodsDetailsUrl="http://121.42.206.45:7082/product/getProductInfo.action?businessType=01&version=1.0.0&productId=";
    //判断是否是0元特卖商品是否已经购买了
    public static String  IsBuyOSaleUrl="http://121.42.206.45:7082/product/checkProduct.action?version=1.0.0&userId=";
    //购物车数量接口
    public static String CartGoodsCountUrl="http://121.42.206.45:7082/cart/countUserCartQuantity.action?businessType=01&version=1.0.0&userId=1";
    //购物车列表接口
    public static String CartGoodsListUrl="http://121.42.206.45:7082/cart/queryUserCart.action?businessType=01&version=1.0.0&clientType=app&userId=1";
    //删除购物车接口
    public static String CartGoodsDeleteUrl="http://121.42.206.45:7082/cart/deleteUserCart.action?businessType=01&version=1.0.0&userId=1&productSKUId=";
    //修改购物车商品详情接口
    public static String CartGoodsEditUrl="http://121.42.206.45:7082/cart/editUserCart.action?businessType=01&version=1.0.0&clientType=app&userId=1&quantity=";
    //搜索商品接口
    public static String SearchGoodsUrl="http://121.42.206.45:7082/product/searchProduct.action?businessType=01&searchType=10&pageSize=20&keyword=";
    //热搜词
    public static String SearchHotUrl="http://121.42.206.45:7082/product/getHotKeywordList.action";
    //提交订单接口
    public static String CommitOrdersUrl="http://121.42.206.45:7082/order/addMallOrder.action";
    //获取支付方式
    public static String CommitPaymentGetCodeUrl="http://121.42.206.45:7082/pay/queryThirdpartyPayType.action";
    //支付
    public static String CommitPaymentUrl="http://121.42.206.45:7082/pay/payConfirm.action";
    //获取订单详情
    public static String GetOrderDetailsUrl="http://121.42.206.45:7082/order/getOrderDetail.action";

    //地址列表
    public static String AddressListUrl="http://121.42.206.45:7082/user/receivers.action";
    //设为默认地址
    public static String AddressIsDefultUrl="http://121.42.206.45:7082/user/receivers_default.action";
    //新建地址
    public static String AddressNewUrl="http://121.42.206.45:7082/user/receivers_add.action";
    //删除地址
    public static String AddressDeleteUrl="http://121.42.206.45:7082/user/receivers_delete.action";
    //编辑地址
    public static String AddressEditUrl="http://121.42.206.45:7082/user/receivers_update.action";
    //省市区
    public static String ORDER_ADDRESS_URL="http://121.42.206.45:7082/user/provider.action";



    //首页三个专题tag
    public static String HOME_SPECIAL_BANNER_URL=HOME_BASE_URL+"special_mall/api/sales/topic_sales?topic=true";

    public static String ApkUrl = "http://121.42.206.45:7082/user/app_versions.action?os=2";
    //0元特卖页面：status=0,普通特卖页面：status=1,首页：status=2
    public static String BannerUrl=HOME_BASE_URL+"special_mall/api/sales/sale_banner?status=";
    //APP换量
    public static String settingRecommendAppUrl="http://121.42.206.45:7082/user/app_exchanges.action?sys=2";

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
    //查看当天是否已经购买了普通特卖商品
    public static String orderCheckGoodsUrl=HOME_BASE_URL+"special_mall/api/merchandises/";

    //确认订单获取短信接口
    public static String OrderGetSMS = "http://portal-web.zhaidou.com/user/vilidate_phone.action?flag=1&phone=";
    //判断手机是否需要验证接口
    public static String OrderAccountOrPhone = "http://121.42.206.45:7082/user/validation_phone.action";
    //提交手机验证接口
    public static String OrderBlindPhone = "http://121.42.206.45:7082/user/user_blinding_phone.action";

    //TAB分类
    public static String CATEGORY_ITEM_URL=HOME_BASE_URL+"article/api/item_categories";

    //用户简单信息
    public static String USER_SIMPLE_PROFILE_URL=HOME_BASE_URL+"api/v1/users/";
    //用户简单信息INFO
    public static String USER_SIMPLE_INFO_URL=HOME_BASE_URL+"/user/queryUserInfo.action";
    //用户编辑信息
    public static String USER_EDIT_PROFILE_URL=HOME_BASE_URL+"api/v1/profiles/";
    //用户退出登录
    public static String USER_LOGOUT_URL=HOME_BASE_URL+"api/v1/user_tokens/logout";
    //用户登录
    public static String USER_LOGIN_URL=HOME_BASE_URL+"user/user_tokens.action";
    //第三方登录--验证
    public static String USER_LOGIN_THIRD_VERIFY_URL=HOME_BASE_URL+"user/verification_other.action";
    //用户注册
    public static String USER_REGISTER_URL=HOME_BASE_URL+"api/v1/users";
    //
    public static String USER_REGISTER_WITH_PHONE_URL=HOME_BASE_URL+"user/phone_register.action";
    //用户收藏
    public static String USER_COLLECT_ITEM_URL=HOME_BASE_URL+"article/api/article_items/like_article_items?per_page=10&page=";
    //用户取消收藏
    public static String USER_DELETE_COLLECT_ITEM_URL=HOME_BASE_URL+"article/api/article_items/like";
    //用户豆搭
    public static String USER_COLLOCATION_ITEM_URL=HOME_BASE_URL+"api/v1/users/";
    //获取验证码
    public static String USER_REGISTER_VERIFY_CODE_URL=HOME_BASE_URL+"user/vilidate_phone.action";
    //修改密码验证码验证
    public static String USER_RESET_PSW_CONFRIM_URL=HOME_BASE_URL+"user/next_to_password.action?phone=";
    //修改密码
    public static String USER_RESET_PSW_URL=HOME_BASE_URL+"user/reset_password.action";
    //注册第一步判断手机是否已经注册
    public static String USER_REGISTER_CHECK_PHONE_URL=HOME_BASE_URL+"user/next_to_register.action";
    //登陆用户进行绑定手机接口
    public static String USER_LOGIN_BINE_PHONE_URL=HOME_BASE_URL+"user/user_blinding_phone.action";


    //后台统计
    public static String URL_STATISTICS=HOME_BASE_URL+"api/v1/device_tokens";

    //搜索单品列表
    public static String SEARCH_PRODUCT_URL=HOME_BASE_URL+"article/api/article_items/search";
    //搜索特卖商城商品列表
    public static String SEARCH_SPECACIAL_PRODUCT_URL=HOME_BASE_URL+"special_mall/api/merchandises/search";
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

    /**
     * 短信验证时间
     */
    public final static int VERFIRY_TIME=60;

    /** 待付款*/
    public final static int STATUS_UNPAY=10;
    /** 部分付款*/
    public final static int STATUS_PARY_PAY=11;
    /** 待审核*/
    public final static int STATUS_UNCHECK=20;
    /**待发货*/
    public final static int STATUS_UNDELIVERY=30;
    /**部分发货*/
    public final static int STATUS_PART_DELIVERY=31;
    /**已发货*/
    public final static int STATUS__DELIVERYED=40;
    /**交易成功*/
    public final static int STATUS_DEAL_SUCCESS=50;
    /**申请取消*/
    public final static int STATUS_ORDER_APPLY_CANCEL=-10;
    /**已取消*/
    public final static int STATUS_ORDER_CANCEL=-20;

    /**已付款*/
    public final static int STATUS_PAYED=1;
    /** 超时过期*/
    public final static int STATUS_OVER_TIME=2;
    /**已取消（已付款）*/
    public final static int STATUS_ORDER_CANCEL_PAYED=3;
    /**已发货*/
    public final static int STATUS_DELIVERY=40;

    /**申请退货*/
    public final static int STATUS_APPLY_GOOD_RETURN=6;
    /**退货中*/
    public final static int STATUS_GOOD_RETURNING=7;
    /**退货成功*/
    public final static int STATUS_RETURN_GOOD_SUCCESS=8;
    /**未付款取消*/
    public final static int STATUS_UNPAY_CANCEL=9;
    /**交易关闭*/
    public final static int STATUS_DEAL_CLOSE=100;
    /**退款成功*/
    public final static int STATUS_RETURN_MONEY_SUCCESS=11;

    /** 订单类型 0：全部 1待付款  2待发货  3待收货 4交易完成 5已取消  */
    public final static String TYPE_ORDER_ALL="0";
    public final static String TYPE_ORDER_PREPAY="1";
    public final static String TYPE_ORDER_PREDELIVERY="2";
    public final static String TYPE_ORDER_SUCCESS="4";
    public final static String TYPE_ORDER_CANCEL="5";


    //订单列表
    public static String URL_ORDER_LIST=HOME_BASE_URL+"order/orderList.action";
    //取消订单
    public static String URL_ORDER_CANCEL=HOME_BASE_URL+"order/cancelOrder.action";
    //申请取消订单
    public static String URL_ORDER_APPLY_CANCEL=HOME_BASE_URL+"order/applyCancelOrder.action";
    //删除订单
    public static String URL_ORDER_DELETE=HOME_BASE_URL+"order/deleteOrder.action";
    //确认收货
    public static String URL_ORDER_CONFIRM=HOME_BASE_URL+"order/confirmReceived.action";
    //订单详情
    public static String URL_ORDER_DETAIL=HOME_BASE_URL+"order/getOrderDetail.action";

    // 淘宝订单
    public static String URL_TAOBAO_ORDER="https://login.m.taobao.com/login.htm?tpl_redirect_url=https://h5.m.taobao.com/mlapp/olist.html";


    public static String BROADCAST_WXAPI_FILTER="com.zhaidou.wxapi.pay";



    //广播标识集合
    /**
     *  购物车选中商品刷新
     */
    public static String IntentRefreshCartGoodsCheckTag ="com.zhaidou.home.refesh.cart.goods.check";
    /**
     *  加入购物车成功
     */
    public static String IntentRefreshAddCartTag="com.zhaidou.home.refesh.goods.add.cart";
    /**
     * 购物车商品刷新
     */
    public static String IntentRefreshCartGoodsTag ="com.zhaidou.home.refesh.cart.goods";
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
     * 商品详情零元特卖购买后刷新
     */
    public static String IntentRefreshOGoodsDetailsTag="com.zhaidou.home.refesh.goods.details";
    /**
     * 商品详情普通特卖购买后刷新
     */
    public static String IntentRefreshGoodsDetailsTag="com.zhaidou.home.refesh.o.goods.details";
    public static String TESTUSERID="16665";//28129//16665//64410
    public static String TESTTYPE="2";
}