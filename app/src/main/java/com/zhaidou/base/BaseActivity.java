package com.zhaidou.base;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.fragments.LoginFragment;
import com.zhaidou.fragments.PersonalFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.fragments.ShopPaymentFailFragment;
import com.zhaidou.fragments.ShopPaymentSuccessFragment;
import com.zhaidou.model.User;
import com.zhaidou.utils.SharedPreferencesUtil;
import com.zhaidou.utils.ToolUtils;

/**
 * Created by wangclark on 15/7/3.
 */
public class BaseActivity extends FragmentActivity implements RegisterFragment.RegisterOrLoginListener {
    protected FrameLayout mChildContainer;
    protected PersonalFragment persoanlFragment;
    protected ImageButton personalButton;
    protected Fragment currentFragment;
    protected WebView webView;
    protected String from;
    protected User user;

    public void navigationToFragment(Fragment fragment) {
        if (fragment != null && fragment instanceof RegisterFragment) {
            RegisterFragment registerFragment = (RegisterFragment) fragment;
            registerFragment.setRegisterOrLoginListener(this);
        }
        if ("MainActivity".equalsIgnoreCase(this.getClass().getSimpleName())) {
            mChildContainer.setVisibility(View.VISIBLE);
        }
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.page_enter_into_the,R.anim.page_enter_out_the,R.anim.page_out_into_the,R.anim.page_out_out_the).replace(R.id.fl_child_container, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    public void popToStack(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.page_out_into_the,R.anim.page_out_out_the).remove(fragment).commitAllowingStateLoss();
        fragmentManager.popBackStack();
        if (fragment instanceof ShopPaymentFailFragment || fragment instanceof ShopPaymentSuccessFragment)
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();

        Log.i("fragment---->", fragment.getClass().getSimpleName());
        if (fragment != null && fragment instanceof LoginFragment) {
            if ("MainActivity".equalsIgnoreCase(this.getClass().getSimpleName())) {
                mChildContainer.setVisibility(View.GONE);
            }
        }
//            mChildContainer.setVisibility(View.GONE);
    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        SharedPreferencesUtil.saveUser(this, user);

        if (fragment instanceof RegisterFragment) {
            popToStack(fragment);
        }
        if ("MainActivity".equalsIgnoreCase(this.getClass().getSimpleName())) {
            if (persoanlFragment == null) {
                persoanlFragment = PersonalFragment.newInstance("", "");
                persoanlFragment.onAttach(this);
            } else {
                persoanlFragment.refreshData(this);
            }
            MainActivity mainActivity = (MainActivity) this;
            mainActivity.selectFragment(currentFragment, persoanlFragment);
            mainActivity.setButton(personalButton);
        } else if ("ItemDetailActivity".equalsIgnoreCase(this.getClass().getSimpleName())) {
            Log.i("ItemDetailActivity-------------->", this.getClass().getSimpleName());
            this.user = user;
            Log.i("from-------------->", from);
            if ("lottery".equalsIgnoreCase(from)) {
                return;
//                Log.i("onRegisterOrLoginSuccess--lottery----------->", "onPageFinished" + "------" + user.getAuthentication_token());
//                webView.loadUrl("javascript:ReceiveUserInfo(" + user.getId() + ", '" + user.getAuthentication_token() + "','" + getDeviceId() + "','" + user.getNickName() + "')");
            } else if ("product".equalsIgnoreCase(from)) {
                return;
//                webView.loadUrl("javascript:ReceiveUserInfo(" + user.getId() + ", '" + user.getAuthentication_token() + "')");
            }
            fragmentManager.beginTransaction().hide(fragment).commit();
        }
    }

    public String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
}
