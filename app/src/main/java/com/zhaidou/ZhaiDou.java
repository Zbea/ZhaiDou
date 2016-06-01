package com.zhaidou;

/**
 * Created by dongsheng on 15/4/22.
 */
public class ZhaiDou {

    public static String HOME_PAGE_URL = "http://buy.zhaidou.com/?json=get_category_posts&slug=%E5%AE%B6%E9%A5%B0%E4%BC%98%E9%80%89&status=publish";
    public static String HOME_BASE_URL = "http://tportal-web.zhaidou.com/";
    public static String HOME_BASE="http://stg.zhaidou.com/";

//    public static String HOME_BASE_URL = "http://portal-web.zhaidou.com/";
//    public static String HOME_BASE="http://www.zhaidou.com/";

    //软装指南
    public static String MagicGuideUrl="http://m.zhaidou.com/rzzh/list.html";
    //美丽家
    public static String HomeBeautifulUrl=HOME_BASE_URL+"zd/getPosts.action?plug=006&pageSize=10&pageNo=";
    //分类
    public static String HomeCategoryUrl=HOME_BASE_URL+"category/queryCategory.action";
    //模块接口
    public static String HomeBannerUrl=HOME_BASE_URL+"index/getBoardContent.action?boardCodes=";
    //首页文章列表
    public static String HomeArticleGoodsUrl=HOME_BASE_URL+"decorate/getChangeCases.action?pageSize=20&pageNo=";
    //首页文章详情
    public static String HomeArticleGoodsDetailsUrl=HOME_BASE_URL+"decorate/getChangeCaseDetail.action?caseId=";
    //首页banner点击统计接口
    public static String HomeClickStatisticalUrl=HOME_BASE_URL+"api/countBannerClick.action?name=";
    //首页微信文章列表
    public static String HomeWeixinListUrl=HOME_BASE_URL+"zd/getArticles.action?pageSize=10&pageNo=";
    //首页特卖列表
    public static String HomeShopListUrl=HOME_BASE_URL+"index/getSpecialSaleList.action?pageSize=10&pageNo=";
    //首页特卖商品列表
    public static String HomeGoodsListUrl=HOME_BASE_URL+"index/getActivityProductList.action?activityCode=";

    //免费经典方案
    public static String MagicClassicCaseUrl=HOME_BASE_URL+"decorate/getFreeClassicsCases.action?pageNo=1&pageSize=50";
    //免费经典方案详情
    public static String MagicClassicCaseDetailsUrl=HOME_BASE_URL+"decorate/getFreeClassicsCaseDetail.action?caseId=";
    //图列风格
    public static String MagicImageCaseUrl=HOME_BASE_URL+"decorate/getSoftDecorateStyles.action";
    //图列颜色分类
    public static String MagicImageClassUrl=HOME_BASE_URL+"decorate/getSoftDecorateCases.action?styleId=";
    //图列套图
    public static String MagicCImageDetailsUrl=HOME_BASE_URL+"decorate/getSoftDecorateImages.action?caseId=";

