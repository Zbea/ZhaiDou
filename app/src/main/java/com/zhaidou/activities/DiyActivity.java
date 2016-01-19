package com.zhaidou.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;

public class DiyActivity extends FragmentActivity
{

    private DrawerLayout mDrawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);

    }

    public void openDrawer(){
        mDrawerLayout.openDrawer(Gravity.RIGHT);
    }
    public void addToStack(Fragment fragment){
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.right_drawer,fragment,((Object) fragment).getClass().getSimpleName());
        transaction.addToBackStack(((Object) fragment).getClass().getSimpleName());
        transaction.commit();
    }
    public void popToStack(){
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
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
