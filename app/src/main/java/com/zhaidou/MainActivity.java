package com.zhaidou;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.CallbackContext;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.zhaidou.activities.LoginActivity;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.dialog.CustomVersionUpdateDialog;
import com.zhaidou.fragments.CategoryFragment1;
import com.zhaidou.fragments.DiyFragment;
import com.zhaidou.fragments.ElementListFragment;
import com.zhaidou.fragments.HomeFragment;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.PersonalFragment;
import com.zhaidou.fragments.PersonalMainFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.fragments.SettingFragment;
import com.zhaidou.fragments.ShopPaymentFragment;
import com.zhaidou.fragments.StrategyFragment;
import com.zhaidou.fragments.WebViewFragment;
import com.zhaidou.model.CartItem;
import com.zhaidou.model.User;
import com.zhaidou.sqlite.CreatCartDB;
import com.zhaidou.sqlite.CreatCartTools;
import com.zhaidou.utils.NetService;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 */
public class MainActivity extends BaseActivity implements DiyFragment.OnFragmentInteractionListener,
        StrategyFragment.OnFragmentInteractionListener,
        ElementListFragment.OnFragmentInteractionListener, WebViewFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener, CategoryFragment1.OnFragmentInteractionListener,
        PersonalMainFragment.OnFragmentInteractionListener, RegisterFragment.RegisterOrLoginListener {

    private Fragment utilityFragment;
    private Fragment beautyHomeFragment;
    private Fragment categoryFragment;
    private Fragment diyFragment;
//    private PersonalFragment persoanlFragment;
//    private Fragment currentFragment;

    private ImageButton homeButton;
    private ImageButton beautyButton;
    private ImageButton categoryButton;
    private ImageButton diyButton;
//    private ImageButton personalButton;

    private ImageButton lastButton;

    private TextView titleView;
    private LinearLayout mTabContainer;
//    private FrameLayout mChildContainer;
    private LoginFragment mLoginFragment;
    private ImageView iv_dot;

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
    private int serverCode;

    public static int num=0;
    private List<CartItem> items=new ArrayList<CartItem>();

    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action=intent.getAction();
            if (action.equals(ZhaiDou.IntentRefreshCartGoodsTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginTag))
            {
                initCartTips();
            }
            if (action.equals(ZhaiDou.IntentRefreshLoginExitTag))
            {
                initCartTips();
            }
            if (action.equalsIgnoreCase(ZhaiDou.BROADCAST_WXAPI_FILTER)) {
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                ShopPaymentFragment shopPaymentFragment=(ShopPaymentFragment)fragments.get(fragments.size()-1);
                shopPaymentFragment.handleWXPayResult(intent.getIntExtra("code", -1));
            }
        }
    };

    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    User user=(User)msg.obj;
                    if (persoanlFragment==null){
                        persoanlFragment= PersonalFragment.newInstance("", "");
                    }
                    else {
                    }
                    selectFragment(currentFragment, persoanlFragment);
                    setButton(personalButton);
                    break;

                case 1:
                    serverCode=parseJosn(msg.obj.toString());
                    if (serverCode>ZDApplication.localVersionCode)
                    {
                       CustomVersionUpdateDialog customVersionUpdateDialog=new CustomVersionUpdateDialog(mContext,serverName);
                       customVersionUpdateDialog.checkUpdateInfo();
                    }

                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);
        iv_dot=(ImageView)findViewById(R.id.iv_dot);
        mContext=this;
        initBroadcastReceiver();

        getVersionServer();


        init();
        AlibabaSDK.asyncInit(this,new InitResultCallback() {
            @Override
            public void onSuccess() {
                Log.i("onSuccess---->","初始化成功");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("onFailure---->","初始化异常--"+s);
            }
        });

        initComponents();
    }

    /**
     * 获取版本信息
     */
    private void getVersionServer()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String url=ZhaiDou.apkUpdateUrl;
                String result= NetService.getHttpService(url);
                if (result!=null)
                {
                    mHandler.obtainMessage(1,result).sendToTarget();
                }
            }
        }).start();
    }
    /**
     * 注册广播
     */
    private void initBroadcastReceiver()
    {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginExitTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshLoginTag);
        intentFilter.addAction(ZhaiDou.IntentRefreshCartGoodsTag);
        intentFilter.addAction(ZhaiDou.BROADCAST_WXAPI_FILTER);
        mContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void init()
    {
        creatCartDB=new CreatCartDB(mContext);
        initCartTips();
    }

    /**
     * 红色标识提示显示数量
     */
    private void initCartTips()
    {
        num=0;
        if (checkLogin())
        {
            getGoodsItems();
            for (int i = 0; i <items.size() ; i++)
            {
                if (items.get(i).isPublish.equals("false")&&items.get(i).isOver.equals("false"))
                {
                    num=num+items.get(i).num;
                }
            }
        }

    }

    /**
     * 获得当前userId的所有商品
     */
    private void getGoodsItems()
    {
        items.removeAll(items);
        //遍历获得这个当前uesrId的所有商品
        items = CreatCartTools.selectByAll(creatCartDB,id);

    }

    public void initComponents() {

        mTabContainer=(LinearLayout)findViewById(R.id.tab_container);
        mChildContainer=(FrameLayout)findViewById(R.id.fl_child_container);
        homeButton = (ImageButton) findViewById(R.id.tab_home);

        lastButton = homeButton;

        if (utilityFragment == null) {
//            utilityFragment = ElementListFragment.newInstance(ZhaiDou.HOME_PAGE_URL, ZhaiDou.ListType.HOME.toString());
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
//                    categoryFragment = CategoryFragment.newInstance("haha", "haha");
                    categoryFragment= CategoryFragment1.newInstance("","");
                }


                selectFragment(currentFragment, categoryFragment);
                setButton(view);
            }
        });

        diyButton = (ImageButton) findViewById(R.id.tab_diy);
        diyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                titleView.setText("DIY");
                if (diyFragment == null) {
                    diyFragment = DiyFragment.newInstance("haha", "haha");
                }
