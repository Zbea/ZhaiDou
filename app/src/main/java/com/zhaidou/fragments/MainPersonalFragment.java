package com.zhaidou.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.easemob.chat.EMChatManager;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZDApplication;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.*;
import com.zhaidou.base.AccountManage;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.CountManage;
import com.zhaidou.base.EaseManage;
import com.zhaidou.base.ProfileManage;
import com.zhaidou.model.User;
import com.zhaidou.utils.EaseUtils;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainPersonalFragment extends BaseFragment implements View.OnClickListener, CountManage.onCountChangeListener, ProfileManage.OnProfileChange, AccountManage.AccountListener, EaseManage.onMessageChange {

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
    private final int UPDATE_CARTCAR_DATA = 7;
    private final int UPDATE_UNREAD_MSG=8;

    private ProfileFragment mProfileFragment;
    private ImageView iv_header, mPrePayView, mPreReceivedView, mReturnView;
    private TextView tv_nickname, tv_desc, tv_collect, tv_collocation, tv_unpay_count;
    private RelativeLayout mCouponsView, mSettingView, mAllOrderView;
    private FrameLayout mChildContainer;
    private TextView mCartCount;
    private int userId;
    private String token;
    private int count = 0;
    private int collectNum = 0;

    private TextView unReadMsgView;
    private User mUser;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_USER_INFO:
                    ToolUtils.setImageCacheUrl("http://" + mUser.getAvatar(), iv_header, R.drawable.icon_header_default);
                    if (!TextUtils.isEmpty(mUser.getNickName()))
                        tv_nickname.setText(mUser.getNickName());
                    SharedPreferencesUtil.saveData(mContext, "avatar", "http://" + mUser.getAvatar());
                    break;
                case UPDATE_USER_DESCRIPTION:
                    SharedPreferencesUtil.saveData(mContext,"mobile",mUser.getMobile());
                    SharedPreferencesUtil.saveData(mContext,"description",mUser.getDescription());
                    tv_desc.setText("null".equalsIgnoreCase(mUser.getDescription()) || mUser.getDescription() == null ? "" : mUser.getDescription());
                    break;
                case UPDATE_USER_COLLECT_COUNT:
                    collectNum = msg.arg1;
                    ToolUtils.setLog("收藏" + msg.arg1);
                    tv_collect.setText(msg.arg1 + "");
                    break;
                case UPDATE_USER_COLLOCATION:
                    ToolUtils.setLog("豆搭：" + msg.arg1);
                    tv_collocation.setText(msg.arg1 + "");
                    break;
                case UPDATE_UNPAY_COUNT:
                    count = msg.arg1;
                    ToolUtils.setLog("代付款：" + count);
                    tv_unpay_count.setVisibility(View.VISIBLE);
                    ((MainActivity) getActivity()).hideTip(View.VISIBLE);
                    tv_unpay_count.setText(count + "");
                    break;
                case UPDATE_UNPAY_COUNT_REFRESH:
                    count = msg.arg1;
                    ToolUtils.setLog("代付款：" + count);
                    tv_unpay_count.setVisibility(View.VISIBLE);
                    tv_unpay_count.setText(count + "");
                    break;
                case UPDATE_CARTCAR_DATA:
                    int num = msg.arg2;
                    mCartCount.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
                    mCartCount.setText("" + num);
                    break;
                case UPDATE_UNREAD_MSG:
                    setUnreadMsg();
                    break;
            }
        }
    };

    public static MainPersonalFragment mainPersonalFragment;

    public static MainPersonalFragment newInstance(String param1, String context) {
        if (mainPersonalFragment == null)
            mainPersonalFragment = new MainPersonalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_CONTEXT, context);
        mainPersonalFragment.setArguments(args);
        return mainPersonalFragment;
    }

    public MainPersonalFragment() {
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
            view = inflater.inflate(R.layout.fragment_main_personal, container, false);

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
            unReadMsgView = (TextView) view.findViewById(R.id.unreadMsg);

            view.findViewById(R.id.accountInfoBtn).setOnClickListener(this);
            mPrePayView.setOnClickListener(this);
            mPreReceivedView.setOnClickListener(this);
            mReturnView.setOnClickListener(this);
            mAllOrderView.setOnClickListener(this);
            mSettingView.setOnClickListener(this);
            view.findViewById(R.id.tv_shopping_cart).setOnClickListener(this);
            view.findViewById(R.id.rl_contact).setOnClickListener(this);
            view.findViewById(R.id.rl_competition).setOnClickListener(this);
            view.findViewById(R.id.rl_collocation).setOnClickListener(this);
            view.findViewById(R.id.rl_addr_manage).setOnClickListener(this);
            view.findViewById(R.id.ll_collect).setOnClickListener(this);
            view.findViewById(R.id.ll_collocation).setOnClickListener(this);
            view.findViewById(R.id.rl_msg).setOnClickListener(this);
            view.findViewById(R.id.rl_service).setOnClickListener(this);
            view.findViewById(R.id.couponLayout).setOnClickListener(this);

            mRequestQueue = Volley.newRequestQueue(getActivity());
            getUserDetail();
            getUserInfo();
            userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
            token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");

            int value = CountManage.getInstance().value(CountManage.TYPE.TAG_PREPAY);
            tv_unpay_count.setText(value + "");
            tv_unpay_count.setVisibility(value == 0 ? View.GONE : View.VISIBLE);
            setUnreadMsg();
            CountManage.getInstance().setOnCountChangeListener(this);
            AccountManage.getInstance().register(this);
            ProfileManage.getInstance().register(this);
            EaseManage.getInstance().setOnMessageChange(this);
            mUser=new User();

        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        return view;
    }

    private void setUnreadMsg() {
        int unreadMsgsCount = EMChatManager.getInstance().getUnreadMsgsCount();
        unReadMsgView.setVisibility(unreadMsgsCount > 0 ? View.VISIBLE : View.GONE);
        unReadMsgView.setText(unreadMsgsCount > 99 ? "99+" : unreadMsgsCount + "");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_shopping_cart:
                ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(shopCartFragment);
                break;
            case R.id.all_order:
                OrderAllOrdersFragment allOrdersFragment = OrderAllOrdersFragment.newInstance(ZhaiDou.TYPE_ORDER_ALL, "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(allOrdersFragment);
                break;
            case R.id.rl_collocation:
                CollocationFragment collocationFragment = CollocationFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(collocationFragment);
                break;
            case R.id.tv_pre_pay:
                ((MainActivity) getActivity()).hideTip(View.GONE);
                OrderAllOrdersFragment unPayFragment = OrderAllOrdersFragment.newInstance(ZhaiDou.TYPE_ORDER_PREPAY, "");
                ((BaseActivity) getActivity()).navigationToFragment(unPayFragment);
                break;
            case R.id.tv_pre_received:
                OrderAllOrdersFragment unReceiveFragment = OrderAllOrdersFragment.newInstance(ZhaiDou.TYPE_ORDER_PRERECEIVE, "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(unReceiveFragment);
                break;
            case R.id.tv_return:
                OrderReturnFragment returnFragment = OrderReturnFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(returnFragment);
                break;
            case R.id.couponLayout:
                CouponsContainerFragment couponsFragment=new CouponsContainerFragment();
                ((BaseActivity)getActivity()).navigationToFragment(couponsFragment);
                break;
            case R.id.rl_addr_manage:
                AddrManageFragment addrManageFragment = AddrManageFragment.newInstance("", "", "", "", 0);
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(addrManageFragment);
                break;
            case R.id.rl_setting:
                SettingFragment mSettingFragment = SettingFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(mSettingFragment);
                break;
            case R.id.rl_contact:
                ContactUsFragment contactUsFragment = ContactUsFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(contactUsFragment);
                break;
            case R.id.accountInfoBtn:
                mProfileFragment = ProfileFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragmentWithAnim(mProfileFragment);
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
                        tv_collect.setText(collectNum - 1 <= 0 ? 0 + "" : "" + --collectNum);
                    }
                });
                break;
            case R.id.rl_msg:
                Intent intent1 = new Intent(getActivity(), ConversationListActivity.class);
                intent1.putExtra("userId", "service");
                startActivity(intent1);
                break;
            case R.id.rl_service:
                EaseUtils.startKeFuActivity(mContext);
                break;
        }
    }

    public void getUserInfo() {
        Object id = SharedPreferencesUtil.getData(getActivity(), "userId", 0);
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + "?id=" + id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String msg = jsonObject.optString("message");
                if (status == 200)
                {
                    JSONObject dataObj = jsonObject.optJSONObject("data");

                    JSONObject userObj = dataObj.optJSONObject("user");
                    String avatar = "";
                    if (userObj != null)
                    {
                        if (userObj.optJSONObject("avatar")!=null)
                        {
                            avatar = userObj.optJSONObject("avatar").optString("url");
                        }
                        String nick_name = userObj.optString("nick_name");
                        String province = userObj.optString("province");
                        String city = userObj.optString("city");
                        //                    User user = new User();
                        mUser.setAvatar(avatar);
                        mUser.setNickName(nick_name);
                        mUser.setProvince(province);
                        mUser.setCity(city);
                        Message message = new Message();
//                    message.what = UPDATE_USER_INFO;
//                    message.obj = user;
                        mHandler.sendEmptyMessage(UPDATE_USER_INFO);
                    }

                } else {
                    ShowToast(msg);
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
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        mRequestQueue.add(request);
    }

    public void getUserDetail() {
        Object id = SharedPreferencesUtil.getData(getActivity(), "userId", 0);
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_DETAIL_PROFILE_URL + "?id=" + id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                int status = jsonObject.optInt("status");
                String msg = jsonObject.optString("message");
                if (status == 200) {
                    JSONObject dataObj = jsonObject.optJSONObject("data");
                    JSONObject userObj = dataObj.optJSONObject("profile");
                    if (userObj != null) {
                        String nick_name = userObj.optString("nick_name");
                        String mobile = userObj.optString("mobile");
                        String description = userObj.optString("description");
                        boolean verified = userObj.optBoolean("verified");
//                        User user = new User(null, null, nick_name, verified, mobile, description);
                        mUser.setNickName(nick_name);
                        mUser.setMobile(mobile);
                        mUser.setDescription(description);
                        mUser.setVerified(verified);
                        Message message = new Message();
                        message.what = UPDATE_USER_DESCRIPTION;
//                        message.obj = user;
                        mHandler.sendEmptyMessage(UPDATE_USER_DESCRIPTION);
                    }

                } else {
                    ShowToast(msg);
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
                headers.put("ZhaidouVesion", mContext.getResources().getString(R.string.app_versionName));
                return headers;
            }
        };
        ((ZDApplication) getActivity().getApplication()).mRequestQueue.add(request);
    }

    public void refreshData(Activity activity) {
        userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        if (userId != -1) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        userId = (Integer) SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        token = (String) SharedPreferencesUtil.getData(getActivity(), "token", "");
        if (!hidden && userId != -1) {
            getUserDetail();
            getUserInfo();
        }
        setUnreadMsg();
        super.onHiddenChanged(hidden);
    }

    public void onResume() {
        super.onResume();
        setUnreadMsg();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_personal));
        InputMethodManager inputMethodManager=(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(getActivity().getWindow().peekDecorView().getApplicationWindowToken(),0);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_personal));
    }

    @Override
    public void onCount(int count) {
        int value = CountManage.getInstance().value(CountManage.TYPE.TAG_PREPAY);
        tv_unpay_count.setText(value + "");
        tv_unpay_count.setVisibility(value == 0 ? View.GONE : View.VISIBLE);
//        if (tv_unpay_count.isShown() || EMChatManager.getInstance().getUnreadMsgsCount() > 0)
//            ((MainActivity) getActivity()).hideTip(View.VISIBLE);

    }

    @Override
    public void onProfileChange(ProfileManage.TAG tag, String message) {
        switch (tag) {
            case HEADER:
                ToolUtils.setImageCacheUrl(message, iv_header, R.drawable.icon_header_default);
                break;
            case NICK:
                if (!TextUtils.isEmpty(message))
                    tv_nickname.setText(message);
                break;
            case DESC:
                tv_desc.setText("null".equalsIgnoreCase(message) || message == null ? "" : message);
                break;
        }
    }

    @Override
    public void onLogOut() {
        tv_unpay_count.setVisibility(View.GONE);
        tv_nickname.setText("");
        tv_desc.setText("");
        iv_header.setBackgroundResource(R.drawable.icon_header_default);
    }

    @Override
    public void onMessage(int unreadMsgCount) {
        mHandler.sendEmptyMessage(UPDATE_UNREAD_MSG);
    }
}
