package com.zhaidou;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.CallbackContext;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.dialog.CustomVersionUpdateDialog;
import com.zhaidou.fragments.CategoryFragment1;
import com.zhaidou.fragments.DiyFragment;
import com.zhaidou.fragments.HomeCategoryFragment;
import com.zhaidou.fragments.HomeFragment;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.PersonalFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.fragments.ShopPaymentFailFragment;
import com.zhaidou.fragments.ShopPaymentFragment;
import com.zhaidou.fragments.ShopPaymentSuccessFragment;
import com.zhaidou.fragments.StrategyFragment;
import com.zhaidou.fragments.WebViewFragment;
import com.zhaidou.model.Area;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.City;
import com.zhaidou.model.Province;
import com.zhaidou.model.User;
import com.zhaidou.model.ZhaiDouRequest;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.NetService;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class MainActivity extends BaseActivity implements DiyFragment.OnFragmentInteractionListener, WebViewFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener, CategoryFragment1.OnFragmentInteractionListener,
        RegisterFragment.RegisterOrLoginListener{

    private Fragment utilityFragment;
    private Fragment beautyHomeFragment;
    private Fragment categoryFragment;
    private Fragment diyFragment;

    private ImageButton homeButton;
    private ImageButton beautyButton;
    private ImageButton categoryButton;
    private ImageButton diyButton;

    private ImageButton lastButton;


    private TextView titleView;
    private LinearLayout mTabContainer;
    private LoginFragment mLoginFragment;
    private ImageView iv_dot;
    private LinearLayout viewLayout;

    private String token;
    private int id;

    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    public String filePath = "";

    private long mTime;
    private Activity mContext;
    private CreatCartDB creatCartDB;
    private String serverName;
    private String serverInfo;
    private String serverUrl;
    private int serverCode;
    private RequestQueue mRequestQueue;
    private final int WX_PAY_SUCCESS = 0;
    private final int WX_PAY_FAILED = -1;
    private final int WX_PAY_CANCEL = -2;
    public static List<Province> provinceList = new ArrayList<Province>();

    public int num = 0;
    public List<CartItem> items = new ArrayList<CartItem>();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag)) {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag)) {
                initCartTips();
            }
            if (action.equalsIgnoreCase(ZhaiDou.BROADCAST_WXAPI_FILTER)) {
                System.out.println("MainActivity.onReceive");
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                int result = intent.getIntExtra("code", -2);
                Log.i("result---------------->", result + "------" + fragments.size());
                if (fragments.size() > 1) {
                    Fragment fragment = fragments.get(fragments.size() - 1);
                    Fragment shopPaymentFragment = getSupportFragmentManager().findFragmentByTag(ShopPaymentFragment.class.getSimpleName());
                    Fragment shopPaymentFailFragment = getSupportFragmentManager().findFragmentByTag(ShopPaymentFailFragment.class.getSimpleName());

                    if (shopPaymentFragment != null) {
                        ((ShopPaymentFragment) shopPaymentFragment).setPayment();
                    }
                    if (shopPaymentFailFragment != null && shopPaymentFailFragment instanceof ShopPaymentFailFragment) {
                        ((ShopPaymentFailFragment) shopPaymentFailFragment).handleWXPayResult(result);
                    } else if (shopPaymentFragment != null && shopPaymentFragment instanceof ShopPaymentFragment) {
                        ((ShopPaymentFragment) shopPaymentFragment).setPayment();
                        ((ShopPaymentFragment) shopPaymentFragment).handleWXPayResult(result);
                    } else {
                        System.out.println("MainActivity.onReceive--------->null------------>");
                    }
                }
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    User user = (User) msg.obj;
                    if (persoanlFragment == null) {
                        persoanlFragment = PersonalFragment.newInstance("", "");
                    } else {
                        persoanlFragment.refreshData(MainActivity.this);
                    }
                    selectFragment(currentFragment, persoanlFragment);
                    setButton(personalButton);
                    break;

                case 1:
                    serverCode = parseJosn(msg.obj.toString());
                    if (serverCode > ZDApplication.localVersionCode) {
                        CustomVersionUpdateDialog customVersionUpdateDialog = new CustomVersionUpdateDialog(mContext, serverName, serverUrl);
                        customVersionUpdateDialog.checkUpdateInfo();
                    }

                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);
        iv_dot = (ImageView) findViewById(R.id.iv_dot);
        viewLayout = (LinearLayout) findViewById(R.id.content);
        mContext = this;
        init();
        initBroadcastReceiver();

        getVersionServer();

        initComponents();
        commitActiveData();
        AlibabaSDK.asyncInit(this, new InitResultCallback()
        {
            @Override
            public void onSuccess()
            {
            }
            @Override
            public void onFailure(int i, String s)
            {
            }
        });
    }

    private void commitActiveData() {
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final String channel = appInfo.metaData.getString("UMENG_CHANNEL");
        Log.d("appInfo---", " msg == " + channel);
        String imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                .getDeviceId();
        Map<String, String> map = new HashMap<String, String>();
        map.put("device_token[device_token]", imei);
        ZhaiDouRequest request = new ZhaiDouRequest(Request.Method.POST, ZhaiDou.URL_STATISTICS, map, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.println("ZDApplication.onResponse---->" + jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<String, String>();
                header.put("zd-client", channel);
                return header;
            }
        };
        mRequestQueue.add(request);
    }

    /**
     * 获取版本信息
     */
    private void getVersionServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = ZhaiDou.ApkUrl;
                String result = NetService.getHttpService(url);
                System.out.println("MainActivity.run----getVersionServer--->"+result);
                if (result != null) {
                    mHandler.obtainMessage(1, result).sendToTarget();
                }
            }
        }).start();
    }

    /**
     * 注册广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.BROADCAST_WXAPI_FILTER);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void init() {
        creatCartDB = new CreatCartDB(mContext);
        initCartTips();
        mRequestQueue = Volley.newRequestQueue(this, new HttpClientStack(new DefaultHttpClient()));
        FetchCityData();
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips() {
        num=0;
        if (checkLogin()) {
            getGoodsItems();
            for (int i = 0; i < items.size(); i++)
            {
                if (items.get(i).isPublish.equals("false") && items.get(i).isOver.equals("false")) {
                    num = num + items.get(i).num;
                }
                System.out.println("MainActivity.run--------->"+num);
            }
        }
    }

    /**
     * 获得当前userId的所有商品
     */
    private void getGoodsItems() {
        items.removeAll(items);
        //遍历获得这个当前uesrId的所有商品
        items = CreatCartTools.selectByAll(creatCartDB, id);

    }

    public int getNum() {
        return num;
    }

    public List<CartItem> getItems()
    {
        return items;
    }

    public void initComponents() {

        mTabContainer = (LinearLayout) findViewById(R.id.tab_container);
        mChildContainer = (FrameLayout) findViewById(R.id.fl_child_container);
        homeButton = (ImageButton) findViewById(R.id.tab_home);

        lastButton = homeButton;

        if (utilityFragment == null) {
            utilityFragment = HomeFragment.newInstance(ZhaiDou.HOME_PAGE_URL, ZhaiDou.ListType.HOME.toString());
        }

        currentFragment = utilityFragment;
        setButton(lastButton);
        setDefaultFragment(utilityFragment);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                titleView.setText("每日精选功能美物");

                selectFragment(currentFragment, utilityFragment);
                setButton(view);
            }
        });

        beautyButton = (ImageButton) findViewById(R.id.tab_beauty);
        beautyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                titleView.setText("专业家居美化方案");

                if (beautyHomeFragment == null) {
                    beautyHomeFragment = new StrategyFragment();
                }

                selectFragment(currentFragment, beautyHomeFragment);
                setButton(view);
            }
        });

        categoryButton = (ImageButton) findViewById(R.id.tab_category);
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                titleView.setText("全类别");

                if (categoryFragment == null) {
                    categoryFragment = CategoryFragment1.newInstance("", "");
                }


                selectFragment(currentFragment, categoryFragment);
                setButton(view);
            }
        });

        diyButton = (ImageButton) findViewById(R.id.tab_diy);
        diyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (diyFragment == null) {
                    diyFragment = DiyFragment.newInstance("haha", "haha");
                }

                selectFragment(currentFragment, diyFragment);

                setButton(view);
            }
        });

        personalButton = (ImageButton) findViewById(R.id.tab_personal);
        personalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkLogin()) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(2);
                    MainActivity.this.startActivityForResult(intent, 10000);
                } else {
                    if (persoanlFragment == null) {
                        persoanlFragment = PersonalFragment.newInstance("", "");
                    }
                    selectFragment(currentFragment, persoanlFragment);
                    setButton(view);
                }

            }
        });

    }

    public void setButton(View view) {
        ImageButton button = (ImageButton) view;

        lastButton.setSelected(false);

        lastButton = button;

        lastButton.setSelected(true);

    }

    public void setDefaultFragment(Fragment defaultFragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.content, defaultFragment).commit();
    }

    public void selectFragment(Fragment from, Fragment to) {
        if (currentFragment != to) {
            currentFragment = to;
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            if (!to.isAdded()) {
                transaction.hide(from).add(R.id.content, to).commit();
            } else {
                transaction.hide(from).show(to).commit();
            }
        }
    }

    public void onFragmentInteraction(Uri uri) {

    }


    public void toggleTabContainer() {
        mTabContainer.setVisibility(mTabContainer.isShown() ? View.GONE : View.VISIBLE);
    }

    public void toggleTabContainer(int visible) {
        mTabContainer.setVisibility(visible);
    }

    public boolean checkLogin() {
        token = (String) SharedPreferencesUtil.getData(this, "token", "");
        id = (Integer) SharedPreferencesUtil.getData(this, "userId", -1);
        boolean isLogin = !TextUtils.isEmpty(token) && id > -1;
        return isLogin;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 2000:
                int id = data.getIntExtra("id", -1);
                String email = data.getStringExtra("email");
                String token = data.getStringExtra("token");
                String nick = data.getStringExtra("nick");
                User user = new User(id, email, token, nick, null);
                Message message = new Message();
                message.obj = user;
                message.what = 0;
                mHandler.sendMessage(message);
                break;
            case 1000:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 版本信息解析
     *
     * @param json
     * @return
     */
    private int parseJosn(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            serverName = jsonObject.optString("app_version");
            serverCode = jsonObject.optInt("code_version");
            serverUrl = jsonObject.optString("package_url");
            ToolUtils.setLog(serverName);
            ToolUtils.setLog("" + serverCode);
            ToolUtils.setLog(serverUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverCode;
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void logout(Fragment fragment) {
        popToStack(fragment);
        if (utilityFragment == null) {
            utilityFragment = HomeFragment.newInstance(ZhaiDou.HOME_PAGE_URL, ZhaiDou.ListType.HOME.toString());
        }
        selectFragment(currentFragment, utilityFragment);
        setButton(homeButton);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager manager = getSupportFragmentManager();
        int num = manager.getBackStackEntryCount();
        List<Fragment> fragments = manager.getFragments();
        //当分类显示时候，返回先隐藏
        for (Fragment fragment : fragments) {
            if (fragment instanceof HomeFragment) {
                Fragment homeCategoryFragment = fragment.getChildFragmentManager().findFragmentByTag(HomeCategoryFragment.class.getSimpleName());
                if (!homeCategoryFragment.isHidden()) {
                    ((HomeFragment) fragment).getHomeCategory();
                    fragment.getChildFragmentManager().beginTransaction().hide(homeCategoryFragment).commit();
                    return true;
                }
            }
        }
        if (num == 0) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - mTime) > 2000) {
                    Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                    mTime = System.currentTimeMillis();
                } else {
                    finish();
                }
                return true;
            }
        } else {
            if (fragments.size() > 0) {
                Fragment shopPaymentSuccessFragmen = manager.findFragmentByTag(ShopPaymentSuccessFragment.class.getSimpleName());
                Fragment shopPaymentFailFragment = manager.findFragmentByTag(ShopPaymentFailFragment.class.getSimpleName());
                Fragment shopPaymentFragment = manager.findFragmentByTag(ShopPaymentFragment.class.getSimpleName());
                if ((shopPaymentSuccessFragmen != null && shopPaymentSuccessFragmen instanceof ShopPaymentSuccessFragment)) {
                    //ShopPaymentSuccessFragment关闭
                    popToStack(shopPaymentSuccessFragmen);
                    return true;
                } else if (shopPaymentFailFragment != null && shopPaymentFailFragment instanceof ShopPaymentFailFragment) {
                    //ShopPaymentFailFragment关闭
                    popToStack(shopPaymentFailFragment);
                    return true;
                } else if (shopPaymentFragment != null && shopPaymentFragment instanceof ShopPaymentFragment) {
                    //ShopPaymentFragment返回弹出提示
                    BackPaymentDialog(shopPaymentFragment);
                    return true;
                }

            }

        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 收银台返回弹窗处理
     *
     * @param shopPaymentFragment
     */
    private void BackPaymentDialog(final Fragment shopPaymentFragment) {
        final Dialog dialog = new Dialog(this, R.style.custom_dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_collect_hint, null);
        TextView textView = (TextView) dialogView.findViewById(R.id.tv_msg);
        textView.setText("确认要放弃支付?");
        TextView cancelTv = (TextView) dialogView.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        TextView okTv = (TextView) dialogView.findViewById(R.id.okTv);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                popToStack(shopPaymentFragment);
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.addContentView(dialogView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();
    }

    public void toHomeFragment() {
        if (currentFragment instanceof HomeFragment) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.show(currentFragment).commit();
        } else {
            if (utilityFragment != null)
                selectFragment(currentFragment, utilityFragment);
            setButton(homeButton);
        }
    }

    /**
     * 清除除开首页的全部fragment
     */
    public void allfragment() {
        FragmentManager manager = getSupportFragmentManager();
        List<Fragment> fragments = manager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof HomeFragment || fragment instanceof PersonalFragment || fragment instanceof StrategyFragment || fragment instanceof CategoryFragment1 || fragment instanceof DiyFragment) {
            } else {
                manager.popBackStack();
                manager.beginTransaction().remove(fragment).commit();
            }
        }
    }

    public void hideTip(int v) {
        iv_dot.setVisibility(v);
    }

    private void FetchCityData() {
        JsonObjectRequest request = new JsonObjectRequest(ZhaiDou.ORDER_ADDRESS_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    JSONArray providerArr = jsonObject.optJSONArray("providers");
                    for (int i = 0; i < providerArr.length(); i++) {
                        JSONObject provinceObj = providerArr.optJSONObject(i);
                        int provinceId = provinceObj.optInt("id");
                        String provinceName = provinceObj.optString("name");
                        Province province = new Province();
                        province.setId(provinceId);
                        province.setName(provinceName);
                        List<City> cityList = new ArrayList<City>();
                        JSONArray cityArr = provinceObj.optJSONArray("cities");
                        if (cityArr != null && cityArr.length() > 0) {
                            for (int k = 0; k < cityArr.length(); k++) {
                                JSONObject cityObj = cityArr.optJSONObject(k);
                                int cityId = cityObj.optInt("id");
                                String cityName = cityObj.optString("name");
                                JSONArray areaArr = cityObj.optJSONArray("children");
                                City city = new City();
                                city.setId(cityId);
                                city.setName(cityName);
                                List<Area> areaList = new ArrayList<Area>();
                                if (areaArr != null && areaArr.length() > 0) {
                                    for (int j = 0; j < areaArr.length(); j++) {
                                        JSONObject areaObj = areaArr.optJSONObject(j);
                                        int areaId = areaObj.optInt("id");
                                        String areaName = areaObj.optString("name");
                                        int areaPrice = areaObj.optInt("price");
                                        Area area = new Area();
                                        area.setId(areaId);
                                        area.setName(areaName);
                                        area.setPrice(areaPrice);
                                        areaList.add(area);
                                    }
                                    city.setAreas(areaList);
                                    cityList.add(city);
                                }

                            }
                        }
                        province.setCityList(cityList);
                        provinceList.add(province);
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
        mRequestQueue.add(request);
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
