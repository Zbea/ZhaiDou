package com.zhaidou;

import com.zhaidou.activities.CategoriesActivity;
import com.zhaidou.activities.DiyActivity;
import com.zhaidou.activities.HomeActivity;
import com.zhaidou.activities.MeActivity;
import com.zhaidou.activities.StrategyActivity;
import com.zhaidou.graphics.TabBitmap;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;


public class MainActivity extends ActivityGroup {

    private static final String TAG_1 = "tab1";
    private static final String TAG_2 = "tab2";
    private static final String TAG_3 = "tab3";
    private static final String TAG_4 = "tab4";
    private static final String TAG_5 = "tab5";

    TabHost mTabHost;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTabs();
    }

    private void setTabs() {
        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup(this.getLocalActivityManager());

        addTab("首页", TAG_1, R.drawable.home_icon, new Intent(this, HomeActivity.class));
        addTab("攻略", TAG_2, R.drawable.gl_icon, new Intent(this, StrategyActivity.class));
        addTab("分类", TAG_3, R.drawable.category_icon, new Intent(this, CategoriesActivity.class));
        addTab("DIY", TAG_4, R.drawable.diy_icon,  new Intent(this, DiyActivity.class));
        //addTab("我", TAG_5, R.drawable.me_icon, new Intent(this, MeActivity.class));
    }

    private Drawable createTabDrawable(int resId) {
        Resources res = getResources();
        StateListDrawable states = new StateListDrawable();

        final Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;

        Bitmap icon = BitmapFactory.decodeResource(res, resId, options);

        Bitmap unselected = TabBitmap.createUnselectedBitmap(res, icon);
        Bitmap selected = TabBitmap.createSelectedBitmap(res, icon);

        icon.recycle();

        states.addState(new int[] { android.R.attr.state_selected }, new BitmapDrawable(res, selected));
        states.addState(new int[] { android.R.attr.state_enabled }, new BitmapDrawable(res, unselected));

        return states;
    }

    private View createTabIndicator(String label, int resId) {//Drawable drawable) {
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, mTabHost.getTabWidget(), false);

        TextView txtTitle = (TextView) tabIndicator.findViewById(R.id.text_view_tab_title);
        txtTitle.setText(label);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) txtTitle.getLayoutParams();
        txtTitle.setLayoutParams(params);

        ImageView imgIcon = (ImageView) tabIndicator.findViewById(R.id.image_view_tab_icon);
        imgIcon.setImageDrawable(getResources().getDrawable(resId));

        return tabIndicator;
    }

    private void addTab(String label, String tag, int resId, Intent intent) {
        TabHost.TabSpec spec = mTabHost.newTabSpec(tag);
        spec.setIndicator(createTabIndicator(label, resId));
        spec.setContent(intent);
        mTabHost.addTab(spec);
    }

    private void addTab(String label, String tag, int resId, int id) {//Drawable drawable, int id) {
        TabHost.TabSpec spec = mTabHost.newTabSpec(tag);
        spec.setIndicator(createTabIndicator(label, resId));
        spec.setContent(id);
        mTabHost.addTab(spec);
    }
}
