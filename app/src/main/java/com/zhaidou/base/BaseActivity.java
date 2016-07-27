package com.zhaidou.base;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.fragments.MainCategoryFragment;
import com.zhaidou.fragments.MainHomeFragment;
import com.zhaidou.fragments.MainMagicFragment;
import com.zhaidou.fragments.MainPersonalFragment;
import com.zhaidou.fragments.RegisterFragment;
import com.zhaidou.fragments.ShopCartFragment;
import com.zhaidou.fragments.ShopPaymentFailFragment;
import com.zhaidou.fragments.ShopPaymentSuccessFragment;
import com.zhaidou.model.User;
import com.zhaidou.utils.SharedPreferencesUtil;

import java.util.List;

/**
 * Created by wangclark on 15/7/3.
 */
public class BaseActivity extends FragmentActivity implements RegisterFragment.RegisterOrLoginListener {
    protected FrameLayout mChildContainer;
    protected MainPersonalFragment persoanlFragment;
    protected ImageButton personalButton;
    protected Fragment currentFragment;
    protected WebView webView;
    protected String from;
    protected User user;
    protected InputMethodManager inputMethodManager;

    public void navigationToFragment(Fragment fragment) {
        if (fragment != null && fragment instanceof RegisterFragment) {
            RegisterFragment registerFragment = (RegisterFragment) fragment;
            registerFragment.setRegisterOrLoginListener(this);
        }
        if ("MainActivity".equalsIgnoreCase(((Object) this).getClass().getSimpleName())||
                "WebViewActivity".equalsIgnoreCase(((Object) this).getClass().getSimpleName())) {
            mChildContainer.setVisibility(View.VISIBLE);
        }
        //.setCustomAnimations(R.anim.page_enter_into_the,R.anim.page_enter_out_the,R.anim.page_out_into_the,R.anim.page_out_out_the)
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_child_container, fragment, ((Object) fragment).getClass().getSimpleName())
                .addToBackStack(null).commitAllowingStateLoss();
    }

    public void navigationToFragmentWithAnim(Fragment fragment) {
        if (fragment != null && fragment instanceof RegisterFragment) {
            RegisterFragment registerFragment = (RegisterFragment) fragment;
            registerFragment.setRegisterOrLoginListener(this);
        }
        if ("MainActivity".equalsIgnoreCase(((Object) this).getClass().getSimpleName())||
                "WebViewActivity".equalsIgnoreCase(((Object) this).getClass().getSimpleName())) {
            mChildContainer.setVisibility(View.VISIBLE);
        }
        //.setCustomAnimations(R.anim.page_enter_into_the,R.anim.page_enter_out_the,R.anim.page_out_into_the,R.anim.page_out_out_the)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_child_container, fragment, ((Object) fragment).getClass().getSimpleName())
                .addToBackStack(null).commitAllowingStateLoss();
    }
    public void popToStack(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        //.setCustomAnimations(R.anim.page_out_into_the,R.anim.page_out_out_the)
        fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        fragmentManager.popBackStack();
        if (fragment instanceof ShopPaymentFailFragment || fragment instanceof ShopPaymentSuccessFragment)
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();

    }

    @Override
    public void onRegisterOrLoginSuccess(User user, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        SharedPreferencesUtil.saveUser(this, user);

        if (fragment instanceof RegisterFragment) {
            popToStack(fragment);
        }
        if ("MainActivity".equalsIgnoreCase(((Object)this).getClass().getSimpleName())) {
            if (persoanlFragment == null) {
                persoanlFragment = MainPersonalFragment.newInstance("", "");
                persoanlFragment.onAttach(this);
            } else {
                persoanlFragment.refreshData(this);
            }
            MainActivity mainActivity = (MainActivity) this;
            mainActivity.selectFragment(currentFragment, persoanlFragment);
            mainActivity.setButton(personalButton);
        } else if ("ItemDetailActivity".equalsIgnoreCase(((Object)this).getClass().getSimpleName())) {
            this.user = user;
            if ("lottery".equalsIgnoreCase(from)) {
                return;
            } else if ("product".equalsIgnoreCase(from)) {
                return;
            }
            fragmentManager.beginTransaction().hide(fragment).commit();
        }
    }

    public String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
    public void hideInputMethod(){
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        System.out.println("inputMethodManager.isActive() = " + inputMethodManager.isActive());
        if (inputMethodManager.isActive())
            inputMethodManager.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);
    }

    /**
     * 清除除开首页的全部fragment
     */
    public void allfragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof MainHomeFragment || fragment instanceof MainPersonalFragment || fragment instanceof MainMagicFragment || fragment instanceof MainCategoryFragment || fragment instanceof ShopCartFragment) {
            } else {
                popToStack(fragment);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments=fm.getFragments();
        if (fragments!=null)
        {
            for(Fragment frag:fragments)
            {
                if (frag!=null)
                {
                    frag.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    /**
     * 递归调用，对所有子Fragement生效
     *
     * @param frag
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void handleResult(Fragment frag, int requestCode, int resultCode,
                              Intent data) {
        frag.onActivityResult(requestCode & 0xffff, resultCode, data);
        List<Fragment> frags = frag.getChildFragmentManager().getFragments();
        if (frags != null) {
            for (Fragment f : frags) {
                if (f != null)
                    handleResult(f, requestCode, resultCode, data);
            }
        }
    }

}
