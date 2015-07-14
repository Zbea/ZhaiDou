package com.zhaidou.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.PersonalFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.model.User;
import com.zhaidou.utils.SharedPreferencesUtil;

/**
 * Created by wangclark on 15/7/3.
 */
public class BaseActivity extends FragmentActivity implements RegisterFragment.RegisterOrLoginListener{
    protected FrameLayout mChildContainer;
    protected PersonalFragment persoanlFragment;
    protected ImageButton personalButton;
    protected Fragment currentFragment;
    protected WebView webView;

    public void navigationToFragment(Fragment fragment){
        if (fragment!=null&&fragment instanceof RegisterFragment){
            RegisterFragment registerFragment=(RegisterFragment)fragment;
            registerFragment.setRegisterOrLoginListener(this);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container,fragment,fragment.getClass().getSimpleName())
                .addToBackStack(null).commit();
        mChildContainer.setVisibility(View.VISIBLE);
    }

    public void popToStack(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.i("childFragmentManager--->", fragmentManager.getBackStackEntryCount() + "");
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().remove(fragment).commit();

        Log.i("fragment---->",fragment.getClass().getSimpleName());
        if (fragment!=null&&fragment instanceof LoginFragment)
            mChildContainer.setVisibility(View.GONE);
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        SharedPreferencesUtil.saveUser(this, user);
        if (fragment instanceof RegisterFragment){
            popToStack(fragment);
        }

        if ("MainActivity".equalsIgnoreCase(this.getClass().getSimpleName())){
            if (persoanlFragment==null){
                persoanlFragment= PersonalFragment.newInstance("", "");
            }
            MainActivity mainActivity = (MainActivity)this;
            mainActivity.selectFragment(currentFragment, persoanlFragment);
            mainActivity.setButton(personalButton);
            persoanlFragment.refreshData();
        }else if ("ItemDetailActivity".equalsIgnoreCase(this.getClass().getSimpleName())){
            webView.loadUrl("javascript:ReceiveUserInfo("+user.getId()+", '"+user.getAuthentication_token()+"')");
        }
        fragmentManager.beginTransaction().hide(fragment).commit();
    }
}