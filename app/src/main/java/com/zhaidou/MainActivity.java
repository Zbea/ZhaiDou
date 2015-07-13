package com.zhaidou;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.CallbackContext;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.zhaidou.base.BaseActivity;
import com.zhaidou.fragments.CategoryFragment1;
import com.zhaidou.fragments.DiyFragment;
import com.zhaidou.fragments.ElementListFragment;
import com.zhaidou.fragments.HomeFragment;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.PersonalFragment;
import com.zhaidou.fragments.PersonalMainFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.fragments.StrategyFragment;
import com.zhaidou.fragments.WebViewFragment;
import com.zhaidou.utils.SharedPreferencesUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 */
public class MainActivity extends BaseActivity implements DiyFragment.OnFragmentInteractionListener,
        StrategyFragment.OnFragmentInteractionListener,
        ElementListFragment.OnFragmentInteractionListener,WebViewFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,CategoryFragment1.OnFragmentInteractionListener,
        PersonalMainFragment.OnFragmentInteractionListener,RegisterFragment.RegisterOrLoginListener{

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

    private String token;
    private int id;

    boolean isFromCamera = false;// 区分拍照旋转
    int degree = 0;
    public String filePath = "";

    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);
        init();
        AlibabaSDK.asyncInit(this,new InitResultCallback() {
            @Override
            public void onSuccess() {
                Log.i("onSuccess---->","初始化成功");
                Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.i("onFailure---->","初始化异常--"+s);
                Toast.makeText(MainActivity.this, "初始化异常"+s, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        initComponents();
    }

    public void init() {

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
                Log.i("---personalButton-->",checkLogin()+"");
                if (!checkLogin()){
                    mLoginFragment=LoginFragment.newInstance("","");
                    mLoginFragment.setRegisterOrLoginListener(MainActivity.this);
                    navigationToFragment(mLoginFragment);
                    mChildContainer.setVisibility(View.VISIBLE);
                }else {
                    if (persoanlFragment==null){
                        persoanlFragment= PersonalFragment.newInstance("", "");
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
        CallbackContext.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void logout(Fragment fragment){
        popToStack(fragment);
        if (utilityFragment==null){
            utilityFragment= HomeFragment.newInstance(ZhaiDou.HOME_PAGE_URL, ZhaiDou.ListType.HOME.toString());
        }
        selectFragment(currentFragment,utilityFragment);
        setButton(homeButton);
    }
}
