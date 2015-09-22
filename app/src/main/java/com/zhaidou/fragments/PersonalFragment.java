package com.zhaidou.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomePTActivity;
import com.zhaidou.activities.WebViewActivity;
import com.zhaidou.base.BaseFragment;

import com.zhaidou.model.CartItem;
import com.zhaidou.model.User;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalFragment extends BaseFragment implements View.OnClickListener, CollectFragment.CollectCountChangeListener,
        CollocationFragment.CollocationCountChangeListener, SettingFragment.ProfileListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CONTEXT = "context";

    private String mParam1;
    private String mParam2;
    private View view;

    private RequestQueue mRequestQueue;
    private final int UPDATE_USER_INFO = 1;
    private final int UPDATE_USER_DESCRIPTION = 2;
    private final int UPDATE_USER_COLLECT_COUNT = 3;
    private final int UPDATE_USER_COLLOCATION = 4;
    private final int UPDATE_UNPAY_COUNT = 5;
    private final int UPDATE_UNPAY_COUNT_REFRESH = 6;
    private final int UPDATE_CARTCAR_DATA=7;

    private Map<String, String> cityMap = new HashMap<String, String>();

    private int collect_count = 0;
    private int collocation_count = 0;

    private Activity mActivity;

    private User user;
    private int num;
    private ProfileFragment mProfileFragment;
    private CreatCartDB creatCartDB;
    private List<CartItem> items = new ArrayList<CartItem>();
    private ImageView iv_header, mPrePayView, mPreReceivedView, mReturnView;
    private TextView tv_nickname, tv_desc, tv_collect, tv_collocation, tv_unpay_count;
    private RelativeLayout mCouponsView, mSettingView, mAllOrderView;
    private FrameLayout mChildContainer;
    private TextView mCartCount;
    private int userId;
    private String token;
    private int count=0;
    private int collectNum=0;