    //特卖列表share
    public static String shopSpecialListShareUrl="http://m.zhaidou.com/mall_list.html?id=";
    //商品加入购物车
    public static String GoodsDetailsAddUrl=HOME_BASE_URL+"cart/addUserCart.action?businessType=01&version=1.0.0&clientType=app&quantity=1&userId=";
    //商品详情share
    public static String goodsDetailsShareUrl="http://m.zhaidou.com/mall_content.html?id=";
    //商品详情接口
    public static String HomeGoodsDetailsUrl=HOME_BASE_URL+"product/getProductInfo.action?businessType=01&version=1.0.0&productId=";
    //判断是否是0元特卖商品是否已经购买了
    public static String  IsBuyOSaleUrl=HOME_BASE_URL+"product/checkProduct.action?version=1.0.0&userId=";
    //判断是否是0元特卖商品是否已经加入购物车
    public static String  IsAddOSaleUrl=HOME_BASE_URL+"cart/queryZeroCart.action?businessType=01&version=1.0.0&clientType=app&userId=";
    //购物车数量接口
    public static String CartGoodsCountUrl=HOME_BASE_URL+"cart/countUserCartQuantity.action?businessType=01&version=1.0.0&userId=";
    //购物车列表接口
    public static String CartGoodsListUrl=HOME_BASE_URL+"cart/queryUserCart.action?businessType=01&version=1.0.0&clientType=app&userId=";
    //删除购物车接口
    public static String CartGoodsDeleteUrl=HOME_BASE_URL+"cart/deleteUserCart.action?businessType=01&version=1.0.0&userId=";
    //修改购物车商品详情接口
    public static String CartGoodsEditUrl=HOME_BASE_URL+"cart/editUserCart.action?businessType=01&version=1.0.0&clientType=app&userId=";
    //搜索商品接口keyword
    public static String SearchGoodsKeyWordUrl=HOME_BASE_URL+"product/searchProduct.action?businessType=01&searchType=10&pageSize=10&keyword=";
    //搜索商品接口Id
    public static String SearchGoodsIdUrl=HOME_BASE_URL+"product/searchProduct.action?businessType=01&searchType=20&pageSize=10&expandedRequest=";
    //热搜词
    public static String SearchHotUrl=HOME_BASE_URL+"product/getHotKeywordList.action";
    //提交订单接口
    public static String CommitOrdersUrl=HOME_BASE_URL+"order/addMallOrder.action";
    //获取支付方式
    public static String CommitPaymentGetCodeUrl=HOME_BASE_URL+"pay/queryThirdpartyPayType.action";
    //支付
    public static String CommitPaymentUrl=HOME_BASE_URL+"pay/payConfirm.action";
    //获取订单详情
    public static String GetOrderDetailsUrl=HOME_BASE_URL+"order/getOrderDetail.action";
    //退货详情
    public static String ORDER_RETURN_DETAIL=HOME_BASE_URL+"order/getEveryProductDiscountAmount.action";

    //获取可使用优惠劵列表:
    public static String GetOrderCouponUrl=HOME_BASE_URL+"user/getEnableCoupons.action";
    //获取默认优惠劵:
    public static String GetOrderCouponDefaultUrl=HOME_BASE_URL+"user/getEnableCoupons_default.action";
    //获取我的优惠卷
    public static String COUPONS_MINE_URL=HOME_BASE_URL+"user/get_my_coupons.action";
    //兑换优惠劵:
    public static String GetRedeemCouponUrl=HOME_BASE_URL+"user/activateCoupons.action";
    //兑换优惠劵和校验是否可用（在下单时候使用）
    public static String GetRedeemAndCheckCouponUrl=HOME_BASE_URL+"user/activateAndCheckCoupons.action";

    //地址列表
    public static String AddressListUrl=HOME_BASE_URL+"user/receivers.action";
    //设为默认地址
    public static String AddressIsDefultUrl=HOME_BASE_URL+"user/receivers_default.action";
    //新建地址
    public static String AddressNewUrl=HOME_BASE_URL+"user/receivers_add.action";
    //删除地址
    public static String AddressDeleteUrl=HOME_BASE_URL+"user/receivers_delete.action";
    //编辑地址
    public static String AddressEditUrl=HOME_BASE_URL+"user/receivers_update.action";
    //省市区
    public static String ORDER_ADDRESS_URL=HOME_BASE_URL+"user/provider.action";

    public static String ApkUrl = HOME_BASE_URL+"user/app_versions.action?os=2";
    //APP换量
    public static String settingRecommendAppUrl=HOME_BASE_URL+"user/app_exchanges.action?sys=2";
    public static String INDEX_CATEGORY_FILTER=HOME_BASE_URL+"article/api/article_categories";
    //拼贴大赛
    public static String COMPETITION_URL="http://www.zhaidou.com/competitions/current?zdclient=ios";
    //首页分类文章
    public static String HOME_CATEGORY_URL="http://www.zhaidou.com/article/api/articles?page=";
    //文章页面
    public static String ARTICLE_DETAIL_URL="http://www.zhaidou.com/article/articles/";

    //确认订单获取短信接口
    public static String OrderGetSMS = HOME_BASE_URL+"user/vilidate_phone.action?flag=1&phone=";
    //判断手机是否需要验证接口
    public static String OrderAccountOrPhone = HOME_BASE_URL+"user/validation_phone.action";
    //提交手机验证接口
    public static String OrderBlindPhone = HOME_BASE_URL+"user/user_blinding_phone.action";

