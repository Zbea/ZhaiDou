package com.zhaidou.fragments;



import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pulltorefresh.PullToRefreshBase;
import com.pulltorefresh.PullToRefreshScrollView;
import com.zhaidou.MainActivity;
import com.zhaidou.R;
import com.zhaidou.base.BaseFragment;
import com.zhaidou.view.ListViewForScrollView;
import com.zhaidou.view.TypeFaceTextView;


/**
 * Created by roy on 15/7/20.
 */
public class ShopSpecialFragment extends BaseFragment {
    private static final String PAGE = "page";
    private static final String INDEX = "index";

    private String mPage;
    private int mIndex;
    private View mView;
    private Context mContext;

    private ImageView adIv;
    private LinearLayout backBtn;
    private TypeFaceTextView titleTv;
    private PullToRefreshScrollView mScrollView;
    private ListViewForScrollView mListView;

    /**
     * 下拉刷新
     */
    private PullToRefreshBase.OnRefreshListener2 refreshListener=new PullToRefreshBase.OnRefreshListener2()
    {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView)
        {

        }
        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView)
        {

        }
    };

    /**
     * 点击事件
     */
    private View.OnClickListener onClickListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.back_btn:
                    ((MainActivity)getActivity()).popToStack(ShopSpecialFragment.this);
                break;
            }
        }
    };

    public static ShopSpecialFragment newInstance(String page, int index) {
        ShopSpecialFragment fragment = new ShopSpecialFragment();
        Bundle args = new Bundle();
        args.putString(PAGE, page);
        args.putInt(INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }
    public ShopSpecialFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getString(PAGE);
            mIndex = getArguments().getInt(INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.shop_special_page, container, false);
        mContext=getActivity();

        initView();

        return mView;
    }

    /**
     * 初始化数据
     */
    private void initView()
    {
        backBtn=(LinearLayout)mView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(onClickListener);
        titleTv=(TypeFaceTextView)mView.findViewById(R.id.title_tv);
        titleTv.setText(R.string.home_shop_special_text);
        adIv=(ImageView)mView.findViewById(R.id.shopAdImage);

        mScrollView = (PullToRefreshScrollView)mView.findViewById(R.id.scrollview);
        mScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mScrollView.setOnRefreshListener(refreshListener);

        mListView=(ListViewForScrollView)mView.findViewById(R.id.shopListView);

    }


}