//                startActivity(new Intent(MainActivity.this, DiyActivity.class));
//                startActivity(new Intent(MainActivity.this,SearchActivity.class));
//                return;


                selectFragment(currentFragment, diyFragment);

                setButton(view);
            }
        });

        personalButton=(ImageButton)findViewById(R.id.tab_personal);
        personalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkLogin()){
//                    mLoginFragment=LoginFragment.newInstance("","");
//                    mLoginFragment.setRegisterOrLoginListener(MainActivity.this);
//                    navigationToFragment(mLoginFragment);
//                    mChildContainer.setVisibility(View.VISIBLE);
                    Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivityForResult(intent, 10000);
                }else {
                    if (persoanlFragment==null){
//                        persoanlFragment= PersonalFragment.newInstance("", "");
                        persoanlFragment= PersonalFragment.newInstance("","");
                    }
                    selectFragment(currentFragment,persoanlFragment);
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


    public void toggleTabContainer(){
        mTabContainer.setVisibility(mTabContainer.isShown()?View.GONE:View.VISIBLE);
    }

    public void toggleTabContainer(int visible){
        mTabContainer.setVisibility(visible);
    }

    public boolean checkLogin(){
        token=(String)SharedPreferencesUtil.getData(this,"token","");
        id=(Integer)SharedPreferencesUtil.getData(this,"userId",-1);
        boolean isLogin=!TextUtils.isEmpty(token)&&id>-1;
        return isLogin;
    }
//
//    public void navigationToFragment(Fragment fragment){
//        if (fragment!=null&&fragment instanceof RegisterFragment){
//            RegisterFragment registerFragment=(RegisterFragment)fragment;
//            registerFragment.setRegisterOrLoginListener(this);
//        }
//        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container,fragment,fragment.getClass().getSimpleName())
//                .addToBackStack(null).commit();
//        mChildContainer.setVisibility(View.VISIBLE);
//    }

//    @Override
//    public void onRegisterOrLoginSuccess(User user,Fragment fragment) {
////        Log.i("MainActivity---->",user.toString());
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.popBackStack();
//        SharedPreferencesUtil.saveUser(this,user);
//        if (fragment instanceof RegisterFragment){
//            popToStack(fragment);
//        }
//        if (persoanlFragment==null){
//            persoanlFragment= PersonalFragment.newInstance("", "");
//        }
//        selectFragment(currentFragment,persoanlFragment);
//        setButton(personalButton);
//
//        fragmentManager.beginTransaction().hide(fragment).commit();
//    }
//    public void popToStack(Fragment fragment){
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        Log.i("childFragmentManager--->", fragmentManager.getBackStackEntryCount()+"");
//        fragmentManager.popBackStack();
//        fragmentManager.beginTransaction().remove(fragment).commit();
//
//        Log.i("fragment---->",fragment.getClass().getSimpleName());
////        if (fragment!=null&&fragment instanceof LoginFragment)
////            mChildContainer.setVisibility(View.GONE);
//    }
//
//    private void saveUserToSP(User user){
//        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.putInt("userId",user.getId());
//        editor.putString("email", user.getEmail());
//        editor.putString("token",user.getAuthentication_token());
//        editor.putString("avatar",user.getAvatar());
//        editor.putString("nickName",user.getNickName());
//        editor.commit();
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.popBackStack();
//    }

    public interface PhotoSelectListener{
        public void onPhotoSelect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 2000:
                int id=data.getIntExtra("id",-1);
                String email=data.getStringExtra("email");
                String token=data.getStringExtra("token");
                String nick=data.getStringExtra("nick");
                User user=new User(id,email,token,nick,null);
                Message message=new Message();
                message.obj=user;
                message.what=0;
                mHandler.sendMessage(message);
                break;
            case 1000:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 版本信息解析
     * @param json
     * @return
     */
    private int parseJosn(String json)
    {
        try
        {
            JSONObject jsonObject=new JSONObject(json);
            serverName=jsonObject.optString("name");
            serverCode=jsonObject.optInt("code");
            serverInfo=jsonObject.optString("info");
            ToolUtils.setLog(serverName);
            ToolUtils.setLog(""+serverCode);
            ToolUtils.setLog(serverInfo);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return serverCode;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void logout(Fragment fragment){
        popToStack(fragment);
        if (utilityFragment==null){
            utilityFragment= HomeFragment.newInstance(ZhaiDou.HOME_PAGE_URL, ZhaiDou.ListType.HOME.toString());
        }
        selectFragment(currentFragment,utilityFragment);
        setButton(homeButton);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        FragmentManager manager=getSupportFragmentManager();
        int num=manager.getBackStackEntryCount();
        if (num==0)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK)
            {
                if ((System.currentTimeMillis()-mTime)>2000)
                {
                    Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                    mTime=System.currentTimeMillis();
                }
                else
                {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public void toHomeFragment(){
        selectFragment(currentFragment, utilityFragment);
        setButton(homeButton);
    }

    public void replaceFragment(Fragment fragment){
        SettingFragment settingFragment=SettingFragment.newInstance("","");
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container,settingFragment).addToBackStack(null).show(settingFragment).commit();
        mChildContainer.setVisibility(View.VISIBLE);
    }
    public void hideTip(int v){
        iv_dot.setVisibility(v);
    }
}
