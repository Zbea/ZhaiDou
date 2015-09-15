package com.zhaidou.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.fragments.ContainerFragment;
import com.zhaidou.fragments.DiyCategoryFragment;
import com.zhaidou.fragments.DiyDetailFragment;
import com.zhaidou.fragments.DrawerFragment;

public class DiyActivity extends FragmentActivity implements ContainerFragment.OnFragmentInteractionListener,
        DrawerFragment.OnFragmentInteractionListener,DiyCategoryFragment.OnFragmentInteractionListener,
        DiyDetailFragment.OnFragmentInteractionListener{

    private DrawerLayout mDrawerLayout;
    private ContainerFragment mContainerFragment;
    private DrawerFragment mDrawerFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        if (mContainerFragment==null){
            mContainerFragment=ContainerFragment.newInstance("container","container");
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame,mContainerFragment,
                    ContainerFragment.TAG).commit();
        }
        initDrawerLayout();

    }
    private void initDrawerLayout() {
        if (mDrawerFragment==null){
            mDrawerFragment=DrawerFragment.newInstance("drawer","drawer");
            getSupportFragmentManager().beginTransaction().replace(R.id.right_drawer,mDrawerFragment,
                    DrawerFragment.TAG).commit();
        }
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
    public void onFragmentInteraction(Uri uri) {
        Log.i("onFragmentInteraction--->",uri.toString());
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
