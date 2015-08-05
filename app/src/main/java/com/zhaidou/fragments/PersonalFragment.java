package com.zhaidou.fragments;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.activities.HomePTActivity;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.User;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.AsyncImageLoader1;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalFragment extends BaseFragment implements View.OnClickListener, CollectFragment.CollectCountChangeListener,
        CollocationFragment.CollocationCountChangeListener, SettingFragment.ProfileListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_CONTEXT = "context";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private SettingFragment mSettingFragment;

    AsyncImageLoader1 imageLoader;
    private RequestQueue mRequestQueue;
    private final int UPDATE_USER_INFO = 1;
    private final int UPDATE_USER_DESCRIPTION = 2;

    private Map<String, String> cityMap = new HashMap<String, String>();

    private int collect_count = 0;
    private int collocation_count = 0;

    private Activity mActivity;

    private User user;
    private int num;

    private CreatCartDB creatCartDB;
    private List<CartItem> items = new ArrayList<CartItem>();
    private ImageView iv_header, mPrePayView, mPreReceivedView, mReturnView;
    private TextView tv_nickname, tv_desc;
    private RelativeLayout mCouponsView, mSettingView, mAllOrderView;
    private FrameLayout mChildContainer;
    private TextView mCartCount;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_USER_INFO:
                    User user = (User) msg.obj;
                    ToolUtils.setImageCacheUrl("http://" + user.getAvatar(), iv_header);
                    if (!TextUtils.isEmpty(user.getNickName()))
                        tv_nickname.setText(user.getNickName());
//                    tv_province.setText(cityMap.get(user.getProvince()));
//                    tv_city.setText(cityMap.get(user.getCity()));
                    break;
                case UPDATE_USER_DESCRIPTION:
                    User u = (User) msg.obj;
                    tv_desc.setText("null".equalsIgnoreCase(u.getDescription()) || u.getDescription() == null ? "" : u.getDescription());
                    break;
            }
        }
    };

    // TODO: Rename and change types and number of parameters
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.personal, container, false);

        mPrePayView = (ImageView) view.findViewById(R.id.tv_pre_pay);
        mPreReceivedView = (ImageView) view.findViewById(R.id.tv_pre_received);
        mReturnView = (ImageView) view.findViewById(R.id.tv_return);
        mAllOrderView = (RelativeLayout) view.findViewById(R.id.all_order);
        mChildContainer = (FrameLayout) view.findViewById(R.id.fl_child_container);

        mSettingView = (RelativeLayout) view.findViewById(R.id.rl_setting);
        iv_header = (ImageView) view.findViewById(R.id.iv_header);
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);
        mCartCount = (TextView) view.findViewById(R.id.tv_cart_count);

        mPrePayView.setOnClickListener(this);
        mPreReceivedView.setOnClickListener(this);
        mReturnView.setOnClickListener(this);
        mAllOrderView.setOnClickListener(this);
        mSettingView.setOnClickListener(this);
        mSettingFragment = SettingFragment.newInstance("", "");
        view.findViewById(R.id.tv_shopping_cart).setOnClickListener(this);
        view.findViewById(R.id.rl_contact).setOnClickListener(this);
        view.findViewById(R.id.rl_competition).setOnClickListener(this);
        view.findViewById(R.id.rl_taobao_order).setOnClickListener(this);
        view.findViewById(R.id.rl_addr_manage).setOnClickListener(this);

        mSettingFragment.setProfileListener(this);

        mRequestQueue = Volley.newRequestQueue(getActivity());
        getUserDetail();
        getUserInfo();
        creatCartDB = new CreatCartDB(getActivity());
        initCartTips();
        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_shopping_cart:
                Toast.makeText(getActivity(), "购物车", Toast.LENGTH_SHORT).show();
                ShopCartFragment shopCartFragment = ShopCartFragment.newInstance("", 0);
                ((MainActivity) getActivity()).navigationToFragment(shopCartFragment);
                break;
            case R.id.all_order:
                AllOrdersFragment allOrdersFragment = AllOrdersFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(allOrdersFragment);
                break;
            case R.id.rl_taobao_order:
                break;
            case R.id.tv_pre_pay:
                UnPayFragment unPayFragment = UnPayFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(unPayFragment);
                break;
            case R.id.tv_pre_received:
                UnReceiveFragment unReceiveFragment = UnReceiveFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(unReceiveFragment);
                break;
            case R.id.tv_return:
                ReturnFragment returnFragment = ReturnFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(returnFragment);
                break;
            case R.id.rl_addr_manage:
                AddrManageFragment addrManageFragment = AddrManageFragment.newInstance("", "", "", "", 0);
                ((MainActivity) getActivity()).navigationToFragment(addrManageFragment);
                break;
            case R.id.rl_setting:
                ((MainActivity) getActivity()).navigationToFragment(mSettingFragment);
                break;
            case R.id.rl_contact:
                ContactUsFragment contactUsFragment = ContactUsFragment.newInstance("", "");
                ((MainActivity) getActivity()).navigationToFragment(contactUsFragment);
                break;
            case R.id.rl_competition:
                Intent intent = new Intent(getActivity(), HomePTActivity.class);
                intent.putExtra("url", ZhaiDou.COMPETITION_URL);
                intent.putExtra("from", "competition");
                intent.putExtra("title", "拼贴大赛");
                startActivity(intent);
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
                Log.i("volleyError---------->", volleyError.toString());
            }
        });
        mRequestQueue.add(request);
    }

    public void getUserDetail() {
        Object id = SharedPreferencesUtil.getData(getActivity(), "userId", 0);
        Log.i("getUserDetail-----id---->", id + "");
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.USER_SIMPLE_PROFILE_URL + id + "/profile", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("getUserDetail----->", jsonObject.toString());
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
                Log.i("volleyError---------->", volleyError.toString());
            }
        });
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(mActivity);
        mRequestQueue.add(request);
    }

    public void getCityList() {
        String city = getString(R.string.city);
        try {
            JSONArray cityArr = new JSONArray(city);
            for (int i = 0; i < cityArr.length(); i++) {
                JSONObject cityObj = cityArr.optJSONObject(i);
                String index = cityObj.optString("index");
                String desc = cityObj.optString("desc");
                cityMap.put(index, desc);
            }
            getUserInfo();
            getUserDetail();
        } catch (Exception e) {

        }
    }

    @Override
    public void onCountChange(int count, Fragment fragment) {
        Log.i("onCountChange------->", count + "");
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
            imageLoader.LoadImage("http://" + user.getAvatar(), iv_header);
        if (!TextUtils.isEmpty(user.getDescription()))
            tv_desc.setText(user.getDescription());
    }

    public void refreshData(Activity activity) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initCartTips() {
        items = CreatCartTools.selectByAll(creatCartDB);
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isPublish.equals("true")) {
                items.remove(items.get(i));
            }
        }
        if (items.size() > 0) {
            num = 0;
            for (int i = 0; i < items.size(); i++) {
                num = num + items.get(i).num;
            }
            mCartCount.setVisibility(View.VISIBLE);
            mCartCount.setText("" + num);
        } else {
            mCartCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Integer userId= (Integer)SharedPreferencesUtil.getData(getActivity(), "userId", -1);
        if (!hidden&&userId!=-1){
            getUserDetail();
            getUserInfo();
        }
        super.onHiddenChanged(hidden);
    }
}
