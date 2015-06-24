package com.zhaidou.fragments;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.TabPageIndicator;
import com.zhaidou.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PersonalFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TabPageIndicator mIndicator;
    private ViewPager mViewpager;

    private List<Fragment> mFragments;
    private CollectFragment mCollectFragment;
    private CollocationFragment mCollocationFragment;
    private SettingFragment mSettingFragment;
    private PersonalFragmentAdapter mAdapter;
    private TextView tv_setting;


    // TODO: Rename and change types and number of parameters
    public static PersonalFragment newInstance(String param1, String param2) {
        PersonalFragment fragment = new PersonalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public PersonalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_personal, container, false);
        mIndicator=(TabPageIndicator)view.findViewById(R.id.tab_personal);
        mViewpager=(ViewPager)view.findViewById(R.id.vp_personal);
        tv_setting=(TextView)view.findViewById(R.id.tv_setting);

        mFragments=new ArrayList<Fragment>();
        mCollectFragment =CollectFragment.newInstance("","");
        mCollocationFragment=CollocationFragment.newInstance("","");
        mSettingFragment=SettingFragment.newInstance("","");

        mFragments.add(mCollectFragment);
        mFragments.add(mCollocationFragment);

        mAdapter =new PersonalFragmentAdapter(getFragmentManager());
        mViewpager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewpager);
        Button login=(Button)view.findViewById(R.id.bt_login);
        login.setOnClickListener(this);
        tv_setting.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
//        LoginFragment fragment = LoginFragment.newInstance("","");
//        ((PersonalMainFragment)getParentFragment()).addToStack(fragment);
        switch (view.getId()){
            case R.id.bt_login:
                ((PersonalMainFragment)getParentFragment()).toggleTabContainer();
                break;
            case R.id.tv_setting:
                ((PersonalMainFragment)getParentFragment()).addToStack(mSettingFragment);
                break;
        }

    }
    private class PersonalFragmentAdapter extends FragmentPagerAdapter {
        public PersonalFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position==0)
                return "收藏";
            return "豆搭";
        }
    }
}
