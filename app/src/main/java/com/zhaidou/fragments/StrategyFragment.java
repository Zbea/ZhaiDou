package com.zhaidou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.zhaidou.R;
import com.zhaidou.ZhaiDou;
import com.zhaidou.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;


public class StrategyFragment extends BaseFragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private WebView webView;
    private Dialog loading;
    private TextView livingRoomButton;
    private TextView entirePartButton;

    private ViewPager viewPager;
    private Fragment beautyHomeFragment;

    private List<View> views;

    private static final String LIVING_ROOM_TAG = "1";
    private static final String ENTIRE_PART_TAG = "2";

    private static final String LIVING_ROOM_URL = "http://buy.zhaidou.com/?zdclient=ios&tag=006&count=10";
    private static final String ENTIRE_PART_URL = "http://buy.zhaidou.com/gl.html";

    private TextView lastButton;
    private OnFragmentInteractionListener mListener;

    public StrategyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = new ArrayList<View>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_strategy, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.strategy_pager);

        views.add(view.inflate(getActivity(), R.layout.beauty_home, null));
        views.add(view.inflate(getActivity(), R.layout.whole_projects, null));

        viewPager.setAdapter(new MyFragmentAdapter(getChildFragmentManager()));//new MyPagerAdapter(views));

        livingRoomButton = (TextView) view.findViewById(R.id.living_room);
        entirePartButton = (TextView) view.findViewById(R.id.entire_part);
        livingRoomButton.setSelected(true);
        lastButton = livingRoomButton;

        livingRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
                lastButton.setSelected(false);
                lastButton = livingRoomButton;
                lastButton.setSelected(true);
            }
        });

        entirePartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                lastButton.setSelected(false);
                lastButton = entirePartButton;
                lastButton.setSelected(true);
            }
        });

        viewPager.setCurrentItem(0);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                lastButton.setSelected(false);
                switch (position) {
                    case 0: {
                        lastButton = livingRoomButton;
                        break;
                    }

                    case 1: {
                        lastButton = entirePartButton;
                        break;
                    }
                }
                lastButton.setSelected(true);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void onClick_Event(View view) {
        Button btn = (Button) view;
        if (lastButton != null) {
            lastButton.setSelected(false);
        }
        String tag = (String) btn.getTag();

        if (LIVING_ROOM_TAG.equals(tag)) {
            viewPager.setCurrentItem(0);
        } else if (ENTIRE_PART_TAG.equals(tag)) {
            viewPager.setCurrentItem(1);
        }

        lastButton = btn;
        lastButton.setSelected(true);
    }


    private class MyFragmentAdapter extends FragmentPagerAdapter {
        public MyFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("HAHAHHAHHA", "swip?");
            switch (position) {
                case 0: {
                    return ElementListFragment.newInstance("http://buy.zhaidou.com/?zdclient=ios&tag=006&count=10&json=1", ZhaiDou.ListType.TAG.toString());
                }
                case 1: {
                    return WebViewFragment.newInstance("http://buy.zhaidou.com/gl.html",false);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    private class MyPagerAdapter extends PagerAdapter {
        private List<View> mListView;

        private MyPagerAdapter(List<View> list) {
            this.mListView= list;
        }

        //销毁position位置的界面
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewGroup)arg0).removeView(mListView.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {

        }

        public int getCount() {
            return mListView.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewGroup)arg0).addView(mListView.get(arg1), 0);
            return mListView.get(arg1);
        }

        // 判断是否由对象生成界面
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==(arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mContext.getResources().getString(R.string.title_beauty));
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mContext.getResources().getString(R.string.title_beauty));
    }
}
