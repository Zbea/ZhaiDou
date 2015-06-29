package com.zhaidou;

import com.zhaidou.activities.SearchActivity;
import com.zhaidou.fragments.CategoryFragment;
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
import com.zhaidou.model.User;
import com.zhaidou.utils.PhotoUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 */
public class MainActivity extends FragmentActivity implements DiyFragment.OnFragmentInteractionListener,
        CategoryFragment.OnFragmentInteractionListener, StrategyFragment.OnFragmentInteractionListener,
        ElementListFragment.OnFragmentInteractionListener, WebViewFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,CategoryFragment1.OnFragmentInteractionListener,
        PersonalMainFragment.OnFragmentInteractionListener,RegisterFragment.RegisterOrLoginListener{

    private Fragment utilityFragment;
    private Fragment beautyHomeFragment;
    private Fragment categoryFragment;
    private Fragment diyFragment;
    private PersonalFragment persoanlFragment;
    private Fragment currentFragment;

    private ImageButton homeButton;
    private ImageButton beautyButton;
    private ImageButton categoryButton;
    private ImageButton diyButton;
    private ImageButton personalButton;

    private ImageButton lastButton;

    private TextView titleView;
    private LinearLayout mTabContainer;
    private FrameLayout mChildContainer;
    private SharedPreferences mSharedPreferences;
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
        mSharedPreferences=getSharedPreferences("zhaidou", Context.MODE_PRIVATE);
        init();

        initComponents();
    }

    public void init() {

    }

    public void initComponents() {
//        View actionBarView = getLayoutInflater().inflate(R.layout.custom_actionbar, null);
//        titleView = (TextView) actionBarView.findViewById(R.id.custom_actionbar_title);
//        titleView.setText("每日精选功能美物");
//
//        getActionBar().setDisplayShowHomeEnabled(false);
//        getActionBar().setDisplayShowTitleEnabled(false);
//        getActionBar().setDisplayShowCustomEnabled(true);
//        getActionBar().setCustomView(actionBarView);

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
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
                return;


//                selectFragment(currentFragment, diyFragment);
//
//                setButton(view);
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

    public void onClick_Event(View v) {
        if (categoryFragment != null) {
            CategoryFragment fragment = (CategoryFragment) categoryFragment;
            fragment.onClick(v);
        }
    }

    public void onAllClick(View view){
        Log.i("onAllClick","onAllClick--------------->" + ((TextView) view).getText().toString());
    }

    public void toggleTabContainer(){
        mTabContainer.setVisibility(mTabContainer.isShown()?View.GONE:View.VISIBLE);
    }

    public void toggleTabContainer(int visible){
        mTabContainer.setVisibility(visible);
    }

    public boolean checkLogin(){
        token=mSharedPreferences.getString("token", null);
        id=mSharedPreferences.getInt("userId",-1);
//        Log.i("token---------->",token);
//        Log.i("id------------>",id+"");
        boolean isLogin=token!=null&&id>-1;
        return isLogin;
    }

    public void navigationToFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container,fragment,fragment.getClass().getSimpleName())
                .addToBackStack(null).commit();
        mChildContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRegisterOrLoginSuccess(User user,Fragment fragment) {
        Log.i("MainActivity---->",user.toString());
        saveUserToSP(user);
//        Message message= new Message();
//        message.obj=fragment;
//        mHandler.sendMessage(message);

        if (persoanlFragment==null){
            persoanlFragment= PersonalFragment.newInstance("", "");
        }
        selectFragment(currentFragment,persoanlFragment);
        setButton(personalButton);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().hide(fragment).commit();

    }
    public void popToStack(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.i("childFragmentManager--->", fragmentManager.getBackStackEntryCount()+"");
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().remove(fragment).commit();

        Log.i("fragment---->",fragment.getClass().getSimpleName());
        if (fragment!=null&&fragment instanceof LoginFragment)
            mChildContainer.setVisibility(View.GONE);
    }

    private void saveUserToSP(User user){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("userId",user.getId());
        editor.putString("email", user.getEmail());
        editor.putString("token",user.getAuthentication_token());
        editor.putString("avatar",user.getAvatar());
        editor.putString("nickName",user.getNickName());
        editor.commit();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
    }

    public interface PhotoSelectListener{
        public void onPhotoSelect();
    }
}
