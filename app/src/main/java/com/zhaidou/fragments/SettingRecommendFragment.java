package com.zhaidou.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.base.BaseListAdapter;
import com.zhaidou.base.ViewHolder;
import com.zhaidou.model.GoodDetail;
import com.zhaidou.model.GoodInfo;
import com.zhaidou.utils.PixelUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by roy on 15/8/28.
 */
public class SettingRecommendFragment extends BaseFragment {
    private static final String DATA = "page";
    private static final String INDEX = "index";

    private View mView;

    private String mPage;
    private String mIndex;
    private Context mContext;
    private ListView mListView;
    private List<GoodInfo> goodInfos;
    private RequestQueue mRequestQueue;
    private static final int UPDATE_GOOD_INFO = 0;
    private LinearLayout mImageContainer;
    private LinearLayout mDetailContainer;




    public static SettingRecommendFragment newInstance(String page, String index) {
        SettingRecommendFragment fragment = new SettingRecommendFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, page);
        args.putSerializable(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingRecommendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(DATA);
            mIndex = getArguments().getString(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mView == null) {
            mView = inflater.inflate(R.layout.setting_recommend_page, container, false);
            mContext = getActivity();
            initView();
        }
        //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }

        return mView;
    }


    private void initView() {


    }




}
