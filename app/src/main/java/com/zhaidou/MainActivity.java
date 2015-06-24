package com.zhaidou;

import com.zhaidou.activities.SearchActivity;
import com.zhaidou.fragments.CategoryFragment;
import com.zhaidou.fragments.CategoryFragment1;
import com.zhaidou.fragments.DiyFragment;
import com.zhaidou.fragments.ElementListFragment;
import com.zhaidou.fragments.HomeFragment;
import com.zhaidou.fragments.PersonalMainFragment;
import com.zhaidou.fragments.StrategyFragment;
import com.zhaidou.fragments.WebViewFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 */
public class MainActivity extends FragmentActivity implements DiyFragment.OnFragmentInteractionListener,
        CategoryFragment.OnFragmentInteractionListener, StrategyFragment.OnFragmentInteractionListener,
        ElementListFragment.OnFragmentInteractionListener, WebViewFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,CategoryFragment1.OnFragmentInteractionListener,
        PersonalMainFragment.OnFragmentInteractionListener{

    private Fragment utilityFragment;
    private Fragment beautyHomeFragment;
    private Fragment categoryFragment;
    private Fragment diyFragment;
    private Fragment persoanlFragment;
    private Fragment currentFragment;

    private ImageButton homeButton;
    private ImageButton beautyButton;
    private ImageButton categoryButton;
    private ImageButton diyButton;
    private ImageButton personalButton;

    private ImageButton lastButton;

    private TextView titleView;
    private LinearLayout mTabContainer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);

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
                if (persoanlFragment==null){
                    persoanlFragment= PersonalMainFragment.newInstance("", "");
                }
                selectFragment(currentFragment,persoanlFragment);
                setButton(view);
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
}