//    private int num;
    private List<CartItem> cartItems = new ArrayList<CartItem>();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag)) {
                count=0;
                initCartTips();
                exitLoginEvent();
            }

            if (action.equals(ZhaiDou.IntentRefreshUnPayAddTag)) {
                ToolUtils.setLog("开始好刷新count加一");
                count = count + 1;
                tv_unpay_count.setText(count + "");
                tv_unpay_count.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).hideTip(View.VISIBLE);
            }
            if (action.equals(ZhaiDou.IntentRefreshUnPayDesTag)) {
                count = count - 1;
                if (count < 1) {
                    tv_unpay_count.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).hideTip(View.GONE);
                } else {
                    tv_unpay_count.setText(count + "");
                }
            }
            if (action.equals(ZhaiDou.IntentRefreshUnPayTag))
            {
                ToolUtils.setLog("开始好刷新count");
                FetchUnPayCount(UPDATE_UNPAY_COUNT_REFRESH);
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_USER_INFO:
                    User user = (User) msg.obj;
                    ToolUtils.setImageCacheUrl("http://" + user.getAvatar(), iv_header,R.drawable.icon_header_default);
                    if (!TextUtils.isEmpty(user.getNickName()))
                        tv_nickname.setText(user.getNickName());
                    break;
                case UPDATE_USER_DESCRIPTION:
                    User u = (User) msg.obj;
                    tv_desc.setText("null".equalsIgnoreCase(u.getDescription()) || u.getDescription() == null ? "" : u.getDescription());
                    break;
                case UPDATE_USER_COLLECT_COUNT:
                    collectNum=msg.arg1;
                    ToolUtils.setLog("收藏"+msg.arg1);
                    tv_collect.setText(msg.arg1 + "");
                    break;
                case UPDATE_USER_COLLOCATION:
                    ToolUtils.setLog("豆搭："+msg.arg1);
                    tv_collocation.setText(msg.arg1 + "");
                    break;
                case UPDATE_UNPAY_COUNT:
                    count = msg.arg1;
                    ToolUtils.setLog("代付款："+count);
                    tv_unpay_count.setVisibility(View.VISIBLE);
                    ((MainActivity) getActivity()).hideTip(View.VISIBLE);
                    tv_unpay_count.setText(count + "");
                    break;
                case UPDATE_UNPAY_COUNT_REFRESH:
                    count = msg.arg1;
                    ToolUtils.setLog("代付款："+count);
                    tv_unpay_count.setVisibility(View.VISIBLE);
                    tv_unpay_count.setText(count + "");
                    break;
                case UPDATE_CARTCAR_DATA:
                    int visible=msg.arg1;
                    int num=msg.arg2;
                    mCartCount.setVisibility(visible);
                    mCartCount.setText("" + num);
                    break;
            }
        }
    };

    public static PersonalFragment personalFragment;

    public static PersonalFragment newInstance(String param1, String context) {
        if (personalFragment == null)
            personalFragment = new PersonalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_CONTEXT, context);
        personalFragment.setArguments(args);
        return personalFragment;
    }

    public PersonalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_CONTEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.personal, container, false);

            initBroadcastReceiver();

            mPrePayView = (ImageView) view.findViewById(R.id.tv_pre_pay);
            mPreReceivedView = (ImageView) view.findViewById(R.id.tv_pre_received);
            mReturnView = (ImageView) view.findViewById(R.id.tv_return);
            mAllOrderView = (RelativeLayout) view.findViewById(R.id.all_order);
            mChildContainer = (FrameLayout) view.findViewById(R.id.fl_child_container);

            mSettingView = (RelativeLayout) view.findViewById(R.id.rl_setting);
            iv_header = (ImageView) view.findViewById(R.id.iv_header);
            tv_desc = (TextView) view.findViewById(R.id.tv_desc);
            tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
            tv_collect = (TextView) view.findViewById(R.id.tv_collect);
            tv_collocation = (TextView) view.findViewById(R.id.tv_collocation);
            tv_unpay_count = (TextView) view.findViewById(R.id.tv_unpay_count);
            mCartCount = (TextView) view.findViewById(R.id.tv_cart_count);

            view.findViewById(R.id.accountInfoBtn).setOnClickListener(this);
            mPrePayView.setOnClickListener(this);
            mPreReceivedView.setOnClickListener(this);
            mReturnView.setOnClickListener(this);
            mAllOrderView.setOnClickListener(this);
            mSettingView.setOnClickListener(this);
            view.findViewById(R.id.tv_shopping_cart).setOnClickListener(this);
            view.findViewById(R.id.rl_contact).setOnClickListener(this);
            view.findViewById(R.id.rl_competition).setOnClickListener(this);
            view.findViewById(R.id.rl_taobao_order).setOnClickListener(this);
            view.findViewById(R.id.rl_addr_manage).setOnClickListener(this);
            view.findViewById(R.id.ll_collect).setOnClickListener(this);
            view.findViewById(R.id.ll_collocation).setOnClickListener(this);

            mRequestQueue = Volley.newRequestQueue(getActivity());
            getUserDetail();
            getUserInfo();
            userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
            token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
            creatCartDB = new CreatCartDB(getActivity());

            initCartTips();

            FetchCollectData();
            FetchCollocationData();
            FetchUnPayCount(UPDATE_UNPAY_COUNT);

        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        return view;
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshUnPayAddTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshUnPayDesTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshUnPayTag);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 退出登录事件处理
     */
    private void exitLoginEvent() {
        tv_collect.setText("0");
        tv_collocation.setText("0");
        tv_unpay_count.setVisibility(View.GONE);
        ((MainActivity) getActivity()).hideTip(View.GONE);
        tv_nickname.setText("");
        tv_desc.setText("");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_shopping_cart:
                ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(shopCartFragment);
                break;
            case R.id.all_order:
                OrderAllOrdersFragment allOrdersFragment = OrderAllOrdersFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(allOrdersFragment);
                break;
            case R.id.rl_taobao_order:
                Intent intent1 = new Intent(getActivity(), WebViewActivity.class);
                intent1.putExtra("url", ZhaiDou.URL_TAOBAO_ORDER);
                startAnimActivity(intent1);
                break;
            case R.id.tv_pre_pay:
                ToolUtils.setLog("count:"+count);
                OrderUnPayFragment unPayFragment = OrderUnPayFragment.newInstance("", "",count);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(unPayFragment);
                unPayFragment.setBackClickListener(new OrderUnPayFragment.BackCountListener()
                {
                    @Override
                    public void onBackCount(int counts)
                    {
                        if (counts>0)
                        {
                            tv_unpay_count.setText(""+counts);
                        }
                        else
                        {
                            tv_unpay_count.setVisibility(View.GONE);
                        }
                    }
                });
                ((MainActivity) getActivity()).hideTip(View.GONE);
                break;
            case R.id.tv_pre_received:
                OrderUnReceiveFragment unReceiveFragment = OrderUnReceiveFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(unReceiveFragment);
                break;
            case R.id.tv_return:
                OrderReturnFragment returnFragment = OrderReturnFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(returnFragment);
                break;
            case R.id.rl_addr_manage:
                AddrManageFragment addrManageFragment = AddrManageFragment.newInstance("", "", "", "", 0);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(addrManageFragment);
                break;
            case R.id.rl_setting:
                SettingFragment mSettingFragment=SettingFragment.newInstance("","");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(mSettingFragment);
                mSettingFragment.setProfileListener(new SettingFragment.ProfileListener() {
                    @Override
                    public void onProfileChange(User user) {
                        if (!TextUtils.isEmpty(user.getDescription())){
                            tv_desc.setText(user.getDescription());
                        }else if (!TextUtils.isEmpty(user.getAvatar())){
                            ToolUtils.setImageCacheUrl("http://" + user.getAvatar(), iv_header);
                        }else if (!TextUtils.isEmpty(user.getNickName())){
                            tv_nickname.setText(user.getNickName());
                        }
                    }
                });
                break;
            case R.id.rl_contact:
                ContactUsFragment contactUsFragment = ContactUsFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(contactUsFragment);
                break;
            case R.id.accountInfoBtn:
                mProfileFragment=ProfileFragment.newInstance("","");
                mProfileFragment.setProfileListener(new ProfileFragment.ProfileListener()
                {
                    @Override
                    public void onProfileChange(User user)
                    {
                        if (!TextUtils.isEmpty(user.getDescription())){
                            tv_desc.setText(user.getDescription());
                        }else if (!TextUtils.isEmpty(user.getAvatar())){
                            ToolUtils.setImageCacheUrl("http://" + user.getAvatar(), iv_header);
                        }else if (!TextUtils.isEmpty(user.getNickName())){
                            tv_nickname.setText(user.getNickName());
                        }
                    }
                });
                ((MainActivity)getActivity()).navigationToFragmentWithAnim(mProfileFragment);
                break;
            case R.id.rl_competition:
                Intent intent = new Intent(getActivity(), HomePTActivity.class);
                intent.putExtra("url", ZhaiDou.COMPETITION_URL);
                intent.putExtra("from", "competition");
                intent.putExtra("title", "拼贴大赛");
                intent.setFlags(2);
                startActivity(intent);
                break;
            case R.id.ll_collect:
                CollectFragment collectFragment = CollectFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(collectFragment);
                collectFragment.setCollectCountChangeListener(new CollectFragment.CollectCountChangeListener() {
                    @Override
                    public void onCountChange(int count, Fragment fragment) {
                        System.out.println("PersonalFragment.onCountChange------->"+collectNum);
//                        collectNum=collectNum-1;
//                        if (collectNum<=0)
//                        {
//                            collectNum=0;
//                        }
                        tv_collect.setText(collectNum-1<=0?0+"":""+--collectNum);
                    }
                });
                break;
            case R.id.ll_collocation:
                CollocationFragment collocationFragment = CollocationFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(collocationFragment);
                break;
        }

    }

    public void getUserInfo() {

        Object id = SharedPreferencesUtil.getData(getActivity(), "userId", 0);
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject userObj = jsonObject.optJSONObject("user");
                String email = userObj.optString("email");
                String avatar = userObj.optJSONObject("avatar").optString("url");
                String nick_name = userObj.optString("nick_name");
                String province = userObj.optString("province");
                String city = userObj.optString("city");
                User user = new User();
                user.setAvatar(avatar);
                user.setNickName(nick_name);
                user.setProvince(province);
                user.setCity(city);
                Message message = new Message();
                message.what = UPDATE_USER_INFO;
                message.obj = user;
                mHandler.sendMessage(message);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        mRequestQueue.add(request);
    }

    public void getUserDetail() {
        Object id = SharedPreferencesUtil.getData(getActivity(), "userId", 0);
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + id + "/profile", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                JSONObject userObj = jsonObject.optJSONObject("profile");
                if (userObj != null) {
                    String nick_name = userObj.optString("nick_name");
                    String mobile = userObj.optString("mobile");
                    String description = userObj.optString("description");
//                int profileId=userObj.optString("id");
                    boolean verified = userObj.optBoolean("verified");
                    User user = new User(null, null, nick_name, verified, mobile, description);
                    Message message = new Message();
                    message.what = UPDATE_USER_DESCRIPTION;
                    message.obj = user;
                    mHandler.sendMessage(message);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(mActivity);
        mRequestQueue.add(request);
    }

    @Override
    public void onCountChange(int count, Fragment fragment) {
        if (fragment instanceof CollectFragment) {
            collect_count = count;
        } else if (fragment instanceof CollocationFragment) {
            collocation_count = count;
        }
    }

    @Override
    public void onProfileChange(User user) {
        if (!TextUtils.isEmpty(user.getNickName()))
            tv_nickname.setText(user.getNickName());
        if (!TextUtils.isEmpty(user.getAvatar()))
            ToolUtils.setImageCacheUrl("http://" + user.getAvatar(), iv_header);
        if (!TextUtils.isEmpty(user.getDescription()))
            tv_desc.setText(user.getDescription());
    }

    public void refreshData(Activity activity) {
        userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        if (userId != -1) {
//            getUserDetail();
//            getUserInfo();
//            FetchCollectData();
//            FetchCollocationData();
//            FetchUnPayCount(UPDATE_UNPAY_COUNT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView()
    {
        if (broadcastReceiver!=null)
            getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips() {
        if (((MainActivity)getActivity()).getNum() > 0) {
            mCartCount.setVisibility(View.VISIBLE);
            mCartCount.setText("" + ((MainActivity)getActivity()).getNum());
        } else {
            mCartCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        System.out.println("PersonalFragment.onHiddenChanged-------->"+hidden);
        userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        if (!hidden && userId != -1) {
            FetchCollectData();
            FetchCollocationData();
            FetchUnPayCount(UPDATE_UNPAY_COUNT_REFRESH);
            getUserDetail();
            getUserInfo();
            initCartTips();
        }
        super.onHiddenChanged(hidden);
    }

    private void FetchCollectData() {
        ToolUtils.setLog("查看收藏");
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_COLLECT_ITEM_URL + 1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("FetchCollectData-------->", jsonObject.toString());
                if (jsonObject != null) {
                    JSONObject meta = jsonObject.optJSONObject("meta");
                    int count = meta == null ? 0 : meta.optInt("count");
                    Message message = new Message();
                    message.what = UPDATE_USER_COLLECT_COUNT;
                    message.arg1 = count;
                    mHandler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    private void FetchCollocationData() {
        ToolUtils.setLog("查看豆搭");
        final String token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
//        final int userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_COLLOCATION_ITEM_URL + userId + "/bean_collocations?page=" + 1
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    JSONObject meta = jsonObject.optJSONObject("meta");
                    int count = meta == null ? 0 : meta.optInt("count");
                    Message message = new Message();
                    message.arg1 = count;
                    message.what = UPDATE_USER_COLLOCATION;
                    mHandler.sendMessage(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        mRequestQueue.add(request);
    }

    private void FetchUnPayCount(final int f) {
        ToolUtils.setLog("查看代付款数量");
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.URL_ORDER_LIST + "?count=1&status=0", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    ToolUtils.setLog(jsonObject.toString());
                    int count = jsonObject.optInt("count");
                    ToolUtils.setLog("个人页面count:"+count);
                    if (count > 0)
                    {
                        Message message = new Message();
                        message.what = f;
                        message.arg1 = count;
                        mHandler.sendMessage(message);
                    }
                    else
                    {
                        tv_unpay_count.setVisibility(View.GONE);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                tv_unpay_count.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("SECAuthorization", token);
                return headers;
            }
        };
        mRequestQueue.add(request);
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_personal));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_personal));
    }
}