    //TAB分类
    public static String CATEGORY_ITEM_URL=HOME_BASE_URL+"article/api/item_categories";

    //用户简单信息
    public static String USER_SIMPLE_PROFILE_URL=HOME_BASE_URL+"user/get_users.action";
    //用户简单信息INFO
    public static String USER_SIMPLE_INFO_URL=HOME_BASE_URL+"user/queryUserInfo.action";
    //获取用户详细信息
    public static String USER_DETAIL_PROFILE_URL=HOME_BASE_URL+"user/get_users_profile.action";
    //修改用户信息
    public static String USER_EDIT_PROFILE_URL=HOME_BASE_URL+"user/profiles.action";
    //用户退出登录
    public static String USER_LOGOUT_URL=HOME_BASE_URL+"user/login_out.action";
    //用户登录
    public static String USER_LOGIN_URL=HOME_BASE_URL+"user/user_tokens.action";
    //第三方登录--验证
    public static String USER_LOGIN_THIRD_VERIFY_URL=HOME_BASE_URL+"user/verification_other.action";
    //用户注册
    public static String USER_REGISTER_URL=HOME_BASE_URL+"user/register_other.action";
    //修改密码
    public static String USER_PSW_CHANGE_URL=HOME_BASE_URL+"user/change_password.action";
    //修改用户头像
    public static String USER_UPDATE_AVATAR_URL=HOME_BASE_URL+"user/update_head_img.action";
    //
    public static String USER_REGISTER_WITH_PHONE_URL=HOME_BASE_URL+"user/phone_register.action";

    //用户收藏
    public static String USER_COLLECT_ITEM_URL=HOME_BASE_URL+"article/api/article_items/like_article_items?per_page=10&page=";
    //用户取消收藏
    public static String USER_DELETE_COLLECT_ITEM_URL=HOME_BASE_URL+"article/api/article_items/like";
    //用户豆搭
    public static String USER_COLLOCATION_ITEM_URL=HOME_BASE+"api/v1/users/";
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
    public static String URL_STATISTICS="http://www.zhaidou.com/api/v1/device_tokens";

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

    /**
     * 评论图片目录
     */
    public static String MyCommentDir = "/sdcard/zhaidou/comment/";

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
    /**已拣货*/
    public final static int STATUS_PICKINGUP=70;
    /**部分发货*/
    public final static int STATUS_PART_DELIVERY=31;
    /**已发货*/
    public final static int STATUS__DELIVERYED=40;
    /**交易成功*/
    public final static int STATUS_DEAL_SUCCESS=50;
    /**退款完成*/
    public final static int STATUS_RETURN_MONEY_SUCCESS=60;
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

    /** 订单类型 0：全部 1待付款  2待发货  3待收货 4交易完成 5已取消  */
    public final static String TYPE_ORDER_ALL="0";
    public final static String TYPE_ORDER_PREPAY="1";
    public final static String TYPE_ORDER_PREDELIVERY="2";
    public final static String TYPE_ORDER_PRERECEIVE="3";
    public final static String TYPE_ORDER_SUCCESS="4";
    public final static String TYPE_ORDER_CANCEL="5";


    //订单列表
    public static String URL_ORDER_LIST=HOME_BASE_URL+"order/orderList.action";
    //订单详情列表
    public static String URL_ORDER_DETAIL_LIST_URL=HOME_BASE_URL+"order/getOrderDetailList.action";
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
    //退换货申请
    public static String URL_ORDER_RETURN_APPLY=HOME_BASE_URL+"afterSeller/applyReturnFlow.action";
    //退换货列表
    public static String URL_ORDER_RETURN_LIST=HOME_BASE_URL+"afterSeller/getReturnFlowList.action";
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
     * 购物车商品支付成功
     */
    public static String IntentRefreshCartPaySuccessTag ="com.zhaidou.home.refesh.cart.pay.success";
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
     * 商品详情零元特卖购买后刷新
     */
    public static String IntentRefreshOGoodsDetailsTag="com.zhaidou.home.refesh.goods.details";
    /**
     * 商品详情普通特卖购买后刷新
     */
    public static String IntentRefreshGoodsDetailsTag="com.zhaidou.home.refesh.o.goods.details";
    public static String TESTUSERID="28325";//28129//16665//64410//28325
    public static String TESTTYPE="2";

}